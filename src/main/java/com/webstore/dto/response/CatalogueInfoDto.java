package com.webstore.dto.response;

import lombok.Data;

@Data
public class CatalogueInfoDto {
    private Integer catalogueId;
    private String catalogueName;
    private String catalogueDescription;
}