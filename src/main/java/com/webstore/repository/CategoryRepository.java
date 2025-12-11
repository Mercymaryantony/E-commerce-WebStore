package com.webstore.repository;

import com.webstore.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

       
       boolean existsByCategoryName(String categoryName);

       Optional<Category> findByCategoryName(String categoryName);

       List<Category> findByCategoryNameContainingIgnoreCase(String name);

       List<Category> findByCategoryDescriptionContainingIgnoreCase(String description);

       // Projection for getting only category names
       interface CategoryNameProjection {
              String getCategoryName();
       }

       @Query("SELECT c.categoryName FROM Category c")
       List<CategoryNameProjection> findAllCategoryNames();

       // Projection for getting only category ID by name
       interface CategoryIdProjection {
              Integer getCategoryId();
       }

       @Query("SELECT c.categoryId FROM Category c WHERE c.categoryName = :categoryName")
       CategoryIdProjection findCategoryIdByCategoryName(@Param("categoryName") String categoryName);

       // Complex queries that need @Query
       @Query(value = "SELECT c.category_name FROM web_store.category c ORDER BY c.category_id ASC LIMIT 3", nativeQuery = true)
       List<String> findTop3CategoryNames();

       @Query("SELECT c FROM Category c WHERE LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                     "OR LOWER(c.categoryDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
       List<Category> searchByNameOrDescription(@Param("searchTerm") String searchTerm);

       @Query("SELECT COUNT(p) FROM Product p WHERE p.catalogueCategory.category.categoryId = :categoryId")
       Long countProductsByCategoryId(@Param("categoryId") Integer categoryId);

       // Complex queries with joins
       @Query("SELECT DISTINCT c FROM Category c " +
                     "LEFT JOIN FETCH c.catalogueCategories cc " +
                     "LEFT JOIN FETCH cc.catalogue " +
                     "WHERE c.categoryId = :id")
       Optional<Category> findByIdWithRelations(@Param("id") Integer id);

       @Query("SELECT DISTINCT c FROM Category c " +
                     "LEFT JOIN FETCH c.catalogueCategories cc " +
                     "LEFT JOIN FETCH cc.catalogue " +
                     "ORDER BY c.categoryId")
       List<Category> findAllWithRelations();

       @Query("SELECT DISTINCT c FROM Category c " +
                     "JOIN c.catalogueCategories cc " +
                     "JOIN cc.products p " +
                     "WHERE p.seller.sellerId = :sellerId")
       List<Category> findBySellerId(@Param("sellerId") Integer sellerId);
}