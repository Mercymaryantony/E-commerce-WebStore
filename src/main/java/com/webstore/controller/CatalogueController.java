package com.webstore.controller;

import com.webstore.dto.request.CatalogueRequestDto;
import com.webstore.dto.response.CatalogueResponseDto;
import com.webstore.service.CatalogueService;
import com.webstore.util.SecurityContextUtils;
import jakarta.validation.Valid;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Setter
@RestController
@RequestMapping("/api/catalogues")
public class CatalogueController {

    private CatalogueService catalogueService;

    @Autowired
    @Qualifier("catalogueServiceImplementation")
    public void setCatalogueService(CatalogueService catalogueService) {
        this.catalogueService = catalogueService;
    }

    @PostMapping
    public ResponseEntity<CatalogueResponseDto> createCatalogue(@RequestBody @Valid CatalogueRequestDto dto) {
        String role = SecurityContextUtils.getCurrentRole();
        if (role != null && "SELLER".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sellers cannot create catalogues");
        }
        return ResponseEntity.ok(catalogueService.createCatalogue(dto));
    }

    @GetMapping
    public ResponseEntity<List<CatalogueResponseDto>> getAllCatalogues(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        List<CatalogueResponseDto> catalogues;

        if (page != null && size != null) {
            catalogues = catalogueService.getAllCatalogues(page, size);
        } else {
            catalogues = catalogueService.getAllCatalogues(0, Integer.MAX_VALUE);
        }

        if (catalogues.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(catalogues);
    }

    @GetMapping("/search")
    public ResponseEntity<List<CatalogueResponseDto>> searchCatalogues(@RequestParam String name) {
        // Searching catalogues with keywords
        List<CatalogueResponseDto> catalogues = catalogueService.searchByName(name);
        return ResponseEntity.ok(catalogues);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CatalogueResponseDto> updateCatalogue(
            @PathVariable Integer id,
            @RequestBody @Valid CatalogueRequestDto dto) {
        String role = SecurityContextUtils.getCurrentRole();
        if (role != null && "SELLER".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sellers cannot update catalogues");
        }
        return ResponseEntity.ok(catalogueService.updateCatalogue(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCatalogue(@PathVariable Integer id) {
        String role = SecurityContextUtils.getCurrentRole();
        if (role != null && "SELLER".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sellers cannot delete catalogues");
        }
        catalogueService.deleteCatalogue(id);
        return ResponseEntity.noContent().build();
    }
}