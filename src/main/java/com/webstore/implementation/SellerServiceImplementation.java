package com.webstore.implementation;

import com.webstore.dto.request.SellerRequestDto;
import com.webstore.dto.response.SellerResponseDto;
import com.webstore.entity.Seller;
import com.webstore.entity.Seller.SellerStatus;
import com.webstore.repository.SellerRepository;
import com.webstore.service.SellerService;
import com.webstore.util.AuthUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/*Seller Service Implementation*/
@Slf4j
@Service
public class SellerServiceImplementation implements SellerService {

   
    private final SellerRepository sellerRepository;

    /*Constructor-based dependency injection*/
    @Autowired
    public SellerServiceImplementation(SellerRepository sellerRepository) {
        this.sellerRepository = sellerRepository;
    }

    /*CREATE SELLER*/
    @Override
    @Transactional  // If anything fails, roll back the database changes
    public SellerResponseDto createSeller(SellerRequestDto requestDto) {
        // Log for debugging - helps track what's happening
        log.info("Creating new seller with email: {}", requestDto.getEmail());

        // Email must be unique
        // Check if email already exists in database
        if (sellerRepository.existsByEmail(requestDto.getEmail())) {
            // Throw error with HTTP 400 (Bad Request)
            log.error("Email already exists: {}", requestDto.getEmail());
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, 
                "Email already exists: " + requestDto.getEmail()
            );
        }

        // Create new Seller entity
        Seller seller = new Seller();
        
        // Map data from DTO to Entity
        mapDtoToEntity(requestDto, seller);

        // Set audit fields - who created this seller
        // AuthUtils.getCurrentUsername() gets current logged-in user
        String currentUser = AuthUtils.getCurrentUsername();
        seller.setCreatedBy(currentUser);
        seller.setUpdatedBy(currentUser);

        // Save to database - JPA automatically generates ID
        Seller savedSeller = sellerRepository.save(seller);
        
        log.info("Successfully created seller with ID: {}", savedSeller.getSellerId());

        // Convert entity to response DTO and return
        return convertToResponseDto(savedSeller);
    }

    /*GET ALL SELLERS*/
    @Override
    @Transactional(readOnly = true)
    public List<SellerResponseDto> getAllSellers() {
        log.info("Fetching all sellers");

        // findAll() - Repository method that gets all records
        // stream() - Convert list to stream for processing
        // map() - Transform each Seller to SellerResponseDto
        // collect() - Collect results back into a list
        return sellerRepository.findAll()
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /*GET SELLER BY ID*/
    @Override
    @Transactional(readOnly = true)
    public SellerResponseDto getSellerById(Integer sellerId) {
        log.info("Fetching seller with ID: {}", sellerId);

        // findById returns Optional<Seller>
        // Optional is a container that may or may not have a value
        // orElseThrow() - If seller exists, return it; otherwise throw error
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Seller not found with ID: " + sellerId
                ));

        return convertToResponseDto(seller);
    }

    /*UPDATE SELLER*/
    @Override
    @Transactional
    public SellerResponseDto updateSeller(Integer sellerId, SellerRequestDto requestDto) {
        log.info("Updating seller with ID: {}", sellerId);

        // Find existing seller
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Seller not found with ID: " + sellerId
                ));

        // Check if email is being changed to one that already exists
        // Only check if email is different from current email
        if (!seller.getEmail().equals(requestDto.getEmail()) 
            && sellerRepository.existsByEmail(requestDto.getEmail())) {
            log.error("Email already exists: {}", requestDto.getEmail());
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Email already exists: " + requestDto.getEmail()
            );
        }

        // Update seller fields
        mapDtoToEntity(requestDto, seller);
        
        // Update audit field - who modified it
        seller.setUpdatedBy(AuthUtils.getCurrentUsername());

        // Save updated seller
        Seller updatedSeller = sellerRepository.save(seller);
        
        log.info("Successfully updated seller with ID: {}", updatedSeller.getSellerId());

        return convertToResponseDto(updatedSeller);
    }

    /*DELETE SELLER*/
    @Override
    @Transactional
    public void deleteSeller(Integer sellerId) {
        log.info("Deleting seller with ID: {}", sellerId);

        // Check if seller exists
        if (!sellerRepository.existsById(sellerId)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Seller not found with ID: " + sellerId
            );
        }

        // Delete seller - all associated data is removed
        sellerRepository.deleteById(sellerId);
        
        log.info("Successfully deleted seller with ID: {}", sellerId);
    }

    /*SEARCH SELLERS*/
    @Override
    @Transactional(readOnly = true)
    public List<SellerResponseDto> searchSellers(String keyword) {
        log.info("Searching sellers with keyword: {}", keyword);

        return sellerRepository.searchSellers(keyword)
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /*GET SELLERS BY STATUS*/
    @Override
    @Transactional(readOnly = true)
    public List<SellerResponseDto> getSellersByStatus(SellerStatus status) {
        log.info("Fetching sellers with status: {}", status);

        return sellerRepository.findByStatus(status)
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /*GET SELLERS JOINED AFTER DATE*/
    @Override
    @Transactional(readOnly = true)
    public List<SellerResponseDto> getSellersJoinedAfter(LocalDate date) {
        log.info("Fetching sellers who joined after: {}", date);

        return sellerRepository.findByJoiningDateAfter(date)
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /*GET SELLERS JOINED BETWEEN DATES*/
    @Override
    @Transactional(readOnly = true)
    public List<SellerResponseDto> getSellersJoinedBetween(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching sellers who joined between {} and {}", startDate, endDate);

        return sellerRepository.findByJoiningDateBetween(startDate, endDate)
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /*COUNT SELLERS BY STATUS*/
    @Override
    @Transactional(readOnly = true)
    public long countSellersByStatus(SellerStatus status) {
        log.info("Counting sellers with status: {}", status);
        return sellerRepository.countByStatus(status);
    }

    /* Map Request DTO to EntityThis method is private - only used within this class*/
    private void mapDtoToEntity(SellerRequestDto dto, Seller seller) {
        seller.setName(dto.getName());
        seller.setEmail(dto.getEmail());
        seller.setJoiningDate(dto.getJoiningDate());
        
        // If status is provided, use it; otherwise keep existing/default
        if (dto.getStatus() != null) {
            seller.setStatus(dto.getStatus());
        }
    }

    /*Convert Entity to Response DTO
     *It converts database entity to API response format*/
    private SellerResponseDto convertToResponseDto(Seller seller) {
        SellerResponseDto dto = new SellerResponseDto();
        dto.setSellerId(seller.getSellerId());
        dto.setName(seller.getName());
        dto.setEmail(seller.getEmail());
        dto.setStatus(seller.getStatus());
        dto.setJoiningDate(seller.getJoiningDate());
        
        // Audit fields from BasicEntities
        dto.setCreatedAt(seller.getCreatedAt());
        dto.setCreatedBy(seller.getCreatedBy());
        dto.setUpdatedAt(seller.getUpdatedAt());
        dto.setUpdatedBy(seller.getUpdatedBy());
        
        return dto;
    }
}

