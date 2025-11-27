package com.webstore.dto.response;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class ProductResponseDto {
    private Integer productId;
    private String productName;
    private String productDescription;
    private String imageUrl;
    private Integer stock;
    private Integer sellerId;
    private CatalogueCategoryResponseDto catalogueCategory;
    private List<PriceInfoDto> prices;  // List of price details instead of single price
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    // Inner DTO for price info (similar to CatalogueInfoDto in CategoryResponseDto)
    @Data
    public static class PriceInfoDto {
        private Integer productId;
        private String productName;
        private Integer currencyId;
        private String currencyCode;
        private String currencySymbol;
        private BigInteger priceAmount;
    }
}