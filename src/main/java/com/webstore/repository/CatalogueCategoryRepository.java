package com.webstore.repository;

import com.webstore.entity.CatalogueCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CatalogueCategoryRepository extends JpaRepository<CatalogueCategory, Integer> {

    Optional<CatalogueCategory> findByCatalogueCatalogueIdAndCategoryCategoryId(Integer catalogueId, Integer categoryId);

    boolean existsByCatalogueCatalogueIdAndCategoryCategoryId(Integer catalogueId, Integer categoryId);
    
    // Find all CatalogueCategory records for a given categoryId using Spring Data JPA method name
    // This will find by the category relationship
    List<CatalogueCategory> findByCategoryCategoryId(Integer categoryId);
    
    // Find all CatalogueCategory records for a given categoryId, eagerly loading catalogue
    @Query("SELECT DISTINCT cc FROM CatalogueCategory cc " +
           "LEFT JOIN FETCH cc.catalogue c " +
           "WHERE cc.category.categoryId = :categoryId")
    List<CatalogueCategory> findByCategoryIdWithCatalogue(@Param("categoryId") Integer categoryId);
    
    // Native query to directly get catalogue information for a category
    @Query(value = "SELECT DISTINCT c.catalogue_id, c.catalogue_name, c.catalogue_description " +
           "FROM web_store.catalogue_categories cc " +
           "INNER JOIN web_store.catalogues c ON cc.catalogue_id = c.catalogue_id " +
           "WHERE cc.category_id = :categoryId", nativeQuery = true)
    List<Object[]> findCatalogueInfoByCategoryIdNative(@Param("categoryId") Integer categoryId);
    
    // Native query to count products for a category
    @Query(value = "SELECT COUNT(DISTINCT p.product_id) " +
           "FROM web_store.products p " +
           "INNER JOIN web_store.catalogue_categories cc ON p.catalogue_category_id = cc.catalogue_category_id " +
           "WHERE cc.category_id = :categoryId", nativeQuery = true)
    Long countProductsByCategoryId(@Param("categoryId") Integer categoryId);
}
