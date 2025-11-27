package com.webstore.repository;

import com.webstore.entity.ProductPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductPriceRepository extends JpaRepository<ProductPrice, Integer> {

    List<ProductPrice> findByProductProductId(Integer productId);

    Optional<ProductPrice> findByProductProductIdAndCurrencyCurrencyId(Integer productId, Integer currencyId);

    @Query("SELECT pp FROM ProductPrice pp JOIN FETCH pp.product p JOIN FETCH pp.currency c WHERE p.catalogueCategory.category.categoryId = :categoryId")
    List<ProductPrice> findByProductCategoryCategoryId(@Param("categoryId") Integer categoryId);

    @Query("SELECT pp FROM ProductPrice pp JOIN FETCH pp.product p JOIN FETCH pp.currency c WHERE p.productId IN :productIds")
    List<ProductPrice> findByProductProductIdIn(@Param("productIds") List<Integer> productIds);

    @Query("SELECT pp FROM ProductPrice pp JOIN FETCH pp.product p JOIN FETCH pp.currency c WHERE pp.currency.currencyId = :currencyId")
    List<ProductPrice> findByCurrencyCurrencyId(@Param("currencyId") Integer currencyId);

    // Native query to get price information with product and currency details for a product
    @Query(value = "SELECT " +
           "pp.product_id, " +
           "p.product_name, " +
           "c.currency_id, " +
           "c.currency_code, " +
           "c.currency_symbol, " +
           "pp.price_amount " +
           "FROM web_store.product_prices pp " +
           "INNER JOIN web_store.products p ON pp.product_id = p.product_id " +
           "INNER JOIN web_store.currencies c ON pp.currency_id = c.currency_id " +
           "WHERE pp.product_id = :productId", nativeQuery = true)
    List<Object[]> findPriceInfoByProductIdNative(@Param("productId") Integer productId);

    // JPQL query to get price information with product and currency details for a product
    @Query("SELECT pp FROM ProductPrice pp " +
           "JOIN FETCH pp.product p " +
           "JOIN FETCH pp.currency c " +
           "WHERE pp.product.productId = :productId")
    List<ProductPrice> findPriceInfoWithDetailsByProductId(@Param("productId") Integer productId);
}