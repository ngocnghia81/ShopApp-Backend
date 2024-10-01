package com.tripleng.shopappserver.services.serviceImpl;

import com.tripleng.shopappserver.dtos.OrderDTO;
import com.tripleng.shopappserver.exceptions.DataNotFoundException;
import com.tripleng.shopappserver.models.Order;
import com.tripleng.shopappserver.models.OrderDetail;
import com.tripleng.shopappserver.models.OrderStatus;
import com.tripleng.shopappserver.models.User;
import com.tripleng.shopappserver.repositories.OrderDetailRepository;
import com.tripleng.shopappserver.repositories.OrderRepository;
import com.tripleng.shopappserver.repositories.UserRepository;
import com.tripleng.shopappserver.response.OrderResponse;
import com.tripleng.shopappserver.services.CloudinaryService;
import com.tripleng.shopappserver.services.IOrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements IOrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final OrderDetailRepository orderDetailRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderDTO orderDTO) throws DataNotFoundException {
        LocalDate shippingDate = orderDTO.getShippingDate() == null ? LocalDate.now() : orderDTO.getShippingDate();
        if (shippingDate.isBefore(LocalDate.now())) {
            throw new DataNotFoundException("Shipping Date cannot be null or empty and must be least today!");
        }
        User user = userRepository.findById(orderDTO.getUserId()).orElseThrow(() -> new DataNotFoundException("Cannot " +
                "find user with id: " + orderDTO.getUserId()));
        System.out.println(user.toString());
        modelMapper.typeMap(OrderDTO.class, Order.class).addMappings(mapper -> {
            mapper.skip(Order::setId);
        });
        Order order = modelMapper.map(orderDTO, Order.class);
        order.setUser(user);
        order.setOrderDate(new Date());
        order.setActive(true);
        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);
        return modelMapper.map(order, OrderResponse.class);
    }

    @Override
    @Transactional
    public OrderResponse updateOrder(Long id, OrderDTO orderDTO) throws DataNotFoundException {
        Order orderToUpdate = orderRepository.findById(id).orElseThrow(() -> new DataNotFoundException("Cannot find " +
                "order with order id: " + id));
        User user = userRepository.findById(orderDTO.getUserId()).orElseThrow(() -> new DataNotFoundException("Cannot " +
                "find user with id: " + orderDTO.getUserId()));

        modelMapper.typeMap(OrderDTO.class, Order.class)
                .addMappings(configurableConditionExpression -> configurableConditionExpression.skip(Order::setId));
        modelMapper.map(orderDTO, orderToUpdate);
        orderToUpdate.setUser(user);
        orderRepository.save(orderToUpdate);
        return modelMapper.map(orderToUpdate, OrderResponse.class);
    }

    @Override
    public OrderResponse findOrderById(Long id) throws DataNotFoundException {
        Order order = orderRepository.findById(id).orElseThrow(() -> new DataNotFoundException("Cannot find order with " +
                "id: " + id));

        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());

        orderDetails =
                orderDetails.stream().peek(ord -> ord.getProduct().setThumbnail(cloudinaryService.getImageUrl(ord.getProduct().getThumbnail()))).toList();

        OrderResponse orderResponse = modelMapper.map(order, OrderResponse.class);
        orderResponse.setOrderDetails(orderDetails);

        return orderResponse;
    }

    @Override
    public List<OrderResponse> findAllOrders(Long user_id) throws DataNotFoundException {
        User user = userRepository.findById(user_id).orElseThrow(() -> new DataNotFoundException("Cannot find user " +
                "with id: " + user_id));
        List<Order> orders = orderRepository.findAllByUserId(user_id);
        List<OrderResponse> orderResponses = new ArrayList<>();
        for (Order order : orders) {
            orderResponses.add(modelMapper.map(order, OrderResponse.class));
        }
        return orderResponses;
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id).orElse(null);
        //no hard-delete, => please soft-delete
        if (order != null) {
            order.setActive(false);
            orderRepository.save(order);
        }
    }

    @Override
    public Page<OrderResponse> getOrdersByKeyword(String keyword, Pageable pageable) {
        return orderRepository.findAllByKeyword(keyword, pageable).map(order -> modelMapper.map(order, OrderResponse.class));
    }

    @Override
    public List<OrderResponse> getOrdersByProductId(Long productId) {
        return orderRepository
                .findAllByProductId(productId).stream().map(order -> modelMapper.map(order, OrderResponse.class)).toList();
    }
}
