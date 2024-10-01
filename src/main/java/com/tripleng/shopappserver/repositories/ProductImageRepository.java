package com.tripleng.shopappserver.repositories;

import com.tripleng.shopappserver.models.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductId(Long productId);

    boolean existsByImageUrl(String imageUrl);
}
