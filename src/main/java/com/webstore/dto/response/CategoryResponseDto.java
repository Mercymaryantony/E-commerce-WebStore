package com.webstore.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class CategoryResponseDto {
    private Integer categoryId;
    private String categoryName;
    private String categoryDescription;
    private Integer productCount;  // Number of products under this category
    private List<CatalogueInfoDto> catalogues;  // Catalogues this category belongs to
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
<<<<<<< HEAD
    private List<ProductResponseDto> products;

    //fields for admin dashbaord 
    private Long productCount;
    private List<CatalogueInfoDto> catalogues;

    //Inner DTO for catalogue info
    @Data
    public static class CatalogueInfoDto{
        private Integer catalogueId;
        private String catalogueName;
        private String catalogueDescription;
    }
=======
>>>>>>> feature-seller
}