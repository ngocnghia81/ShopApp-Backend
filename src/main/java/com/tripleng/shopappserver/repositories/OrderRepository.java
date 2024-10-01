package com.tripleng.shopappserver.repositories;

import com.tripleng.shopappserver.models.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByUserId(Long userId);

    Optional<Order> findById(Long id);

    @Query(
            "SELECT o FROM Order o " +
                    "WHERE o.active = TRUE AND (:keyword IS NULL OR :keyword = '' " +
                    "OR o.address LIKE %:keyword% " +
                    "OR o.fullName LIKE %:keyword% " +
                    "OR o.note LIKE %:keyword% " +
                    "OR o.phoneNumber LIKE %:keyword%)"
    )
    Page<Order> findAllByKeyword(@Param("keyword") String keyword, Pageable pageable);


    @Query("""
                    SELECT o FROM Order o
                    JOIN o.orderDetail od
                    WHERE od.product.id = :productId
            """)
    List<Order> findAllByProductId(Long productId);
}
