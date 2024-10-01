package com.tripleng.shopappserver.services;

import com.tripleng.shopappserver.dtos.CategoryDTO;
import com.tripleng.shopappserver.models.Category;

import java.util.List;

public interface ICategoryService {

    Category createCategory(CategoryDTO category);
    Category getCategoryById(Long id);
    List<Category> getAllCategories();
    Category updateCategory(Long id,CategoryDTO category);
    void deleteCategory(Long id);
}
