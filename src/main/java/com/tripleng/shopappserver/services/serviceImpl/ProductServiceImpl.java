package com.tripleng.shopappserver.services.serviceImpl;

import com.tripleng.shopappserver.Components.LocalizationUtils;
import com.tripleng.shopappserver.dtos.ProductDTO;
import com.tripleng.shopappserver.dtos.ProductImageDTO;
import com.tripleng.shopappserver.exceptions.DataNotFoundException;
import com.tripleng.shopappserver.exceptions.InvalidParamException;
import com.tripleng.shopappserver.models.Category;
import com.tripleng.shopappserver.models.Product;
import com.tripleng.shopappserver.models.ProductImage;
import com.tripleng.shopappserver.repositories.CategoryRepository;
import com.tripleng.shopappserver.repositories.ProductImageRepository;
import com.tripleng.shopappserver.repositories.ProductRepository;
import com.tripleng.shopappserver.response.ProductResponse;
import com.tripleng.shopappserver.response.UploadImageResponse;
import com.tripleng.shopappserver.services.CloudinaryService;
import com.tripleng.shopappserver.services.IProductService;
import com.tripleng.shopappserver.utils.MessageKeys;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final CloudinaryService cloudinaryService;
    private LocalizationUtils localizationUtils;
    private final ProductRedisService productRedisService;


    @Override
    public Page<ProductResponse> getAllProducts(String keyword, Long categoryId, PageRequest pageRequest) {

        // Lấy danh sách sản phẩm theo trang(page) và giới hạn(limit)
        Page<Product> productPage = productRepository.searchProducts(keyword, categoryId, pageRequest);
        return productPage
                .map(ProductResponse::fromProduct)
                .map(response -> {
                    if (response.getThumbnail() != null) {
                        response.setThumbnail(cloudinaryService.getImageUrl(response.getThumbnail()));
                        return response;
                    }
                    response.setThumbnail(cloudinaryService.getImageUrl(cloudinaryService.getImageUrl("notFound_xdbnqe")));
                    return response;
                });
    }

    @Override
    @Transactional
    public Product createProduct(ProductDTO productDTO) throws DataNotFoundException {
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new DataNotFoundException("Category not found with id: " + productDTO.getCategoryId()));

        Product product = Product
                .builder()
                .name(productDTO.getName())
                .description(productDTO.getDescription())
                .category(category)
                .price(productDTO.getPrice())
                .thumbnail(productDTO.getThumbnail())
                .build();

        return productRepository.save(product);
    }

    @Override
    public ProductResponse getProductById(long id) throws DataNotFoundException {
        return productRepository.findById(id).
                map(product -> {
                    if (product.getThumbnail() != null) {
                        product.setThumbnail(cloudinaryService.getImageUrl(product.getThumbnail()));
                        return product;
                    }
                    product.setThumbnail(cloudinaryService.getImageUrl(cloudinaryService.getImageUrl("notFound_xdbnqe")));
                    return product;
                })
                .map(ProductResponse::fromProduct).orElseThrow(() -> new DataNotFoundException(
                        "Product not found" +
                                " " +
                                "with " +
                                "id: " + id));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(long id, ProductDTO productDTO) throws Exception {
        Product product = productRepository.findById(id).orElseThrow(() -> new DataNotFoundException("Product not found" +
                " " +
                "with " +
                "id: " + id));
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setThumbnail(productDTO.getThumbnail());

        productRepository.save(product);
        return ProductResponse.fromProduct(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) throws DataNotFoundException, IOException {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Product not found"));

        // Xóa tất cả các hình ảnh liên quan đến sản phẩm
        List<ProductImage> productImages = productImageRepository.findByProductId(id);
        for (ProductImage productImage : productImages) {
            cloudinaryService.deleteImageByPublicId(productImage.getImageUrl());
        }

        // Xóa sản phẩm
        productRepository.delete(product);
        log.info("Product deleted successfully with id: {}", id);
    }

    @Override
    public boolean existsByName(String name) {
        return productRepository.existsByName(name);
    }

    @Override
    @Transactional
    public ProductImage createProductImage(Long productId, ProductImageDTO productImageDTO) throws Exception {
        Product existingProduct = productRepository
                .findById(productId)
                .orElseThrow(() ->
                        new DataNotFoundException(
                                "Cannot find product with id: " + productImageDTO.getProductId()));
        ProductImage newProductImage = ProductImage.builder()
                .product(existingProduct)
                .imageUrl(productImageDTO.getImageUrl())
                .build();
        //Ko cho insert quá 5 ảnh cho 1 sản phẩm
        int size = productImageRepository.findByProductId(productId).size();
        if (size > ProductImage.MAXIMUM_IMAGES_PER_PRODUCT) {
            throw new InvalidParamException(
                    "Number of images must be <= "
                            + ProductImage.MAXIMUM_IMAGES_PER_PRODUCT);
        }
        return productImageRepository.save(newProductImage);
    }

    @Override
    @Transactional
    public UploadImageResponse upLoadImage(Long productId, List<MultipartFile> files) throws DataNotFoundException, IOException {
        UploadImageResponse uploadImageResponse = new UploadImageResponse();
        Product product = getProductById(productId);
        if (files.size() > 5) {
            uploadImageResponse.addError(localizationUtils.getLocalizedMessage(MessageKeys.UPLOAD_IMAGES_MAX_5));
            return uploadImageResponse;
        }
        for (MultipartFile file : files) {
            if (file.getSize() > 10 * 1024 * 1024) { // Kích thước > 10MB
                uploadImageResponse.addError(localizationUtils.getLocalizedMessage(MessageKeys.UPLOAD_IMAGES_FILE_MUST_BE_IMAGE, file.getOriginalFilename()));
            } else if (isImageFile(file)) {
                String imageUrl = cloudinaryService.upload(file);
                ProductImage productImage = ProductImage
                        .builder()
                        .imageUrl(imageUrl)
                        .product(product)
                        .build();
                productImageRepository.save(productImage);
                uploadImageResponse.addImageUrl(imageUrl);
            }
        }
        return uploadImageResponse;
    }

    @Override
    public String getImageByPublicId(String publicId) throws DataNotFoundException {
        if (productImageRepository.existsByImageUrl(publicId)) {
            return cloudinaryService.getImageUrl(publicId);
        }
        throw new DataNotFoundException("Image not found with publicId: " + publicId);
    }

    @Override
    public List<ProductImage> getImageByProducttId(Long id) {
        return this.productImageRepository.findByProductId(id).stream().map(productImage -> {
            productImage.setImageUrl(cloudinaryService.getImageUrl(productImage.getImageUrl()));
            return productImage;
        }).toList();
    }

    @Override
    public List<ProductResponse> getProductsByIds(List<Long> ids) {
        return productRepository.findAllByIds(ids).stream()
                .peek(product -> product.setThumbnail(cloudinaryService.getImageUrl(product.getThumbnail())))
                .map(ProductResponse::fromProduct).toList();
    }

    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    private Product getProductById(Long id) throws DataNotFoundException {
        return productRepository.findById(id).orElseThrow(() -> new DataNotFoundException("Product not found" +
                " " +
                "with " +
                "id: " + id));
    }

}
