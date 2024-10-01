package com.tripleng.shopappserver.listeners;

import com.tripleng.shopappserver.models.Product;
import com.tripleng.shopappserver.services.IProductRedisService;
import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
public class ProductListener {
    private final IProductRedisService productRedisService;
    private static final Logger logger = LoggerFactory.getLogger(ProductListener.class);

    @PrePersist
    public void postPersist(Product product) {
        logger.info("Cleared redis cache");
        productRedisService.clear();
    }

    @PreUpdate
    public void preUpdate(Product product) {
        logger.info("Cleared redis cache");
    }

    @PostUpdate
    public void postUpdate(Product product) {
        logger.info("Cleared redis cache");
        productRedisService.clear();
    }

    @PreRemove
    public void preRemove(Product product) {
        logger.info("Cleared redis cache");
    }

    @PostRemove
    public void postRemove(Product product) {
        logger.info("Cleared redis cache");
        productRedisService.clear();
    }
}
