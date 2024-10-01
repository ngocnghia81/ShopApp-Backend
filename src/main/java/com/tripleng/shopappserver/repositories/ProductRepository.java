package com.tripleng.shopappserver.repositories;

import com.tripleng.shopappserver.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByName(String name);

    //    Page<Product> findByNameContaining(String name, Pageable pageable);
//
    Page<Product> findAll(Pageable pageable);//phân trang

    @Query(
            "select p from Product p where" +
                    "(:keyword is null or :keyword='' or p.name like %:keyword% or p.description like %:keyword%)" +
                    "and (:categoryId = 0 or p.category.id = :categoryId)"
    )
    Page<Product> searchProducts(@Param("keyword") String keyword, @Param("categoryId") Long categoryId, PageRequest pageRequest);

    @Query("""
            select p from Product p where p.id in :ids
            """)
    List<Product> findAllByIds(List<Long> ids);

}
