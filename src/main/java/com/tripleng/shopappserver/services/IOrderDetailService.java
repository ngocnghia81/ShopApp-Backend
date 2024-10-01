package com.tripleng.shopappserver.services;

import com.tripleng.shopappserver.dtos.OrderDetailDTO;
import com.tripleng.shopappserver.exceptions.DataNotFoundException;
import com.tripleng.shopappserver.response.OrderDetailResponse;

import java.util.List;

public interface IOrderDetailService {
    OrderDetailResponse createOrderDetail(OrderDetailDTO newOrderDetail) throws Exception;

    OrderDetailResponse getOrderDetail(Long id) throws DataNotFoundException;

    OrderDetailResponse updateOrderDetail(Long id, OrderDetailDTO newOrderDetailData)
            throws DataNotFoundException, Exception;

    void deleteById(Long id);

    List<OrderDetailResponse> findByOrderId(Long orderId);
}
