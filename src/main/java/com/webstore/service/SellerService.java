package com.webstore.service;

import com.webstore.dto.request.SellerRequestDto;
import com.webstore.dto.response.SellerResponseDto;
import com.webstore.dto.response.SellerDetailsResponseDto;
import com.webstore.entity.Seller.SellerStatus;

import java.time.LocalDate;
import java.util.List;

/*The actual implementation will be in SellerServiceImplementation.java*/
public interface SellerService {

    /* Create a new seller */
    SellerResponseDto createSeller(SellerRequestDto sellerRequestDto);

    /* Get all sellers */
    List<SellerResponseDto> getAllSellers(int page, int size);

    /* Get a single seller by ID */
    SellerResponseDto getSellerById(Integer sellerId);

    /* Update an existing seller */
    SellerResponseDto updateSeller(Integer sellerId, SellerRequestDto sellerRequestDto);

    /* Delete a seller */
    void deleteSeller(Integer sellerId);

    /* Search sellers by keyword */
    List<SellerResponseDto> searchSellers(String keyword);

    /* Get sellers by status */
    List<SellerResponseDto> getSellersByStatus(SellerStatus status);

    /* Get sellers who joined after a specific date */
    List<SellerResponseDto> getSellersJoinedAfter(LocalDate date);

    /* Get sellers who joined in a date range */
    List<SellerResponseDto> getSellersJoinedBetween(LocalDate startDate, LocalDate endDate);

    /* Count sellers by status */
    long countSellersByStatus(SellerStatus status);

    /* Get seller details with catalogues, categories, and product counts */
    SellerDetailsResponseDto getSellerDetailsWithCataloguesAndCategories(Integer sellerId);
}