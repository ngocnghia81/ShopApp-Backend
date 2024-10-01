package com.tripleng.shopappserver.services;

import com.tripleng.shopappserver.dtos.OrderDTO;
import com.tripleng.shopappserver.exceptions.DataNotFoundException;
import com.tripleng.shopappserver.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IOrderService {
    OrderResponse createOrder(OrderDTO order) throws DataNotFoundException;

    OrderResponse updateOrder(Long id, OrderDTO order) throws DataNotFoundException;

    OrderResponse findOrderById(Long id) throws DataNotFoundException;

    List<OrderResponse> findAllOrders(Long user_id) throws DataNotFoundException;

    void deleteOrder(Long id);

    Page<OrderResponse> getOrdersByKeyword(String keyword, Pageable pageable);

    List<OrderResponse> getOrdersByProductId(Long productId);
}
