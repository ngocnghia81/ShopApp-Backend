package com.tripleng.shopappserver.controllers;

import com.tripleng.shopappserver.Components.LocalizationUtils;
import com.tripleng.shopappserver.dtos.OrderDTO;
import com.tripleng.shopappserver.response.ListOrderResponse;
import com.tripleng.shopappserver.response.OrderResponse;
import com.tripleng.shopappserver.services.IOrderService;
import com.tripleng.shopappserver.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/orders")
@RequiredArgsConstructor
public class OrderController {
    private final IOrderService orderService;
    private final LocalizationUtils localizationUtils;

    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderDTO order, BindingResult result) {
        try {
            if (result.hasErrors()) {
                List<String> errors =
                        result.getFieldErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList();
                return ResponseEntity.badRequest().body(errors.toString());
            }
            return ResponseEntity.ok(orderService.createOrder(order));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/{user_id}")
    public ResponseEntity<?> getOrders(@PathVariable Long user_id) {
        try {
            return ResponseEntity.ok(orderService.findOrderById(user_id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(orderService.findOrderById(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrder(@PathVariable Long id,
                                         @Valid @RequestBody OrderDTO orderDTO,
                                         BindingResult result) {
        try {
            if (result.hasErrors()) {
                List<String> errors =
                        result.getFieldErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList();
                return ResponseEntity.badRequest().body(errors.toString());
            }
            return ResponseEntity.ok(orderService.updateOrder(id, orderDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> deleteOrder(@PathVariable Long id) {
        try {
            orderService.deleteOrder(id);
            return ResponseEntity.ok(localizationUtils.getLocalizedMessage(MessageKeys.DELETE_ORDER_SUCCESSFULLY, id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }


    @GetMapping("get-orders-by-keyword")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ListOrderResponse> getOrdersByKeyword(
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "limit", required = false, defaultValue = "10") int limit
    ) {
        PageRequest pageRequest = PageRequest.of(page - 1, limit, Sort.by("id").ascending());

        Page<OrderResponse> orderPage = orderService.getOrdersByKeyword(keyword, pageRequest);

        int totalPages = orderPage.getTotalPages();
        List<OrderResponse> orders = orderPage.getContent();

        return ResponseEntity.ok(
                ListOrderResponse
                        .builder()
                        .orderResponses(orders)
                        .totalPages(totalPages)
                        .build()
        );
    }

    @GetMapping("/product/{product_id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getOrdersByProductId(@PathVariable Long product_id) {
        try {
            return ResponseEntity.ok(orderService.getOrdersByProductId(product_id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
