package com.webstore.implementation;

import com.webstore.dto.request.ProductRequestDto;
import com.webstore.dto.response.CatalogueCategoryResponseDto;
import com.webstore.dto.response.ProductResponseDto;
import com.webstore.entity.CatalogueCategory;
import com.webstore.entity.Product;
import com.webstore.entity.ProductPrice;
import com.webstore.entity.Seller;
import com.webstore.repository.CatalogueCategoryRepository;
import com.webstore.repository.ProductPriceRepository;
import com.webstore.repository.ProductRepository;
import com.webstore.repository.SellerRepository;
import com.webstore.service.ProductService;
import com.webstore.util.SecurityContextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImplementation implements ProductService {

    private final ProductRepository productRepository;
    private final CatalogueCategoryRepository catalogueCategoryRepository;
    private final SellerRepository sellerRepository;
    private final ProductPriceRepository productPriceRepository;

    @Override
    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto dto) {
        log.info("Creating product with name: {}", dto.getProductName());

        // If seller, ensure they can only create products for themselves
        String role = SecurityContextUtils.getCurrentRole();
        if (role != null && "SELLER".equals(role)) {
            Integer sellerId = SecurityContextUtils.getCurrentSellerId();
            if (sellerId == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Seller ID not found in token");
            }
            // Override sellerId from DTO to ensure seller can only create for themselves
            dto.setSellerId(sellerId);
            log.info("Seller {} is creating product, sellerId set to {}",
                    SecurityContextUtils.getCurrentSellerEmail().orElse("unknown"), sellerId);
        }

        // Validate and find CatalogueCategory
        CatalogueCategory catalogueCategory = catalogueCategoryRepository
                .findByCatalogueCatalogueIdAndCategoryCategoryId(dto.getCatalogueId(), dto.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("CatalogueCategory not found for Catalogue ID: %d and Category ID: %d",
                                dto.getCatalogueId(), dto.getCategoryId())));

        // Validate and find Seller
        Seller seller = sellerRepository.findById(dto.getSellerId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Seller not found with ID: %d", dto.getSellerId())));

        Product product = new Product();
        product.setProductName(dto.getProductName());
        product.setProductDescription(dto.getProductDescription());
        product.setCatalogueCategory(catalogueCategory);
        product.setSeller(seller);
        product.setImageUrl(dto.getImageUrl());
        product.setStock(dto.getStock() != null ? dto.getStock() : 0);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        Product saved = productRepository.save(product);
        log.info("Product created with ID: {}", saved.getProductId());

        return convertToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getAllProducts(int page, int size) {
        log.info("Fetching products - page: {}, size: {}", page, size);

        List<Product> products;
        String role = SecurityContextUtils.getCurrentRole();
        if (role != null && "SELLER".equals(role)) {
            Integer sellerId = SecurityContextUtils.getCurrentSellerId();
            if (sellerId == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Seller ID not found in token");
            }
            // Get all products for this seller
            List<Product> allSellerProducts = productRepository.findAllBySellerId(sellerId);
            // Apply pagination manually
            int start = page * size;
            int end = Math.min(start + size, allSellerProducts.size());
            if (start < allSellerProducts.size()) {
                products = allSellerProducts.subList(Math.max(0, start), end);
            } else {
                products = new ArrayList<>();
            }
            log.info("Loaded {} products for seller {} (page {}, total: {})",
                    products.size(), sellerId, page, allSellerProducts.size());
        } else {
            // Admin or unauthenticated - return all products with pagination
            Pageable pageable = PageRequest.of(page, size);
            Page<Product> productPage = productRepository.findAll(pageable);
            products = productPage.getContent();
            log.info("Loaded {} products for admin/unauthenticated (page {}, total: {})",
                    products.size(), page, productPage.getTotalElements());
        }

        return products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(Integer id) {
        log.info("Fetching product with ID: {}", id);

        Product product = productRepository.findByIdWithSeller(id)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + id));

        // Check if seller is trying to access another seller's product
        String role = SecurityContextUtils.getCurrentRole();
        if (role != null && "SELLER".equals(role)) {
            Integer sellerId = SecurityContextUtils.getCurrentSellerId();
            if (sellerId == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Seller ID not found in token");
            }
            if (!sellerId.equals(product.getSeller().getSellerId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Access denied: Product does not belong to your seller account");
            }
            log.info("Seller {} accessed their product {}", sellerId, id);
        }

        return convertToDto(product);
    }

    @Override
    @Transactional
    public ProductResponseDto updateProduct(Integer id, ProductRequestDto dto) {
        log.info("Updating product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + id));

        // Check if seller is trying to update another seller's product
        String role = SecurityContextUtils.getCurrentRole();
        if (role != null && "SELLER".equals(role)) {
            Integer sellerId = SecurityContextUtils.getCurrentSellerId();
            if (sellerId == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Seller ID not found in token");
            }
            if (!sellerId.equals(product.getSeller().getSellerId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Access denied: Product does not belong to your seller account");
            }
            // Override sellerId to ensure seller can only update their own products
            dto.setSellerId(sellerId);
            log.info("Seller {} is updating their product {}", sellerId, id);
        }

        // Validate and find CatalogueCategory
        CatalogueCategory catalogueCategory = catalogueCategoryRepository
                .findByCatalogueCatalogueIdAndCategoryCategoryId(dto.getCatalogueId(), dto.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("CatalogueCategory not found for Catalogue ID: %d and Category ID: %d",
                                dto.getCatalogueId(), dto.getCategoryId())));

        // Validate and find Seller
        Seller seller = sellerRepository.findById(dto.getSellerId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Seller not found with ID: %d", dto.getSellerId())));

        product.setProductName(dto.getProductName());
        product.setProductDescription(dto.getProductDescription());
        product.setCatalogueCategory(catalogueCategory);
        product.setSeller(seller);
        product.setImageUrl(dto.getImageUrl());
        product.setStock(dto.getStock() != null ? dto.getStock() : 0);
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
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + id));

        // Check if seller is trying to delete another seller's product
        String role = SecurityContextUtils.getCurrentRole();
        if (role != null && "SELLER".equals(role)) {
            Integer sellerId = SecurityContextUtils.getCurrentSellerId();
            if (sellerId == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Seller ID not found in token");
            }
            if (!sellerId.equals(product.getSeller().getSellerId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Access denied: Product does not belong to your seller account");
            }
            log.info("Seller {} is deleting their product {}", sellerId, id);
        }

        productRepository.delete(product);
        log.info("Product with ID: {} has been deleted", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> searchProducts(String searchTerm) {
        log.info("Searching products with term: {}", searchTerm);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllProducts(0, Integer.MAX_VALUE);
        }

        List<Product> products;
        String role = SecurityContextUtils.getCurrentRole();
        if (role != null && "SELLER".equals(role)) {
            Integer sellerId = SecurityContextUtils.getCurrentSellerId();
            if (sellerId == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Seller ID not found in token");
            }
            products = productRepository.searchBySellerIdAndNameOrDescription(sellerId, searchTerm.trim());
            log.info("Found {} products for seller {} matching '{}'", products.size(), sellerId, searchTerm);
        } else {
            products = productRepository.searchByNameOrDescription(searchTerm.trim());
            log.info("Found {} products matching '{}' (admin/unauthenticated)", products.size(), searchTerm);
        }

        return products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ProductResponseDto convertToDto(Product product) {
        ProductResponseDto dto = new ProductResponseDto();
        dto.setProductId(product.getProductId());
        dto.setProductName(product.getProductName());
        dto.setProductDescription(product.getProductDescription());
        dto.setImageUrl(product.getImageUrl());
        dto.setStock(product.getStock());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        dto.setCreatedBy(product.getCreatedBy());
        dto.setUpdatedBy(product.getUpdatedBy());

        // Set seller information - ensure seller_id is always set
        if (product.getSeller() != null) {
            dto.setSellerId(product.getSeller().getSellerId());
        } else {
            // Fallback: fetch seller_id directly from database if seller relationship is
            // not loaded
            log.warn("Seller relationship not loaded for product {}, fetching seller_id directly",
                    product.getProductId());
            Integer sellerId = getSellerIdFromProduct(product.getProductId());
            if (sellerId != null) {
                dto.setSellerId(sellerId);
            } else {
                log.error("Product {} has no seller_id assigned!", product.getProductId());
            }
        }

        // Fetch price information using JPQL query
        Integer productId = product.getProductId();
        log.debug("Fetching price information for productId: {}", productId);

        List<ProductPrice> productPrices = productPriceRepository.findPriceInfoWithDetailsByProductId(productId);
        List<ProductResponseDto.PriceInfoDto> prices = new ArrayList<>();

        if (productPrices != null && !productPrices.isEmpty()) {
            log.debug("Found {} prices for product {}", productPrices.size(), productId);

            for (ProductPrice pp : productPrices) {
                try {
                    ProductResponseDto.PriceInfoDto priceInfo = new ProductResponseDto.PriceInfoDto();
                    priceInfo.setProductId(pp.getProduct().getProductId());
                    priceInfo.setProductName(pp.getProduct().getProductName());
                    priceInfo.setCurrencyId(pp.getCurrency().getCurrencyId());
                    priceInfo.setCurrencyCode(pp.getCurrency().getCurrencyCode());
                    priceInfo.setCurrencySymbol(pp.getCurrency().getCurrencySymbol());
                    priceInfo.setPriceAmount(pp.getPriceAmount());
                    prices.add(priceInfo);

                    log.debug("✓ Added price: Product={}, Currency={}, Amount={}",
                            pp.getProduct().getProductName(),
                            pp.getCurrency().getCurrencyCode(),
                            pp.getPriceAmount());
                } catch (Exception e) {
                    log.error("Error processing price data: {}", e.getMessage(), e);
                }
            }
        } else {
            log.warn("⚠ No prices found for product {}", productId);
        }

        dto.setPrices(prices);
        log.debug("Product {}: prices={}", product.getProductId(), prices.size());

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

    // Helper method to get seller_id directly from database
    private Integer getSellerIdFromProduct(Integer productId) {
        try {
            // Use native query to get seller_id directly
            return productRepository.findSellerIdByProductId(productId);
        } catch (Exception e) {
            log.error("Error fetching seller_id for product {}: {}", productId, e.getMessage());
            return null;
        }
    }
}