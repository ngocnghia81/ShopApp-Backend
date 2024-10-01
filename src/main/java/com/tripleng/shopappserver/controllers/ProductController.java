package com.tripleng.shopappserver.controllers;

import com.github.javafaker.Faker;
import com.tripleng.shopappserver.dtos.ProductDTO;
import com.tripleng.shopappserver.exceptions.DataNotFoundException;
import com.tripleng.shopappserver.models.Product;
import com.tripleng.shopappserver.response.ProductListResponse;
import com.tripleng.shopappserver.response.ProductResponse;
import com.tripleng.shopappserver.services.IProductRedisService;
import com.tripleng.shopappserver.services.IProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
    private final IProductService productService;
    private final IProductRedisService productRedisService;

    @GetMapping("")
    public ResponseEntity<ProductListResponse> getProducts(
            @RequestParam(value = "keyword", defaultValue = "") String keyword,
            @RequestParam(value = "categoryId", defaultValue = "0") Long categoryId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        try {
            int totalPages = 0;
            PageRequest pageRequest = PageRequest.of(page - 1, limit, Sort.by("id").ascending());
            List<ProductResponse> productResponses = productRedisService.getAllProducts(keyword, categoryId, pageRequest);
            log.info(String.format("Get products from redis with keyword: %s, categoryId: %s, page: %d, limit: %d", keyword, categoryId, page, limit));
            if (productResponses == null) {
                Page<ProductResponse> productPage = productService.getAllProducts(keyword, categoryId, pageRequest);
                totalPages = productPage.getTotalPages();
                productRedisService.saveAllProducts(productPage.getContent(), keyword, categoryId, pageRequest);
            }
            return ResponseEntity.ok(
                    ProductListResponse
                            .builder()
                            .productResponseList(productResponses)
                            .totalPage(totalPages)
                            .build()
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(productService.getProductById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/by-ids")
    public ResponseEntity<List<ProductResponse>> getProductsByIds(@RequestParam List<Long> ids) {
        try {
            return ResponseEntity.ok(productService.getProductsByIds(ids));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("")
    public ResponseEntity<?> createProduct(
            @Valid @RequestBody ProductDTO productDTO,
            BindingResult result
    ) {
        try {
            if (result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errorMessages);
            }
            Product newProduct = productService.createProduct(productDTO);
            return ResponseEntity.ok(newProduct);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(value = "uploads/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImages(
            @PathVariable("id") Long productId,
            @ModelAttribute("files") List<MultipartFile> files
    ) {
        try {
            return ResponseEntity.ok(productService.upLoadImage(productId, files));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok("Deleted product by id = " + id);
        } catch (DataNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@Valid @RequestBody ProductDTO productDTO,
                                           BindingResult bindingResult, @PathVariable Long id) {
        try {
            if (bindingResult.hasErrors()) {
                List<String> errors =
                        bindingResult.getFieldErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList();
                return ResponseEntity.badRequest().body(errors.toString());
            }
            return ResponseEntity.ok(productService.updateProduct(id, productDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("images/pub-{publicId}")
    public ResponseEntity<?> viewImages(@PathVariable("publicId") String publicId) {

        try {
            return ResponseEntity.ok(productService.getImageByPublicId(publicId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("images/{id}")
    public ResponseEntity<?> getImages(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(productService.getImageByProducttId(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("gennerateFakeProducts")
    public ResponseEntity<?> generateFakeProducts() throws Exception {
        Faker faker = new Faker();
        for (int i = 0; i < 500; i++) {
            String productName = faker.commerce().productName();
            if (productService.existsByName(productName)) {
                continue;
            }
            ProductDTO productDTO = ProductDTO
                    .builder()
                    .name(productName)
                    .price((float) faker.number().numberBetween(10, 90000000))
                    .description(faker.lorem().sentence())
                    .thumbnail("")
                    .categoryId((long) faker.number().numberBetween(1, 4))
                    .build();
            productService.createProduct(productDTO);
        }
        return ResponseEntity.ok("Generated fake products");
    }
}
