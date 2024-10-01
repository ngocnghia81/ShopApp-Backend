package com.tripleng.shopappserver.controllers;

import com.tripleng.shopappserver.services.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;

@RestController
@RequestMapping("${api.prefix}/healthcheck")
@RequiredArgsConstructor
public class HealthCheckController {
    private final ICategoryService categoryService;

    @GetMapping("")
    public ResponseEntity<String> healthCheck() {
        try {
            categoryService.getAllCategories();
            String hostname = InetAddress.getLocalHost().getHostName();
            return ResponseEntity.ok(hostname);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("failed");
        }
    }
}
