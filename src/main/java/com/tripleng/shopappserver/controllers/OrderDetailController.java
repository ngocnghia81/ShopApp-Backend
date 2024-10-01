package com.tripleng.shopappserver.controllers;

import com.tripleng.shopappserver.Components.LocalizationUtils;
import com.tripleng.shopappserver.dtos.OrderDetailDTO;
import com.tripleng.shopappserver.services.IOrderDetailService;
import com.tripleng.shopappserver.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/order_details")
@RequiredArgsConstructor
public class OrderDetailController {
    private final IOrderDetailService orderDetailService;
    private LocalizationUtils localizationUtils;

    @PostMapping("")
    public ResponseEntity<?> createOrderDetail(@Valid @RequestBody OrderDetailDTO dto, BindingResult result) {
        try {
            if (result.hasErrors()) {
                List<String> errors =
                        result.getFieldErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList();
                return ResponseEntity.badRequest().body(errors.toString());
            }
            return ResponseEntity.ok().body(orderDetailService.createOrderDetail(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderDetail(@PathVariable Long id) {
        try {
            return ResponseEntity.ok().body(orderDetailService.getOrderDetail(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/order/{order_id}")
    public ResponseEntity<?> getOrderDetails(@PathVariable Long order_id) {
        try {
            return ResponseEntity.ok(orderDetailService.findByOrderId(order_id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrderDetail(@Valid @RequestBody OrderDetailDTO dto, BindingResult result,
                                               @PathVariable Long id) {
        if (result.hasErrors()) {
            List<String> errors =
                    result.getFieldErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList();
            return ResponseEntity.badRequest().body(errors.toString());
        }
        try {
            return ResponseEntity.ok().body(orderDetailService.updateOrderDetail(id, dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrderDetail(@PathVariable Long id) {
        try {
            orderDetailService.deleteById(id);
            return ResponseEntity.ok().body(localizationUtils.getLocalizedMessage(MessageKeys.DELETE_ORDER_DETAIL_SUCCESSFULLY));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
