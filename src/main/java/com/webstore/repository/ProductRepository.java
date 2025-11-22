package com.webstore.repository;

import com.webstore.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    boolean existsByProductName(String productName);
    
    // Find products by catalogue-category ID
    @Query("SELECT p FROM Product p WHERE p.catalogueCategory.catalogueCategoryId = :catalogueCategoryId")
    List<Product> findByCatalogueCategoryId(@Param("catalogueCategoryId") Integer catalogueCategoryId);

    // Find products by category ID (through catalogueCategory)
    @Query("SELECT p FROM Product p WHERE p.catalogueCategory.category.categoryId = :categoryId")
    List<Product> findByCategoryId(@Param("categoryId") Integer categoryId);

    // Find products by category name (through catalogueCategory)
    @Query("SELECT p FROM Product p WHERE p.catalogueCategory.category.categoryName = :categoryName")
    List<Product> findByCategoryName(@Param("categoryName") String categoryName);

    // Find products by catalogue ID
    @Query("SELECT p FROM Product p WHERE p.catalogueCategory.catalogue.catalogueId = :catalogueId")
    List<Product> findByCatalogueId(@Param("catalogueId") Integer catalogueId);

    // Find products by both catalogue and category
    @Query("SELECT p FROM Product p WHERE p.catalogueCategory.catalogue.catalogueId = :catalogueId " +
           "AND p.catalogueCategory.category.categoryId = :categoryId")
    List<Product> findByCatalogueIdAndCategoryId(@Param("catalogueId") Integer catalogueId, 
                                                   @Param("categoryId") Integer categoryId);

    // Find top N products by catalogue-category ID
    @Query("SELECT p FROM Product p WHERE p.catalogueCategory.catalogueCategoryId = :catalogueCategoryId " +
           "ORDER BY p.createdAt DESC")
    List<Product> findTop5ByCatalogueCategoryIdOrderByCreatedAtDesc(@Param("catalogueCategoryId") Integer catalogueCategoryId);

    // Get product names by category for WhatsApp display
    @Query("SELECT p.productName FROM Product p WHERE p.catalogueCategory.category.categoryId = :categoryId " +
           "ORDER BY p.productId ASC")
    List<String> findProductNamesByCategoryId(@Param("categoryId") Integer categoryId);

    // Find product ID by product name
    @Query("SELECT p.productId FROM Product p WHERE p.productName = :productName")
    Integer findProductIdByProductName(@Param("productName") String productName);
    
    // Delete all products for a category
    @Modifying
    @Query("DELETE FROM Product p WHERE p.catalogueCategory.category.categoryId = :categoryId")
    void deleteByCategoryId(@Param("categoryId") Integer categoryId);
}
