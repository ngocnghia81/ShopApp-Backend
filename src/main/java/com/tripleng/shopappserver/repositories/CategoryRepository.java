package com.tripleng.shopappserver.repositories;

import com.tripleng.shopappserver.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
