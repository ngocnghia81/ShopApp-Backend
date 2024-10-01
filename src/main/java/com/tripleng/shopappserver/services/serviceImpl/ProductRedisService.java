package com.tripleng.shopappserver.services.serviceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripleng.shopappserver.response.ProductResponse;
import com.tripleng.shopappserver.services.IProductRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class ProductRedisService implements IProductRedisService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private String getKeyFrom(String keyword, Long categoryId, PageRequest pageRequest) {
        int pageNumber = pageRequest.getPageNumber() + 1;
        int pageSize = pageRequest.getPageSize();
        Sort sort = pageRequest.getSort();
        String sortDirection = Objects.requireNonNull(sort.getOrderFor("id")).getDirection() == Sort.Direction.ASC ? "asc" : "desc";
        return String.format("all_products:%d:%d:%s", pageNumber, pageSize, sortDirection);
    }

    @Override
    public void clear() {
        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().flushAll();
    }

    @Override
    public List<ProductResponse> getAllProducts(String keyword, Long categoryId, PageRequest pageRequest) throws JsonProcessingException {

        String key = getKeyFrom(keyword, categoryId, pageRequest);
        String json = (String) redisTemplate.opsForValue().get(key);
        List<ProductResponse> productResponseList =
                json != null ?
                        objectMapper.readValue(json, new TypeReference<List<ProductResponse>>() {
                        }) : null;
        return productResponseList;
    }

    @Override
    public void saveAllProducts(List<ProductResponse> productResponses, String keyword, Long categoryId, PageRequest pageRequest) throws JsonProcessingException {

        String key = getKeyFrom(keyword, categoryId, pageRequest);
        redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(productResponses));
    }
}
