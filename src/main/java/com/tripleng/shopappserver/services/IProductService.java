package com.tripleng.shopappserver.services;

import com.tripleng.shopappserver.dtos.ProductDTO;
import com.tripleng.shopappserver.dtos.ProductImageDTO;
import com.tripleng.shopappserver.exceptions.DataNotFoundException;
import com.tripleng.shopappserver.models.Product;
import com.tripleng.shopappserver.models.ProductImage;
import com.tripleng.shopappserver.response.ProductResponse;
import com.tripleng.shopappserver.response.UploadImageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IProductService {
    Page<ProductResponse> getAllProducts(String keyword, Long categoryId, PageRequest pageRequest) throws DataNotFoundException;

    Product createProduct(ProductDTO productDTO) throws Exception;

    ProductResponse getProductById(long id) throws Exception;

    //    Page<ProductResponse> getAllProducts(PageRequest pageRequest);
    ProductResponse updateProduct(long id, ProductDTO productDTO) throws Exception;

    void deleteProduct(Long id) throws DataNotFoundException, IOException;

    boolean existsByName(String name);

    ProductImage createProductImage(
            Long productId,
            ProductImageDTO productImageDTO) throws Exception;

    UploadImageResponse upLoadImage(Long productId, List<MultipartFile> files) throws DataNotFoundException, IOException;

    String getImageByPublicId(String publicId) throws DataNotFoundException;

    List<ProductImage> getImageByProducttId(Long id);

    List<ProductResponse> getProductsByIds(List<Long> ids);
}
