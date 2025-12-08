package com.webstore.controller;

import com.webstore.dto.request.CategoryRequestDto;
import com.webstore.dto.response.CategoryResponseDto;
import com.webstore.service.CategoryService;
import com.webstore.util.SecurityContextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

        if (page != null && size != null) {
            categories = categoryService.getAllCategories(page, size);
        } else {
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
        String role = SecurityContextUtils.getCurrentRole();
        if (role != null && "SELLER".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sellers cannot create categories");
        }
        CategoryResponseDto created = categoryService.createCategory(dto);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> updateCategory(@PathVariable Integer id,
            @RequestBody @Valid CategoryRequestDto dto) {
        String role = SecurityContextUtils.getCurrentRole();
        if (role != null && "SELLER".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sellers cannot update categories");
        }
        return ResponseEntity.ok(categoryService.updateCategory(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Integer id) {
        String role = SecurityContextUtils.getCurrentRole();
        if (role != null && "SELLER".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sellers cannot delete categories");
        }
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}