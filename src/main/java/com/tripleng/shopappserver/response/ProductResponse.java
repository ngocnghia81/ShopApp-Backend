package com.tripleng.shopappserver.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripleng.shopappserver.models.BaseEntity;
import com.tripleng.shopappserver.models.Product;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
//@NoArgsConstructor
@Builder
@RequiredArgsConstructor
public class ProductResponse extends BaseEntity {
    private Long id;
    private String name;
    private Float price;
    private String thumbnail;
    private String description;

    @JsonProperty("category_id")
    private Long categoryId;

    public static ProductResponse fromProduct(Product product) {
        ProductResponse productResponse = ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .thumbnail(product.getThumbnail())
                .description(product.getDescription())
                .categoryId(product.getCategory().getId())
                .build();
        productResponse.setCreatedAt(product.getCreatedAt());
        productResponse.setUpdatedAt(product.getUpdatedAt());
        return productResponse;
    }
}
