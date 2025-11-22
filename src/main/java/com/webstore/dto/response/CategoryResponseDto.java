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
}