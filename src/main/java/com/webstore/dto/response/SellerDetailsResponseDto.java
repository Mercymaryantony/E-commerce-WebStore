package com.webstore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerDetailsResponseDto {
    private Integer sellerId;
    private String sellerName;
    private String sellerEmail;
    private List<CatalogueDetailsDto> catalogues = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CatalogueDetailsDto {
        private Integer catalogueId;
        private String catalogueName;
        private String catalogueDescription;
        private List<CategoryDetailsDto> categories = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryDetailsDto {
        private Integer categoryId;
        private String categoryName;
        private String categoryDescription;
        private Long productCount; // Number of products for this seller in this category
    }
}