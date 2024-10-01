package com.tripleng.shopappserver.services.serviceImpl;

import com.tripleng.shopappserver.dtos.OrderDetailDTO;
import com.tripleng.shopappserver.exceptions.DataNotFoundException;
import com.tripleng.shopappserver.models.Order;
import com.tripleng.shopappserver.models.OrderDetail;
import com.tripleng.shopappserver.models.Product;
import com.tripleng.shopappserver.repositories.OrderDetailRepository;
import com.tripleng.shopappserver.repositories.OrderRepository;
import com.tripleng.shopappserver.repositories.ProductRepository;
import com.tripleng.shopappserver.response.OrderDetailResponse;
import com.tripleng.shopappserver.services.IOrderDetailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderDetailService implements IOrderDetailService {
    private final OrderDetailRepository orderDetailRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public OrderDetailResponse createOrderDetail(OrderDetailDTO newOrderDetail) throws Exception {
        Order order =
                orderRepository.findById(newOrderDetail.getOrderId()).orElseThrow(() -> new DataNotFoundException(
                        "Order not found with id: " + newOrderDetail.getOrderId()));

        Product product = productRepository.findById(newOrderDetail.getProductId())
                .orElseThrow(() -> new DataNotFoundException("Product not found with id: " + newOrderDetail.getProductId()));

        OrderDetail orderDetail = modelMapper.map(newOrderDetail, OrderDetail.class);
        orderDetail.setProduct(product);
        orderDetail.setOrder(order);
        OrderDetail savedOrderDetail = orderDetailRepository.save(orderDetail);
        return modelMapper.map(savedOrderDetail, OrderDetailResponse.class);
    }

    @Override
    public OrderDetailResponse getOrderDetail(Long id) throws DataNotFoundException {
        OrderDetail orderDetail = orderDetailRepository.findById(id).orElseThrow(() -> new DataNotFoundException("Order not found with id: " + id));

        return modelMapper.map(orderDetail, OrderDetailResponse.class);
    }

    @Override
    @Transactional
    public OrderDetailResponse updateOrderDetail(Long id, OrderDetailDTO newOrderDetailData) throws DataNotFoundException, Exception {
        OrderDetail orderDetail = orderDetailRepository.findById(id).orElseThrow(() -> new DataNotFoundException("Order not found with id: " + id));
        modelMapper.typeMap(OrderDetailDTO.class, OrderDetail.class);
        modelMapper.map(newOrderDetailData, orderDetail);
        OrderDetail savedOrderDetail = orderDetailRepository.save(orderDetail);
        return modelMapper.map(savedOrderDetail, OrderDetailResponse.class);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        orderDetailRepository.deleteById(id);
    }

    @Override
    public List<OrderDetailResponse> findByOrderId(Long orderId) {
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(orderId);
        List<OrderDetailResponse> orderDetailResponses = new ArrayList<>();
        for (OrderDetail orderDetail : orderDetails) {
            orderDetailResponses.add(modelMapper.map(orderDetail, OrderDetailResponse.class));
        }
        return orderDetailResponses;
    }
}
