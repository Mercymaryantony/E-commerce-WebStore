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

    @Query(value = "SELECT c.category_name FROM web_store.category c ORDER BY c.category_id ASC", nativeQuery = true)
    List<String> findTop3CategoryNames();

    @Query("SELECT c.categoryId FROM Category c WHERE c.categoryName = :categoryName")
    Integer findCategoryIdByCategoryName(@Param("categoryName") String categoryName);

    Optional<Category> findByCategoryName(String categoryName);

    @Query("SELECT c.categoryName FROM Category c")
    List<String> findAllCategoryNames();

    @Query("SELECT c FROM Category c WHERE LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Category> searchByCategoryName(@Param("name") String name);

    @Query("SELECT c FROM Category c WHERE LOWER(c.categoryDescription) LIKE LOWER(CONCAT('%', :description, '%'))")
    List<Category> searchByCategoryDescription(@Param("description") String description);

    @Query("SELECT c FROM Category c WHERE LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(c.categoryDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Category> searchByNameOrDescription(@Param("searchTerm") String searchTerm);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.catalogueCategory.category.categoryId = :categoryId")
    Long countProductsByCategoryId(@Param("categoryId") Integer categoryId);

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

    List<Category> findByCategoryNameContainingIgnoreCase(String name);

    // Find categories that have products from a specific seller
    @Query("SELECT DISTINCT c FROM Category c " +
           "JOIN c.catalogueCategories cc " +
           "JOIN cc.products p " +
           "WHERE p.seller.sellerId = :sellerId")
    List<Category> findBySellerId(@Param("sellerId") Integer sellerId);
}