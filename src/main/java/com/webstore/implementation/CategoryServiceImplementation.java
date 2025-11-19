package com.webstore.implementation;

import com.webstore.dto.request.CategoryRequestDto;
import com.webstore.dto.response.CategoryResponseDto;
import com.webstore.entity.Category;
import com.webstore.entity.CatalogueCategory;
import com.webstore.repository.CategoryRepository;
import com.webstore.repository.ProductRepository;
import com.webstore.service.CategoryService;
import com.webstore.util.AuthUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.EntityExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImplementation implements CategoryService {

    
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Autowired
    public CategoryServiceImplementation(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public CategoryResponseDto createCategory(CategoryRequestDto dto) {
        if (categoryRepository.existsByCategoryName(dto.getCategoryName())) {
            throw new EntityExistsException("Category name already exists");
        }

        Category category = new Category();
        category.setCategoryName(dto.getCategoryName());
        category.setCategoryDescription(dto.getCategoryDescription());

        String currentUser = AuthUtils.getCurrentUsername();
        category.setCreatedBy(currentUser);
        category.setUpdatedBy(currentUser);

        return mapToResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getAllCategories(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Category> categoryPage = categoryRepository.findAll(pageable);

        return categoryPage.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDto getCategoryById(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + id));
        return mapToResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponseDto updateCategory(Integer id, CategoryRequestDto dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + id));

        if (!category.getCategoryName().equals(dto.getCategoryName()) &&
                categoryRepository.existsByCategoryName(dto.getCategoryName())) {
            throw new EntityExistsException("Category with name " + dto.getCategoryName() + " already exists");
        }

        category.setCategoryName(dto.getCategoryName());
        category.setCategoryDescription(dto.getCategoryDescription());
        category.setUpdatedBy(AuthUtils.getCurrentUsername());

        return mapToResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Integer id) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Category not found with ID: " + id);
        }
        categoryRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDto> searchCategories(String searchTerm){
        if (searchTerm == null || searchTerm.trim().isEmpty()){
            return getAllCategories(0, 100);
        }

        List<Category> categories = categoryRepository.searchByNameOrDescription(searchTerm.trim());
        return categories.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private CategoryResponseDto mapToResponse(Category category) {
        CategoryResponseDto dto = new CategoryResponseDto();
        dto.setCategoryId(category.getCategoryId());
        dto.setCategoryName(category.getCategoryName());
        dto.setCategoryDescription(category.getCategoryDescription());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setCreatedBy(category.getCreatedBy());
        dto.setUpdatedAt(category.getUpdatedAt());
        dto.setUpdatedBy(category.getUpdatedBy());

        //get product count for this category
        Long productCount = categoryRepository.countProductsByCategoryId(category.getCategoryId());
        dto.setProductCount(productCount !=null ? productCount :0L);

        //get catalogues this category belongs to 
        List<CategoryResponseDto.CatalogueInfoDto> catalogues = category.getCatalogueCategories().stream()
                .map(CatalogueCategory::getCatalogue)
                .map(catalogue -> {
                    CategoryResponseDto.CatalogueInfoDto catalogueInfo = new CategoryResponseDto.CatalogueInfoDto();
                    catalogueInfo.setCatalogueId(catalogue.getCatalogueId());
                    catalogueInfo.setCatalogueName(catalogue.getCatalogueName());
                    catalogueInfo.setCatalogueDescription(catalogue.getCatalogueDescription());
                    return catalogueInfo;
                })
                .collect(Collectors.toList());
        dto.setCatalogues(catalogues);

        return dto;
    }
}
