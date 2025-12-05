package com.webstore.controller;

import com.webstore.dto.request.SellerRequestDto;
import com.webstore.dto.response.SellerResponseDto;
import com.webstore.entity.Seller.SellerStatus;
import com.webstore.service.SellerService;
import com.webstore.validation.SellerValidation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.webstore.dto.response.SellerDetailsResponseDto;
import java.time.LocalDate;
import java.util.List;

/*This controller handles all HTTP requests related to sellers.
 * It's the entry point for the frontend admin dashboard.*/
@Slf4j
@RestController
@RequestMapping("/api/sellers")
public class SellerController {

    /* Seller Service - handles business logic */
    private final SellerService sellerService;

    @Autowired
    public SellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    /*
     * CREATE NEW SELLER
     * Endpoint: POST /api/sellers
     * Request Body (JSON):
     * {
     * "name": "John Doe",
     * "email": "john@example.com",
     * "status": "ACTIVE",
     * "joiningDate": "2024-01-15"
     * }
     */
    @PostMapping
    public ResponseEntity<SellerResponseDto> createSeller(
            @Validated(SellerValidation.class) @RequestBody SellerRequestDto requestDto) {

        log.info("POST /api/sellers - Creating new seller: {}", requestDto.getName());

        // Call service to create seller
        SellerResponseDto createdSeller = sellerService.createSeller(requestDto);

        // Return 201 CREATED with the created seller
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdSeller);
    }

    /*
     * GET ALL SELLERS
     * Endpoint: GET /api/sellers
     */
    @GetMapping
    public ResponseEntity<List<SellerResponseDto>> getAllSellers(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        log.info("GET /api/sellers - Fetching sellers");

        List<SellerResponseDto> sellers;

        // If pagination parameters are provided, use pagination
        if (page != null && size != null) {
            sellers = sellerService.getAllSellers(page, size);
        } else {
            // If no pagination parameters, return all sellers
            sellers = sellerService.getAllSellers(0, Integer.MAX_VALUE);
        }

        return ResponseEntity.ok(sellers);
    }

    /*
     * GET SELLER BY ID
     * GET /api/sellers/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<SellerResponseDto> getSellerById(@PathVariable Integer id) {
        log.info("GET /api/sellers/{} - Fetching seller", id);

        SellerResponseDto seller = sellerService.getSellerById(id);

        return ResponseEntity.ok(seller);
    }

    /*
     * UPDATE SELLER
     * Endpoint: PUT /api/sellers/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<SellerResponseDto> updateSeller(
            @PathVariable Integer id,
            @Validated(SellerValidation.class) @RequestBody SellerRequestDto requestDto) {

        log.info("PUT /api/sellers/{} - Updating seller", id);

        SellerResponseDto updatedSeller = sellerService.updateSeller(id, requestDto);

        return ResponseEntity.ok(updatedSeller);
    }

    /*
     * DELETE SELLER
     * Endpoint: DELETE /api/sellers/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSeller(@PathVariable Integer id) {
        log.info("DELETE /api/sellers/{} - Deleting seller", id);

        sellerService.deleteSeller(id);

        // Return 204 NO_CONTENT (success, no response body needed)
        return ResponseEntity.noContent().build();
    }

    /*
     * SEARCH SELLERS
     * Endpoint: GET /api/sellers/search?keyword=john
     */
    @GetMapping("/search")
    public ResponseEntity<List<SellerResponseDto>> searchSellers(
            @RequestParam String keyword) {

        log.info("GET /api/sellers/search?keyword={}", keyword);

        List<SellerResponseDto> sellers = sellerService.searchSellers(keyword);

        return ResponseEntity.ok(sellers);
    }

    /*
     * GET SELLERS BY STATUS
     * Endpoint: GET /api/sellers/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<SellerResponseDto>> getSellersByStatus(
            @PathVariable SellerStatus status) {

        log.info("GET /api/sellers/status/{}", status);

        List<SellerResponseDto> sellers = sellerService.getSellersByStatus(status);

        return ResponseEntity.ok(sellers);
    }

    /*
     * GET SELLERS JOINED AFTER DATE
     * Endpoint: GET /api/sellers/joined-after?date=2024-01-01
     */
    @GetMapping("/joined-after")
    public ResponseEntity<List<SellerResponseDto>> getSellersJoinedAfter(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        log.info("GET /api/sellers/joined-after?date={}", date);

        List<SellerResponseDto> sellers = sellerService.getSellersJoinedAfter(date);

        return ResponseEntity.ok(sellers);
    }

    /*
     * GET SELLERS JOINED BETWEEN DATES
     * Endpoint: GET
     * /api/sellers/joined-between?startDate=2024-01-01&endDate=2024-12-31
     */
    @GetMapping("/joined-between")
    public ResponseEntity<List<SellerResponseDto>> getSellersJoinedBetween(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        log.info("GET /api/sellers/joined-between?startDate={}&endDate={}", startDate, endDate);

        List<SellerResponseDto> sellers = sellerService.getSellersJoinedBetween(startDate, endDate);

        return ResponseEntity.ok(sellers);
    }

    /*
     * COUNT SELLERS BY STATUS
     * Endpoint: GET /api/sellers/count/{status}
     */
    @GetMapping("/count/{status}")
    public ResponseEntity<Long> countSellersByStatus(@PathVariable SellerStatus status) {
        log.info("GET /api/sellers/count/{}", status);

        long count = sellerService.countSellersByStatus(status);

        return ResponseEntity.ok(count);
    }

    /*GET SELLER DETAILS WITH CATALOGUES AND CATEGORIES
     * Endpoint: GET /api/sellers/{id}/details*/
    @GetMapping("/{id}/details")
    public ResponseEntity<SellerDetailsResponseDto> getSellerDetailsWithCataloguesAndCategories(
            @PathVariable Integer id) {

        log.info("GET /api/sellers/{}/details - Fetching seller details with catalogues and categories", id);

        SellerDetailsResponseDto sellerDetails = sellerService.getSellerDetailsWithCataloguesAndCategories(id);

        return ResponseEntity.ok(sellerDetails);
    }
}
