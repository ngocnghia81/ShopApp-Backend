package com.tripleng.shopappserver.controllers;

import com.tripleng.shopappserver.Components.LocalizationUtils;
import com.tripleng.shopappserver.dtos.CategoryDTO;
import com.tripleng.shopappserver.models.Category;
import com.tripleng.shopappserver.response.CategoryResponse;
import com.tripleng.shopappserver.services.ICategoryService;
import com.tripleng.shopappserver.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final ICategoryService categoryService;
    private final LocalizationUtils localizationUtils;

    @PostMapping("")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryDTO categoryDTO, BindingResult bindingResult) {
        CategoryResponse categoryResponse = new CategoryResponse();
        if (bindingResult.hasErrors()) {
            List<String> error = bindingResult.getFieldErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            categoryResponse.setErrors(error);
            categoryResponse.setMessage(localizationUtils.getLocalizedMessage(MessageKeys.INSERT_CATEGORY_FAILED));
            return ResponseEntity.badRequest().body(categoryResponse);
        }
        try {
            Category category = categoryService.createCategory(categoryDTO);
            categoryResponse.setMessage(localizationUtils.getLocalizedMessage(MessageKeys.INSERT_CATEGORY_SUCCESSFULLY));
            categoryResponse.setCategory(category);
            return ResponseEntity.ok(categoryResponse);
        } catch (Exception e) {
            categoryResponse.setMessage(MessageKeys.INSERT_CATEGORY_FAILED);
            categoryResponse.setErrors(List.of(e.getMessage()));
            return ResponseEntity.badRequest().body(categoryResponse);
        }
    }

    @GetMapping("")
    public ResponseEntity<List<Category>> getAllCategories(
//            @RequestParam(value = "page") int page,
//            @RequestParam("limit") int limit
    ) {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }


    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable("id") Long id,
            @Valid @RequestBody CategoryDTO categoryDTO,
            BindingResult bindingResult
    ) {
        CategoryResponse categoryResponse = new CategoryResponse();
        if (bindingResult.hasErrors()) {
            List<String> error = bindingResult.getFieldErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            categoryResponse.setErrors(error);
            categoryResponse.setMessage(localizationUtils.getLocalizedMessage(MessageKeys.INSERT_CATEGORY_FAILED));
            return ResponseEntity.badRequest().body(categoryResponse);
        }
        try {
            Category category = categoryService.updateCategory(id, categoryDTO);
            categoryResponse.setCategory(category);
            categoryResponse.setMessage(localizationUtils.getLocalizedMessage(MessageKeys.UPDATE_CATEGORY_SUCCESSFULLY));
            return ResponseEntity.ok(categoryResponse);
        } catch (Exception e) {
            categoryResponse.setErrors(List.of(e.getMessage()));
            categoryResponse.setMessage(localizationUtils.getLocalizedMessage(MessageKeys.INSERT_CATEGORY_FAILED));
            return ResponseEntity.badRequest().body(categoryResponse);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CategoryResponse> deleteCategory(@PathVariable("id") Long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok(CategoryResponse.builder().message(MessageKeys.DELETE_CATEGORY_SUCCESSFULLY).build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(CategoryResponse.builder().errors(List.of(e.getMessage())).build());
        }
    }
}
