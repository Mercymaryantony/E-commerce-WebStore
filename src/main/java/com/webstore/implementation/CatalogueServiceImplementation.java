package com.webstore.implementation;

import com.webstore.constant.UserRole;
import com.webstore.dto.request.CatalogueRequestDto;
import com.webstore.dto.response.CatalogueResponseDto;
import com.webstore.dto.response.CategoryResponseDto;
import com.webstore.entity.Catalogue;
import com.webstore.repository.CatalogueCategoryRepository;
import com.webstore.repository.CatalogueRepository;
import com.webstore.service.CatalogueService;
import com.webstore.service.CategoryService;
import com.webstore.util.AuthUtils;
import com.webstore.util.SecurityContextUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CatalogueServiceImplementation implements CatalogueService {

    private final CatalogueRepository catalogueRepository;

    private final CatalogueCategoryRepository catalogueCategoryRepository;

    private final CategoryService categoryService;

    public CatalogueServiceImplementation(CatalogueRepository catalogueRepository,
            CatalogueCategoryRepository catalogueCategoryRepository,
            CategoryService categoryService) {
        this.catalogueRepository = catalogueRepository;
        this.catalogueCategoryRepository = catalogueCategoryRepository;
        this.categoryService = categoryService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CatalogueResponseDto> getAllCatalogues(int page, int size) {
        // Create Pageable for pagination (used by both seller and admin)
        Pageable pageable = PageRequest.of(page, size);
        Page<Catalogue> cataloguePage;

        String role = SecurityContextUtils.getCurrentRole();

        if (role != null && UserRole.SELLER.equals(role)) {
            // Seller: Get paginated catalogues for this seller
            Integer sellerId = SecurityContextUtils.getCurrentSellerId();
            if (sellerId == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Seller ID not found in token");
            }
            cataloguePage = catalogueRepository.findBySellerId(sellerId, pageable);
        } else {
            // Admin or unauthenticated: Get all paginated catalogues
            cataloguePage = catalogueRepository.findAll(pageable);
        }

        // Convert to DTOs (same for both seller and admin)
        return cataloguePage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CatalogueResponseDto createCatalogue(CatalogueRequestDto dto) {
        Catalogue catalogue = new Catalogue();
        catalogue.setCatalogueName(dto.getCatalogueName());
        catalogue.setCatalogueDescription(dto.getCatalogueDescription());

        String currentUser = AuthUtils.getCurrentUsername();
        catalogue.setCreatedBy(currentUser);
        catalogue.setUpdatedBy(currentUser);

        return convertToDto(catalogueRepository.save(catalogue));
    }

    @Override
    @Transactional(readOnly = true)
    public CatalogueResponseDto getCatalogueById(Integer id) {
        Catalogue catalogue = catalogueRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Catalogue with id " + id + " not found"));
        return convertToDto(catalogue);
    }

    @Override
    @Transactional
    public CatalogueResponseDto updateCatalogue(Integer id, CatalogueRequestDto dto) {
        Catalogue catalogue = catalogueRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Catalogue not found"));

        catalogue.setCatalogueName(dto.getCatalogueName());
        catalogue.setCatalogueDescription(dto.getCatalogueDescription());
        catalogue.setUpdatedBy(AuthUtils.getCurrentUsername());

        return convertToDto(catalogueRepository.save(catalogue));
    }

    @Override
    @Transactional
    public void deleteCatalogue(Integer id) {
        Catalogue catalogue = catalogueRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Catalogue not found"));

        // Check if catalogue has associated categories
        // Force fetch the catalogueCategories (LAZY loading)
        catalogue.getCatalogueCategories().size(); // This triggers the fetch

        if (catalogue.getCatalogueCategories() != null && !catalogue.getCatalogueCategories().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot delete catalogue. Please delete the corresponding categories first, then you can delete the catalogue.");
        }

        // If no categories are associated, proceed with deletion
        catalogueRepository.delete(catalogue);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CatalogueResponseDto> searchByName(String name) {
        // If search term is null or empty, return all catalogues
        if (name == null || name.trim().isEmpty()) {
            return getAllCatalogues(0, Integer.MAX_VALUE);
        }

        // Otherwise, search for matching catalogues
        List<Catalogue> catalogues;
        String role = SecurityContextUtils.getCurrentRole();

        if (role != null && UserRole.SELLER.equals(role)) {
            // For sellers, only search in their own catalogues
            Integer sellerId = SecurityContextUtils.getCurrentSellerId();
            if (sellerId == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Seller ID not found in token");
            }

            // Get all seller's catalogues first, then filter by search term
            List<Catalogue> allSellerCatalogues = catalogueRepository.findBySellerId(sellerId);
            catalogues = allSellerCatalogues.stream()
                    .filter(catalogue -> catalogue.getCatalogueName()
                            .toLowerCase()
                            .contains(name.toLowerCase()))
                    .collect(Collectors.toList());
        } else {
            // For admin or unauthenticated users, search in all catalogues
            catalogues = catalogueRepository.findByCatalogueNameContainingIgnoreCase(name);
        }

        return catalogues.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<CategoryResponseDto> getCategoriesByCatalogueId(Integer catalogueId) {
        // Find the catalogue
        Catalogue catalogue = catalogueRepository.findById(catalogueId)
                .orElseThrow(() -> new RuntimeException("Catalogue not found with ID: " + catalogueId));

        // Get category IDs from catalogue_category mappings
        List<Integer> categoryIds = catalogue.getCatalogueCategories().stream()
                .map(cc -> cc.getCategory().getCategoryId())
                .collect(Collectors.toList());

        // Get detailed category info for each ID
        List<CategoryResponseDto> categories = new ArrayList<>();
        for (Integer categoryId : categoryIds) {
            try {
                CategoryResponseDto category = categoryService.getCategoryById(categoryId);
                categories.add(category);
            } catch (Exception e) {
                // Skip categories that can't be found
            }
        }

        return categories;
    }

    private CatalogueResponseDto convertToDto(Catalogue catalogue) {
        CatalogueResponseDto dto = new CatalogueResponseDto();
        dto.setCatalogueId(catalogue.getCatalogueId());
        dto.setCatalogueName(catalogue.getCatalogueName());
        dto.setCatalogueDescription(catalogue.getCatalogueDescription());
        dto.setCreatedAt(catalogue.getCreatedAt());
        dto.setUpdatedAt(catalogue.getUpdatedAt());
        dto.setCreatedBy(catalogue.getCreatedBy());
        dto.setUpdatedBy(catalogue.getUpdatedBy());
        return dto;
    }
}