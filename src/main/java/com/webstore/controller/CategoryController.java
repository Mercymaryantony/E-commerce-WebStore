package com.webstore.controller;

import com.webstore.dto.request.CategoryRequestDto;
import com.webstore.dto.response.CategoryResponseDto;
import com.webstore.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("api/categories")
public class CategoryController {

    private CategoryService categoryService;

    @Autowired
    public void setCategoryService(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        List<CategoryResponseDto> categories;

        // if paginataion parameters are provided 
        if (page != null && size != null) {
            categories = categoryService.getAllCategories(page, size);
        } else {
            // If no pagination parameters, return all categories
            categories = categoryService.getAllCategories(0, Integer.MAX_VALUE);
        }

        if (categories.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> getCategoryById(@PathVariable Integer id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    // Search option for categories
    @GetMapping("/search")
    public ResponseEntity<List<CategoryResponseDto>> searchCategories(
            @RequestParam(required = false) String searchTerm) {
        List<CategoryResponseDto> categories;
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            categories = categoryService.getAllCategories(0, 100);
        } else {
            categories = categoryService.searchCategories(searchTerm);
        }
        if (categories.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(categories);
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDto> createCategory(@RequestBody @Valid CategoryRequestDto dto) {
        CategoryResponseDto created = categoryService.createCategory(dto);
        return ResponseEntity.status(201).body(created);
    }

    // Updating based on id
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> updateCategory(@PathVariable Integer id,
            @RequestBody @Valid CategoryRequestDto dto) {
        return ResponseEntity.ok(categoryService.updateCategory(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Integer id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}