package com.webstore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webstore.dto.request.SellerRequestDto;
import com.webstore.dto.response.SellerResponseDto;
import com.webstore.entity.Seller.SellerStatus;
import com.webstore.service.SellerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller Unit Tests for SellerController
 * 
 * What are controller tests?
 * - Tests for HTTP endpoints (REST API)
 * - Verifies correct HTTP status codes, JSON responses
 * - Tests request/response handling
 * - Does NOT test business logic (that's in service tests)
 * 
 * Testing Framework Used:
 * @WebMvcTest - Loads only the web layer (controllers)
 * MockMvc - Simulates HTTP requests without starting full server
 * @MockBean - Creates mock service (doesn't use real service)
 * ObjectMapper - Converts objects to/from JSON
 * 
 * Benefits:
 * - Fast (no database, no real HTTP server)
 * - Isolated (only tests controller layer)
 * - Comprehensive (tests all HTTP scenarios)
 */
@WebMvcTest(SellerController.class)
@DisplayName("Seller Controller Tests")
class SellerControllerTest {

    /**
     * MockMvc - Simulates HTTP requests
     * Can send GET, POST, PUT, DELETE requests
     * Verifies responses without starting a real server
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * ObjectMapper - Converts Java objects to JSON and vice versa
     * Needed to create JSON request bodies
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * @MockBean - Creates a MOCK service
     * Controller calls this mock instead of real service
     * We control what it returns using when().thenReturn()
     */
    @MockBean
    private SellerService sellerService;

    // Test data
    private SellerRequestDto requestDto;
    private SellerResponseDto responseDto;

    /**
     * Setup - runs before each test
     * Creates test data
     */
    @BeforeEach
    void setUp() {
        // Request DTO (what frontend sends)
        requestDto = new SellerRequestDto();
        requestDto.setName("John Doe");
        requestDto.setEmail("john@example.com");
        requestDto.setStatus(SellerStatus.ACTIVE);
        requestDto.setJoiningDate(LocalDate.of(2024, 1, 15));

        // Response DTO (what backend returns)
        responseDto = new SellerResponseDto();
        responseDto.setSellerId(1);
        responseDto.setName("John Doe");
        responseDto.setEmail("john@example.com");
        responseDto.setStatus(SellerStatus.ACTIVE);
        responseDto.setJoiningDate(LocalDate.of(2024, 1, 15));
        responseDto.setCreatedAt(LocalDateTime.now());
        responseDto.setCreatedBy("admin");
        responseDto.setUpdatedAt(LocalDateTime.now());
        responseDto.setUpdatedBy("admin");
    }

    /**
     * TEST: Create Seller - Success
     * 
     * HTTP Request:
     * POST /api/sellers
     * Body: {"name":"John Doe","email":"john@example.com",...}
     * 
     * Expected Response:
     * Status: 201 CREATED
     * Body: {"sellerId":1,"name":"John Doe",...}
     */
    @Test
    @DisplayName("POST /api/sellers - Should create seller and return 201")
    void givenValidRequest_whenCreateSeller_thenReturns201() throws Exception {
        // ARRANGE
        // Mock service to return created seller
        when(sellerService.createSeller(any(SellerRequestDto.class)))
                .thenReturn(responseDto);

        // ACT & ASSERT
        mockMvc.perform(post("/api/sellers")  // HTTP POST to /api/sellers
                        .contentType(MediaType.APPLICATION_JSON)  // Content type is JSON
                        .content(objectMapper.writeValueAsString(requestDto)))  // Request body as JSON
                .andExpect(status().isCreated())  // Expect HTTP 201 CREATED
                .andExpect(jsonPath("$.sellerId").value(1))  // Verify JSON field sellerId = 1
                .andExpect(jsonPath("$.name").value("John Doe"))  // Verify name
                .andExpect(jsonPath("$.email").value("john@example.com"))  // Verify email
                .andExpect(jsonPath("$.status").value("ACTIVE"));  // Verify status

        // VERIFY
        verify(sellerService).createSeller(any(SellerRequestDto.class));
    }

    /**
     * TEST: Create Seller - Validation Error
     * 
     * HTTP Request:
     * POST /api/sellers
     * Body: {"name":"","email":"invalid",...}  // Invalid data
     * 
     * Expected Response:
     * Status: 400 BAD REQUEST
     */
    @Test
    @DisplayName("POST /api/sellers - Should return 400 when validation fails")
    void givenInvalidRequest_whenCreateSeller_thenReturns400() throws Exception {
        // ARRANGE
        // Create invalid request (empty name)
        SellerRequestDto invalidDto = new SellerRequestDto();
        invalidDto.setName("");  // Invalid: name is blank
        invalidDto.setEmail("invalid-email");  // Invalid: not a valid email
        invalidDto.setJoiningDate(LocalDate.of(2024, 1, 15));

        // ACT & ASSERT
        mockMvc.perform(post("/api/sellers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());  // Expect HTTP 400 BAD REQUEST

        // VERIFY
        // Service should never be called because validation failed
        verify(sellerService, never()).createSeller(any(SellerRequestDto.class));
    }

    /**
     * TEST: Get All Sellers - Success
     * 
     * HTTP Request:
     * GET /api/sellers
     * 
     * Expected Response:
     * Status: 200 OK
     * Body: [{"sellerId":1,...},{"sellerId":2,...}]
     */
    @Test
    @DisplayName("GET /api/sellers - Should return all sellers with 200")
    void whenGetAllSellers_thenReturns200() throws Exception {
        // ARRANGE
        // Create second seller
        SellerResponseDto responseDto2 = new SellerResponseDto();
        responseDto2.setSellerId(2);
        responseDto2.setName("Jane Smith");
        responseDto2.setEmail("jane@example.com");
        responseDto2.setStatus(SellerStatus.ACTIVE);
        responseDto2.setJoiningDate(LocalDate.of(2024, 2, 20));

        // Mock service to return list of 2 sellers
        List<SellerResponseDto> sellers = Arrays.asList(responseDto, responseDto2);
        when(sellerService.getAllSellers(0,Integer.MAX_VALUE)).thenReturn(sellers);

        // ACT & ASSERT
        mockMvc.perform(get("/api/sellers")  // HTTP GET to /api/sellers
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())  // Expect HTTP 200 OK
                .andExpect(jsonPath("$", hasSize(2)))  // Verify array has 2 items
                .andExpect(jsonPath("$[0].sellerId").value(1))  // First seller ID = 1
                .andExpect(jsonPath("$[0].name").value("John Doe"))  // First seller name
                .andExpect(jsonPath("$[1].sellerId").value(2))  // Second seller ID = 2
                .andExpect(jsonPath("$[1].name").value("Jane Smith"));  // Second seller name

        // VERIFY
        verify(sellerService).getAllSellers(0,Integer.MAX_VALUE);
    }

    /**
     * TEST: Get All Sellers - Empty List
     * 
     * HTTP Request:
     * GET /api/sellers
     * 
     * Expected Response:
     * Status: 200 OK
     * Body: []
     */
    @Test
    @DisplayName("GET /api/sellers - Should return empty array with 200 when no sellers exist")
    void whenGetAllSellers_withNoSellers_thenReturnsEmptyArray() throws Exception {
        // ARRANGE
        when(sellerService.getAllSellers(0,Integer.MAX_VALUE)).thenReturn(List.of());

        // ACT & ASSERT
        mockMvc.perform(get("/api/sellers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())  // Still 200 OK
                .andExpect(jsonPath("$", hasSize(0)));  // Empty array

        // VERIFY
        verify(sellerService).getAllSellers(0,Integer.MAX_VALUE);
    }

    /**
     * TEST: Get Seller By ID - Success
     * 
     * HTTP Request:
     * GET /api/sellers/1
     * 
     * Expected Response:
     * Status: 200 OK
     * Body: {"sellerId":1,"name":"John Doe",...}
     */
    @Test
    @DisplayName("GET /api/sellers/{id} - Should return seller with 200")
    void givenValidId_whenGetSellerById_thenReturns200() throws Exception {
        // ARRANGE
        when(sellerService.getSellerById(1)).thenReturn(responseDto);

        // ACT & ASSERT
        mockMvc.perform(get("/api/sellers/1")  // HTTP GET to /api/sellers/1
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())  // Expect HTTP 200 OK
                .andExpect(jsonPath("$.sellerId").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"));

        // VERIFY
        verify(sellerService).getSellerById(1);
    }

    /**
     * TEST: Get Seller By ID - Not Found
     * 
     * HTTP Request:
     * GET /api/sellers/999
     * 
     * Expected Response:
     * Status: 404 NOT FOUND
     */
    @Test
    @DisplayName("GET /api/sellers/{id} - Should return 404 when seller not found")
    void givenInvalidId_whenGetSellerById_thenReturns404() throws Exception {
        // ARRANGE
        // Mock service to throw exception
        when(sellerService.getSellerById(999))
                .thenThrow(new ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        "Seller not found"));

        // ACT & ASSERT
        mockMvc.perform(get("/api/sellers/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());  // Expect HTTP 404 NOT FOUND

        // VERIFY
        verify(sellerService).getSellerById(999);
    }

    /**
     * TEST: Update Seller - Success
     * 
     * HTTP Request:
     * PUT /api/sellers/1
     * Body: {"name":"John Doe Updated",...}
     * 
     * Expected Response:
     * Status: 200 OK
     * Body: Updated seller
     */
    @Test
    @DisplayName("PUT /api/sellers/{id} - Should update seller and return 200")
    void givenValidIdAndRequest_whenUpdateSeller_thenReturns200() throws Exception {
        // ARRANGE
        responseDto.setName("John Doe Updated");
        when(sellerService.updateSeller(eq(1), any(SellerRequestDto.class)))
                .thenReturn(responseDto);

        // ACT & ASSERT
        mockMvc.perform(put("/api/sellers/1")  // HTTP PUT to /api/sellers/1
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())  // Expect HTTP 200 OK
                .andExpect(jsonPath("$.sellerId").value(1))
                .andExpect(jsonPath("$.name").value("John Doe Updated"));

        // VERIFY
        verify(sellerService).updateSeller(eq(1), any(SellerRequestDto.class));
    }

    /**
     * TEST: Update Seller - Not Found
     * 
     * HTTP Request:
     * PUT /api/sellers/999
     * Body: {...}
     * 
     * Expected Response:
     * Status: 404 NOT FOUND
     */
    @Test
    @DisplayName("PUT /api/sellers/{id} - Should return 404 when seller not found")
    void givenInvalidId_whenUpdateSeller_thenReturns404() throws Exception {
        // ARRANGE
        when(sellerService.updateSeller(eq(999), any(SellerRequestDto.class)))
                .thenThrow(new ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        "Seller not found"));

        // ACT & ASSERT
        mockMvc.perform(put("/api/sellers/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());

        // VERIFY
        verify(sellerService).updateSeller(eq(999), any(SellerRequestDto.class));
    }

    /**
     * TEST: Delete Seller - Success
     * 
     * HTTP Request:
     * DELETE /api/sellers/1
     * 
     * Expected Response:
     * Status: 204 NO CONTENT
     */
    @Test
    @DisplayName("DELETE /api/sellers/{id} - Should delete seller and return 204")
    void givenValidId_whenDeleteSeller_thenReturns204() throws Exception {
        // ARRANGE
        doNothing().when(sellerService).deleteSeller(1);

        // ACT & ASSERT
        mockMvc.perform(delete("/api/sellers/1")  // HTTP DELETE to /api/sellers/1
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());  // Expect HTTP 204 NO CONTENT

        // VERIFY
        verify(sellerService).deleteSeller(1);
    }

    /**
     * TEST: Delete Seller - Not Found
     * 
     * HTTP Request:
     * DELETE /api/sellers/999
     * 
     * Expected Response:
     * Status: 404 NOT FOUND
     */
    @Test
    @DisplayName("DELETE /api/sellers/{id} - Should return 404 when seller not found")
    void givenInvalidId_whenDeleteSeller_thenReturns404() throws Exception {
        // ARRANGE
        doThrow(new ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND,
                "Seller not found"))
                .when(sellerService).deleteSeller(999);

        // ACT & ASSERT
        mockMvc.perform(delete("/api/sellers/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // VERIFY
        verify(sellerService).deleteSeller(999);
    }

    /**
     * TEST: Search Sellers
     * 
     * HTTP Request:
     * GET /api/sellers/search?keyword=john
     * 
     * Expected Response:
     * Status: 200 OK
     * Body: Array of matching sellers
     */
    @Test
    @DisplayName("GET /api/sellers/search - Should return matching sellers with 200")
    void givenKeyword_whenSearchSellers_thenReturns200() throws Exception {
        // ARRANGE
        when(sellerService.searchSellers("john")).thenReturn(List.of(responseDto));

        // ACT & ASSERT
        mockMvc.perform(get("/api/sellers/search")
                        .param("keyword", "john")  // Query parameter
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("John Doe"));

        // VERIFY
        verify(sellerService).searchSellers("john");
    }

    /**
     * TEST: Get Sellers By Status
     * 
     * HTTP Request:
     * GET /api/sellers/status/ACTIVE
     * 
     * Expected Response:
     * Status: 200 OK
     * Body: Array of ACTIVE sellers
     */
    @Test
    @DisplayName("GET /api/sellers/status/{status} - Should return sellers by status with 200")
    void givenStatus_whenGetSellersByStatus_thenReturns200() throws Exception {
        // ARRANGE
        when(sellerService.getSellersByStatus(SellerStatus.ACTIVE))
                .thenReturn(List.of(responseDto));

        // ACT & ASSERT
        mockMvc.perform(get("/api/sellers/status/ACTIVE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        // VERIFY
        verify(sellerService).getSellersByStatus(SellerStatus.ACTIVE);
    }

    /**
     * TEST: Count Sellers By Status
     * 
     * HTTP Request:
     * GET /api/sellers/count/ACTIVE
     * 
     * Expected Response:
     * Status: 200 OK
     * Body: 5
     */
    @Test
    @DisplayName("GET /api/sellers/count/{status} - Should return count with 200")
    void givenStatus_whenCountSellersByStatus_thenReturns200() throws Exception {
        // ARRANGE
        when(sellerService.countSellersByStatus(SellerStatus.ACTIVE)).thenReturn(5L);

        // ACT & ASSERT
        mockMvc.perform(get("/api/sellers/count/ACTIVE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));  // Response body is just the number 5

        // VERIFY
        verify(sellerService).countSellersByStatus(SellerStatus.ACTIVE);
    }
}

