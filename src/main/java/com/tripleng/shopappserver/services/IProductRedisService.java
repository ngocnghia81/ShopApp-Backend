package com.tripleng.shopappserver.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tripleng.shopappserver.response.ProductResponse;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface IProductRedisService {
    void clear();

    List<ProductResponse> getAllProducts(
            String keyword, Long categoryId, PageRequest pageRequest
    ) throws JsonProcessingException; // <1>

    void saveAllProducts(List<ProductResponse> productResponses, String keyword, Long categoryId, PageRequest pageRequest) throws JsonProcessingException;
}
