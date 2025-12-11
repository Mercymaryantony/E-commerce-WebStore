package com.webstore.repository;

import com.webstore.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
       List<Product> findTop5ByCatalogueCategoryIdOrderByCreatedAtDesc(
                     @Param("catalogueCategoryId") Integer catalogueCategoryId);

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

       // Search by product name (case-insensitive, partial match) with seller
       @Query("SELECT DISTINCT p FROM Product p " +
                     "LEFT JOIN FETCH p.seller s " +
                     "WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
       List<Product> searchByProductName(@Param("searchTerm") String searchTerm);

       // Search by product description (case-insensitive, partial match) with seller
       @Query("SELECT DISTINCT p FROM Product p " +
                     "LEFT JOIN FETCH p.seller s " +
                     "WHERE LOWER(p.productDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
       List<Product> searchByProductDescription(@Param("searchTerm") String searchTerm);

       // Search by both name and description with seller
       @Query("SELECT DISTINCT p FROM Product p " +
                     "LEFT JOIN FETCH p.seller s " +
                     "WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                     "OR LOWER(p.productDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
       List<Product> searchByNameOrDescription(@Param("searchTerm") String searchTerm);

       // Find all products with seller eagerly loaded
       @Query("SELECT DISTINCT p FROM Product p " +
                     "LEFT JOIN FETCH p.seller s " +
                     "ORDER BY p.productId")
       List<Product> findAllWithSeller();

       // Find product by ID with seller eagerly loaded
       @Query("SELECT DISTINCT p FROM Product p " +
                     "LEFT JOIN FETCH p.seller s " +
                     "WHERE p.productId = :id")
       Optional<Product> findByIdWithSeller(@Param("id") Integer id);

       // Get seller_id directly from products table
       @Query(value = "SELECT seller_id FROM web_store.products WHERE product_id = :productId", nativeQuery = true)
       Integer findSellerIdByProductId(@Param("productId") Integer productId);

       // Find all products by seller ID with eager loading
       @Query("SELECT DISTINCT p FROM Product p " +
                     "LEFT JOIN FETCH p.catalogueCategory cc " +
                     "LEFT JOIN FETCH cc.catalogue c " +
                     "LEFT JOIN FETCH cc.category cat " +
                     "WHERE p.seller.sellerId = :sellerId")
       List<Product> findBySellerIdWithRelations(@Param("sellerId") Integer sellerId);

       // Find products with pagination, filtered by seller ID
       @Query("SELECT DISTINCT p FROM Product p " +
                     "LEFT JOIN FETCH p.seller s " +
                     "WHERE p.seller.sellerId = :sellerId " +
                     "ORDER BY p.productId")
       List<Product> findAllBySellerId(@Param("sellerId") Integer sellerId);

       // Search products by name or description, filtered by seller ID
       @Query("SELECT DISTINCT p FROM Product p " +
                     "LEFT JOIN FETCH p.seller s " +
                     "WHERE p.seller.sellerId = :sellerId " +
                     "AND (LOWER(p.productName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                     "OR LOWER(p.productDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
       List<Product> searchBySellerIdAndNameOrDescription(@Param("sellerId") Integer sellerId,
                     @Param("searchTerm") String searchTerm);
}
