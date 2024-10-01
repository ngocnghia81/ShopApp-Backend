package com.tripleng.shopappserver.services.serviceImpl;

import com.tripleng.shopappserver.dtos.CategoryDTO;
import com.tripleng.shopappserver.models.Category;
import com.tripleng.shopappserver.repositories.CategoryRepository;
import com.tripleng.shopappserver.services.ICategoryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService implements ICategoryService {
    private final CategoryRepository _categoryRepository;

    @Override
    @Transactional
    public Category createCategory(CategoryDTO category) {
        Category newCategory = Category
                .builder()
                .name(category.getName())
                .build();
        return _categoryRepository.save(newCategory);
    }

    @Override
    public Category getCategoryById(Long id) {
        return _categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Category not found"));
    }

    @Override
    public List<Category> getAllCategories() {
        return _categoryRepository.findAll();
    }

    @Override
    @Transactional
    public Category updateCategory(Long id, CategoryDTO category) {
        Category oldCategory = getCategoryById(id);
        oldCategory.setName(category.getName());
        _categoryRepository.save(oldCategory);
        return oldCategory;
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        _categoryRepository.deleteById(id);
    }
}
