package com.webstore.implementation;

import com.webstore.dto.request.ProductRequestDto;
import com.webstore.dto.response.CatalogueCategoryResponseDto;
import com.webstore.dto.response.ProductResponseDto;
import com.webstore.entity.CatalogueCategory;
import com.webstore.entity.Product;
import com.webstore.repository.CatalogueCategoryRepository;
import com.webstore.repository.ProductRepository;
import com.webstore.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImplementation implements ProductService {

    private final ProductRepository productRepository;
    private final CatalogueCategoryRepository catalogueCategoryRepository;

    @Override
    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto dto) {
        log.info("Creating product with name: {}", dto.getProductName());

        // Find or validate CatalogueCategory based on catalogueId + categoryId
        CatalogueCategory catalogueCategory = catalogueCategoryRepository
                .findByCatalogueCatalogueIdAndCategoryCategoryId(dto.getCatalogueId(), dto.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("CatalogueCategory not found for Catalogue ID: %d and Category ID: %d. " +
                                "Please create the catalogue-category mapping first.", 
                                dto.getCatalogueId(), dto.getCategoryId())
                ));

        Product product = new Product();
        product.setProductName(dto.getProductName());
        product.setProductDescription(dto.getProductDescription());
        product.setCatalogueCategory(catalogueCategory);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        Product saved = productRepository.save(product);
        log.info("Product created with ID: {}", saved.getProductId());

        return convertToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getAllProducts() {
        log.info("Fetching all products");

        return productRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(Integer id) {
        log.info("Fetching product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + id));

        return convertToDto(product);
    }

    @Override
    @Transactional
    public ProductResponseDto updateProduct(Integer id, ProductRequestDto dto) {
        log.info("Updating product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + id));

        // Find catalogueCategory based on new catalogueId + categoryId
        CatalogueCategory catalogueCategory = catalogueCategoryRepository
                .findByCatalogueCatalogueIdAndCategoryCategoryId(dto.getCatalogueId(), dto.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("CatalogueCategory not found for Catalogue ID: %d and Category ID: %d",
                                dto.getCatalogueId(), dto.getCategoryId())
                ));

        product.setProductName(dto.getProductName());
        product.setProductDescription(dto.getProductDescription());
        product.setCatalogueCategory(catalogueCategory);
        product.setUpdatedAt(LocalDateTime.now());

        Product updated = productRepository.save(product);
        log.info("Product with ID: {} updated successfully", id);

        return convertToDto(updated);
    }

    @Override
    @Transactional
    public void deleteProduct(Integer id) {
        log.info("Deleting product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + id));

        productRepository.delete(product);
        log.info("Product with ID: {} has been deleted", id);
    }

    private ProductResponseDto convertToDto(Product product) {
        ProductResponseDto dto = new ProductResponseDto();
        dto.setProductId(product.getProductId());
        dto.setProductName(product.getProductName());
        dto.setProductDescription(product.getProductDescription());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        dto.setCreatedBy(product.getCreatedBy());
        dto.setUpdatedBy(product.getUpdatedBy());

        // Convert CatalogueCategory to DTO
        if (product.getCatalogueCategory() != null) {
            CatalogueCategory cc = product.getCatalogueCategory();
            CatalogueCategoryResponseDto ccDto = new CatalogueCategoryResponseDto();
            ccDto.setCatalogueCategoryId(cc.getCatalogueCategoryId());
            
            if (cc.getCatalogue() != null) {
                ccDto.setCatalogueId(cc.getCatalogue().getCatalogueId());
                ccDto.setCatalogueName(cc.getCatalogue().getCatalogueName());
            }
            
            if (cc.getCategory() != null) {
                ccDto.setCategoryId(cc.getCategory().getCategoryId());
                ccDto.setCategoryName(cc.getCategory().getCategoryName());
            }
            
            ccDto.setCreatedAt(cc.getCreatedAt());
            ccDto.setCreatedBy(cc.getCreatedBy());
            ccDto.setUpdatedAt(cc.getUpdatedAt());
            ccDto.setUpdatedBy(cc.getUpdatedBy());
            
            dto.setCatalogueCategory(ccDto);
        }

        return dto;
    }
}
