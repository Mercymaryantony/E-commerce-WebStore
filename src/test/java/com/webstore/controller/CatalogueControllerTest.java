package com.webstore.controller;

import com.webstore.dto.request.CatalogueRequestDto;
import com.webstore.dto.response.CatalogueResponseDto;
import com.webstore.service.CatalogueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CatalogueControllerTest {

    @Mock
    private CatalogueService catalogueService;

    private CatalogueController catalogueController;

    private CatalogueRequestDto requestDto;
    private CatalogueResponseDto responseDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create controller and manually inject the mock service
        catalogueController = new CatalogueController();
        catalogueController.setCatalogueService(catalogueService);

        requestDto = new CatalogueRequestDto();
        requestDto.setCatalogueName("Electronics");
        requestDto.setCatalogueDescription("Gadgets & Devices");

        responseDto = new CatalogueResponseDto();
        responseDto.setCatalogueId(1);
        responseDto.setCatalogueName("Electronics");
        responseDto.setCatalogueDescription("Gadgets & Devices");
    }

    @Test
    void testCreateCatalogue() {
        when(catalogueService.createCatalogue(requestDto)).thenReturn(responseDto);
        ResponseEntity<CatalogueResponseDto> response = catalogueController.createCatalogue(requestDto);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
        assertEquals(responseDto, response.getBody());
        verify(catalogueService, times(1)).createCatalogue(requestDto);
    }

    @Test
    void testGetAllCatalogues() {
        when(catalogueService.getAllCatalogues(0, Integer.MAX_VALUE)).thenReturn(Arrays.asList(responseDto));
        ResponseEntity<List<CatalogueResponseDto>> response = catalogueController.getAllCatalogues(null, null);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(catalogueService, times(1)).getAllCatalogues(0, Integer.MAX_VALUE);
    }

    @Test
    void testGetAllCataloguesWithPagination() {
        when(catalogueService.getAllCatalogues(0, 10)).thenReturn(Arrays.asList(responseDto));
        ResponseEntity<List<CatalogueResponseDto>> response = catalogueController.getAllCatalogues(0, 10);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(catalogueService, times(1)).getAllCatalogues(0, 10);
    }

    @Test
    void testGetAllCataloguesEmpty() {
        when(catalogueService.getAllCatalogues(0, Integer.MAX_VALUE)).thenReturn(Arrays.asList());
        ResponseEntity<List<CatalogueResponseDto>> response = catalogueController.getAllCatalogues(null, null);

        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatusCode().value());
        verify(catalogueService, times(1)).getAllCatalogues(0, Integer.MAX_VALUE);
    }

    @Test
    void testSearchCatalogues() {
        when(catalogueService.searchByName("Electronics")).thenReturn(Arrays.asList(responseDto));
        ResponseEntity<List<CatalogueResponseDto>> response = catalogueController.searchCatalogues("Electronics");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(catalogueService, times(1)).searchByName("Electronics");
    }

    @Test
    void testUpdateCatalogue() {
        when(catalogueService.updateCatalogue(1, requestDto)).thenReturn(responseDto);
        ResponseEntity<CatalogueResponseDto> response = catalogueController.updateCatalogue(1, requestDto);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
        assertEquals(responseDto, response.getBody());
        verify(catalogueService, times(1)).updateCatalogue(1, requestDto);
    }

    @Test
    void testDeleteCatalogue() {
        doNothing().when(catalogueService).deleteCatalogue(1);
        ResponseEntity<Void> response = catalogueController.deleteCatalogue(1);

        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatusCode().value());
        verify(catalogueService, times(1)).deleteCatalogue(1);
    }
}