package com.webstore.repository;

import com.webstore.entity.Catalogue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CatalogueRepository extends JpaRepository<Catalogue, Integer> {
    List<Catalogue> findByCatalogueNameContainingIgnoreCase(String name);

    // Find catalogues that have products from a specific seller
    @Query("SELECT DISTINCT c FROM Catalogue c " +
           "JOIN c.catalogueCategories cc " +
           "JOIN cc.products p " +
           "WHERE p.seller.sellerId = :sellerId")
    List<Catalogue> findBySellerId(@Param("sellerId") Integer sellerId);
}