package com.webstore.implementation;

import com.webstore.dto.request.ProductRequestDto;
import com.webstore.dto.response.ProductResponseDto;
import com.webstore.entity.Catalogue;
import com.webstore.entity.Category;
import com.webstore.entity.CatalogueCategory;
import com.webstore.entity.Product;
import com.webstore.repository.CatalogueCategoryRepository;
import com.webstore.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceImplementationTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CatalogueCategoryRepository catalogueCategoryRepository;

    @InjectMocks
    private ProductServiceImplementation productService;

    private ProductRequestDto requestDto;
    private Product mockProduct;
    private Category mockCategory;
    private Catalogue mockCatalogue;
    private CatalogueCategory mockCatalogueCategory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        requestDto = new ProductRequestDto();
        requestDto.setProductName("Test Product");
        requestDto.setProductDescription("Test Description");
        requestDto.setCatalogueId(1);
        requestDto.setCategoryId(1);

        mockCategory = new Category();
        mockCategory.setCategoryId(1);
        mockCategory.setCategoryName("Electronics");

        mockCatalogue = new Catalogue();
        mockCatalogue.setCatalogueId(1);
        mockCatalogue.setCatalogueName("Summer Collection");

        mockCatalogueCategory = new CatalogueCategory();
        mockCatalogueCategory.setCatalogueCategoryId(1);
        mockCatalogueCategory.setCatalogue(mockCatalogue);
        mockCatalogueCategory.setCategory(mockCategory);

        mockProduct = new Product();
        mockProduct.setProductId(1);
        mockProduct.setProductName("Test Product");
        mockProduct.setProductDescription("Test Description");
        mockProduct.setCatalogueCategory(mockCatalogueCategory);
    }

    @Test
    void testCreateProduct() {
        when(catalogueCategoryRepository.findByCatalogueCatalogueIdAndCategoryCategoryId(1, 1))
                .thenReturn(Optional.of(mockCatalogueCategory));
        when(productRepository.save(any())).thenReturn(mockProduct);

        ProductResponseDto response = productService.createProduct(requestDto);

        assertNotNull(response);
        assertEquals("Test Product", response.getProductName());
    }

    @Test
    void testGetAllProducts() {
        when(productRepository.findAll()).thenReturn(List.of(mockProduct));

        List<ProductResponseDto> result = productService.getAllProducts();

        assertEquals(1, result.size());
    }

    @Test
    void testGetProductById() {
        when(productRepository.findById(1)).thenReturn(Optional.of(mockProduct));

        ProductResponseDto result = productService.getProductById(1);

        assertEquals("Test Product", result.getProductName());
    }

    @Test
    void testUpdateProduct() {
        when(productRepository.findById(1)).thenReturn(Optional.of(mockProduct));
        when(catalogueCategoryRepository.findByCatalogueCatalogueIdAndCategoryCategoryId(1, 1))
                .thenReturn(Optional.of(mockCatalogueCategory));
        when(productRepository.save(any())).thenReturn(mockProduct);

        ProductResponseDto response = productService.updateProduct(1, requestDto);

        assertNotNull(response);
        assertEquals("Test Product", response.getProductName());
    }

    @Test
    void testDeleteProduct() {
        when(productRepository.findById(1)).thenReturn(Optional.of(mockProduct));

        assertDoesNotThrow(() -> productService.deleteProduct(1));
        verify(productRepository, times(1)).delete(mockProduct);
    }

    @Test
    void testDeleteProduct_NotFound() {
        when(productRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> productService.deleteProduct(1));
    }
}
