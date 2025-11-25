package com.webstore.implementation;

import com.webstore.dto.request.CategoryRequestDto;
import com.webstore.dto.response.CategoryResponseDto;
import com.webstore.entity.Category;
import com.webstore.entity.CatalogueCategory;
import com.webstore.repository.CategoryRepository;
import com.webstore.repository.CatalogueCategoryRepository;
import com.webstore.repository.ProductRepository;
import com.webstore.service.CategoryService;
import com.webstore.util.AuthUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.EntityExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImplementation implements CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryServiceImplementation.class);

    private final CategoryRepository categoryRepository;
    private final CatalogueCategoryRepository catalogueCategoryRepository;
    private final ProductRepository productRepository;
    private final EntityManager entityManager;

    @Autowired
    public CategoryServiceImplementation(CategoryRepository categoryRepository, 
                                        CatalogueCategoryRepository catalogueCategoryRepository,
                                        ProductRepository productRepository,
                                        EntityManager entityManager) {
        this.categoryRepository = categoryRepository;
        this.catalogueCategoryRepository = catalogueCategoryRepository;
        this.productRepository = productRepository;
        this.entityManager = entityManager;
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
        logger.info("=== getAllCategories: page={}, size={} ===", page, size);
        
        // Use regular findAll with Pageable to avoid ConcurrentModificationException
        Pageable pageable = PageRequest.of(page, size);
        Page<Category> categoryPage = categoryRepository.findAll(pageable);
        logger.info("✓ Loaded {} categories from database (page {}, total: {})", 
                   categoryPage.getContent().size(), page, categoryPage.getTotalElements());
        
        List<Category> categories = categoryPage.getContent();
        logger.info("Processing {} categories for page {}", categories.size(), page);
        
        return categories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDto getCategoryById(Integer id) {
        // Use regular findById instead of findByIdWithRelations to avoid ConcurrentModificationException
        // We don't need the eagerly loaded collections since we use native queries in mapToResponse
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
    @Transactional(readOnly = true)
    public List<CategoryResponseDto> searchByName(String name) {
        logger.info("Searching categories with name: {}", name);
        return categoryRepository.findByCategoryNameContainingIgnoreCase(name)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteCategory(Integer id) {
        // Verify category exists
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + id));
        
        // Step 1: Delete all products for this category first (to avoid foreign key violations)
        // Products reference CatalogueCategory, so they must be deleted first
        logger.info("Deleting products for category ID: {}", id);
        productRepository.deleteByCategoryId(id);
        entityManager.flush(); // Ensure products are deleted before proceeding
        
        // Step 2: Delete CatalogueCategory records for this category
        // This is handled by cascade, but we can also delete explicitly to avoid issues
        logger.info("Deleting CatalogueCategory records for category ID: {}", id);
        List<CatalogueCategory> catalogueCategories = catalogueCategoryRepository.findByCategoryCategoryId(id);
        if (catalogueCategories != null && !catalogueCategories.isEmpty()) {
            catalogueCategoryRepository.deleteAll(catalogueCategories);
            entityManager.flush(); // Ensure CatalogueCategory records are deleted
        }
        
        // Step 3: Delete the category itself
        logger.info("Deleting category ID: {}", id);
        categoryRepository.delete(category);
        entityManager.flush(); // Ensure category is deleted
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
        try {
            logger.debug("mapToResponse: Starting for category id={}, name={}", 
                        category.getCategoryId(), category.getCategoryName());
            
            CategoryResponseDto dto = new CategoryResponseDto();
            dto.setCategoryId(category.getCategoryId());
            dto.setCategoryName(category.getCategoryName());
            dto.setCategoryDescription(category.getCategoryDescription());
            dto.setCreatedAt(category.getCreatedAt());
            dto.setCreatedBy(category.getCreatedBy());
            dto.setUpdatedAt(category.getUpdatedAt());
            dto.setUpdatedBy(category.getUpdatedBy());
            logger.debug("✓ Basic fields set for category {}", category.getCategoryId());

            // Fetch catalogue information directly using native query
            // This is the most reliable way to get catalogue data
            Integer categoryId = category.getCategoryId();
            logger.info("Fetching catalogue information for categoryId: {}", categoryId);
            
            // First, try native query to get catalogue info directly
            List<Object[]> catalogueData = catalogueCategoryRepository.findCatalogueInfoByCategoryIdNative(categoryId);
            
            List<CategoryResponseDto.CatalogueInfoDto> catalogues = new java.util.ArrayList<>();
            Long productCount = 0L;
            
            if (catalogueData != null && !catalogueData.isEmpty()) {
                logger.info("Found {} catalogues via native query for category {}", catalogueData.size(), categoryId);
                
                // Extract catalogue information from native query results
                for (Object[] row : catalogueData) {
                    try {
                        Integer catalogueId = (Integer) row[0];
                        String catalogueName = (String) row[1];
                        String catalogueDescription = (String) row[2];
                        
                        CategoryResponseDto.CatalogueInfoDto catalogueInfo = new CategoryResponseDto.CatalogueInfoDto();
                        catalogueInfo.setCatalogueId(catalogueId);
                        catalogueInfo.setCatalogueName(catalogueName);
                        catalogueInfo.setCatalogueDescription(catalogueDescription);
                        catalogues.add(catalogueInfo);
                        
                        logger.info("✓ Added catalogue ID: {}, Name: {}", catalogueId, catalogueName);
                    } catch (Exception e) {
                        logger.error("Error processing catalogue data: {}", e.getMessage(), e);
                    }
                }
                
                // Get product count using native query to avoid ConcurrentModificationException
                // This avoids accessing entity collections which can cause concurrent modification issues
                Long productCountLong = catalogueCategoryRepository.countProductsByCategoryId(categoryId);
                productCount = productCountLong != null ? productCountLong : 0L;
            } else {
                logger.warn("⚠ No catalogues found for category {} via native query", categoryId);
                // Still get product count even if no catalogues found
                Long productCountLong = catalogueCategoryRepository.countProductsByCategoryId(categoryId);
                productCount = productCountLong != null ? productCountLong : 0L;
            }
            
            dto.setProductCount(productCount);
            dto.setCatalogues(catalogues);
            logger.info("Category {}: productCount={}, catalogues={}", 
                       category.getCategoryId(), productCount, catalogues.size());

            logger.debug("✓ Successfully completed mapToResponse for category {}", category.getCategoryId());
            return dto;
        } catch (Exception e) {
            logger.error("=== FATAL ERROR in mapToResponse for category {} ===", 
                        category != null ? category.getCategoryId() : "null");
            logger.error("Exception type: {}", e.getClass().getName());
            logger.error("Error message: {}", e.getMessage());
            logger.error("Full stack trace:", e);
            
            // If anything fails, return a basic DTO with minimal info
            CategoryResponseDto dto = new CategoryResponseDto();
            if (category != null) {
                dto.setCategoryId(category.getCategoryId());
                dto.setCategoryName(category.getCategoryName());
                dto.setCategoryDescription(category.getCategoryDescription());
                dto.setProductCount(0L);
                dto.setCatalogues(List.of());
                if (category.getCreatedAt() != null) dto.setCreatedAt(category.getCreatedAt());
                if (category.getCreatedBy() != null) dto.setCreatedBy(category.getCreatedBy());
                if (category.getUpdatedAt() != null) dto.setUpdatedAt(category.getUpdatedAt());
                if (category.getUpdatedBy() != null) dto.setUpdatedBy(category.getUpdatedBy());
            }
            return dto;
        }
    }
}
