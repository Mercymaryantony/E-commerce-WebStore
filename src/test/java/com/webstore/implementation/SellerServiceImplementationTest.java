package com.webstore.implementation;

import com.webstore.dto.request.SellerRequestDto;
import com.webstore.dto.response.SellerResponseDto;
import com.webstore.entity.Seller;
import com.webstore.entity.Seller.SellerStatus;
import com.webstore.repository.SellerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for SellerServiceImplementation
 * 
 * What are unit tests?
 * - Tests for individual methods/units of code
 * - Run fast (no database, no network)
 * - Verify business logic works correctly
 * 
 * Testing Framework Used:
 * - JUnit 5: Test framework
 * - Mockito: Creates fake (mock) objects
 * - AssertJ: Readable assertions
 * 
 * Annotations Explained:
 * @ExtendWith(MockitoExtension.class) - Enables Mockito in JUnit 5
 * @Mock - Creates a fake object (doesn't use real database)
 * @InjectMocks - Creates real object and injects mocks into it
 * @BeforeEach - Runs before each test method
 * @Test - Marks a method as a test
 * @DisplayName - Human-readable test description
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Seller Service Implementation Tests")
class SellerServiceImplementationTest {

    /**
     * @Mock - Creates a FAKE repository
     * We don't want to use a real database in unit tests
     * Instead, we tell it what to return using when().thenReturn()
     */
    @Mock
    private SellerRepository sellerRepository;

    /**
     * @InjectMocks - Creates REAL service and injects mocked repository
     * This is what we're actually testing
     */
    @InjectMocks
    private SellerServiceImplementation sellerService;

    // Test data - reused across multiple tests
    private SellerRequestDto requestDto;
    private Seller seller;

    /**
     * Setup method - runs before each test
     * Creates test data that we can use in every test
     * 
     * Why @BeforeEach?
     * - Ensures each test starts with fresh data
     * - Prevents tests from affecting each other
     */
    @BeforeEach
    void setUp() {
        // Create a request DTO (what frontend sends)
        requestDto = new SellerRequestDto();
        requestDto.setName("John Doe");
        requestDto.setEmail("john@example.com");
        requestDto.setStatus(SellerStatus.ACTIVE);
        requestDto.setJoiningDate(LocalDate.of(2024, 1, 15));

        // Create a seller entity (what database returns)
        seller = new Seller();
        seller.setSellerId(1);
        seller.setName("John Doe");
        seller.setEmail("john@example.com");
        seller.setStatus(SellerStatus.ACTIVE);
        seller.setJoiningDate(LocalDate.of(2024, 1, 15));
        seller.setCreatedAt(LocalDateTime.now());
        seller.setCreatedBy("admin");
        seller.setUpdatedAt(LocalDateTime.now());
        seller.setUpdatedBy("admin");
    }

    /**
     * TEST: Create Seller - Happy Path
     * 
     * Test Scenario:
     * - Email doesn't exist
     * - All validation passes
     * - Seller is created successfully
     * 
     * Test Structure (AAA Pattern):
     * 1. Arrange: Set up test data and mocks
     * 2. Act: Call the method being tested
     * 3. Assert: Verify the results
     */
    @Test
    @DisplayName("Should create seller when email doesn't exist")
    void givenValidRequest_whenCreateSeller_thenReturnsSavedSeller() {
        // ARRANGE - Set up test conditions
        // Tell mock what to return when methods are called
        when(sellerRepository.existsByEmail(requestDto.getEmail())).thenReturn(false);
        when(sellerRepository.save(any(Seller.class))).thenReturn(seller);

        // ACT - Call the method we're testing
        SellerResponseDto result = sellerService.createSeller(requestDto);

        // ASSERT - Verify the results
        assertThat(result).isNotNull();  // Response should not be null
        assertThat(result.getSellerId()).isEqualTo(1);  // ID should be 1
        assertThat(result.getName()).isEqualTo("John Doe");  // Name should match
        assertThat(result.getEmail()).isEqualTo("john@example.com");  // Email should match
        assertThat(result.getStatus()).isEqualTo(SellerStatus.ACTIVE);  // Status should be ACTIVE

        // VERIFY - Ensure repository methods were called
        verify(sellerRepository).existsByEmail(requestDto.getEmail());  // Was email checked?
        verify(sellerRepository).save(any(Seller.class));  // Was seller saved?
    }

    /**
     * TEST: Create Seller - Email Already Exists
     * 
     * Test Scenario:
     * - Email already exists in database
     * - Should throw ResponseStatusException with BAD_REQUEST
     * 
     * This tests the business rule: "Emails must be unique"
     */
    @Test
    @DisplayName("Should throw exception when email already exists")
    void givenExistingEmail_whenCreateSeller_thenThrowsException() {
        // ARRANGE
        // Simulate email already exists
        when(sellerRepository.existsByEmail(requestDto.getEmail())).thenReturn(true);

        // ACT & ASSERT
        // Verify that calling createSeller throws the right exception
        assertThatThrownBy(() -> sellerService.createSeller(requestDto))
                .isInstanceOf(ResponseStatusException.class)  // Should throw this exception
                .hasMessageContaining("Email already exists");  // With this message

        // VERIFY
        // Save should never be called because validation failed
        verify(sellerRepository, never()).save(any(Seller.class));
    }

    /**
     * TEST: Get All Sellers
     * 
     * Test Scenario:
     * - Database has multiple sellers
     * - Should return list of all sellers as DTOs
     */
    @Test
    @DisplayName("Should return all sellers")
    void whenGetAllSellers_thenReturnsSellerList() {
        // ARRANGE
        // Create a second seller for testing
        Seller seller2 = new Seller();
        seller2.setSellerId(2);
        seller2.setName("Jane Smith");
        seller2.setEmail("jane@example.com");
        seller2.setStatus(SellerStatus.ACTIVE);
        seller2.setJoiningDate(LocalDate.of(2024, 2, 20));

        // Mock repository to return list of 2 sellers
        when(sellerRepository.findAll()).thenReturn(Arrays.asList(seller, seller2));

        // ACT
        List<SellerResponseDto> result = sellerService.getAllSellers();

        // ASSERT
        assertThat(result).hasSize(2);  // Should have 2 sellers
        assertThat(result.get(0).getName()).isEqualTo("John Doe");  // First seller name
        assertThat(result.get(1).getName()).isEqualTo("Jane Smith");  // Second seller name

        // VERIFY
        verify(sellerRepository).findAll();
    }

    /**
     * TEST: Get Seller By ID - Found
     * 
     * Test Scenario:
     * - Seller exists with given ID
     * - Should return seller details
     */
    @Test
    @DisplayName("Should return seller when ID exists")
    void givenValidId_whenGetSellerById_thenReturnsSeller() {
        // ARRANGE
        // Mock repository to return seller when findById(1) is called
        when(sellerRepository.findById(1)).thenReturn(Optional.of(seller));

        // ACT
        SellerResponseDto result = sellerService.getSellerById(1);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getSellerId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("John Doe");

        // VERIFY
        verify(sellerRepository).findById(1);
    }

    /**
     * TEST: Get Seller By ID - Not Found
     * 
     * Test Scenario:
     * - Seller doesn't exist with given ID
     * - Should throw ResponseStatusException with NOT_FOUND
     */
    @Test
    @DisplayName("Should throw exception when seller ID not found")
    void givenInvalidId_whenGetSellerById_thenThrowsException() {
        // ARRANGE
        // Mock repository to return empty Optional (seller not found)
        when(sellerRepository.findById(999)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThatThrownBy(() -> sellerService.getSellerById(999))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Seller not found");

        // VERIFY
        verify(sellerRepository).findById(999);
    }

    /**
     * TEST: Update Seller - Success
     * 
     * Test Scenario:
     * - Seller exists
     * - New email doesn't conflict
     * - Should update and return updated seller
     */
    @Test
    @DisplayName("Should update seller when ID exists and email is unique")
    void givenValidIdAndRequest_whenUpdateSeller_thenReturnsUpdatedSeller() {
        // ARRANGE
        // Create updated request with different email
        SellerRequestDto updateDto = new SellerRequestDto();
        updateDto.setName("John Doe Updated");
        updateDto.setEmail("john.new@example.com");
        updateDto.setStatus(SellerStatus.INACTIVE);
        updateDto.setJoiningDate(LocalDate.of(2024, 1, 15));

        // Mock repository responses
        when(sellerRepository.findById(1)).thenReturn(Optional.of(seller));
        when(sellerRepository.existsByEmail("john.new@example.com")).thenReturn(false);
        when(sellerRepository.save(any(Seller.class))).thenReturn(seller);

        // ACT
        SellerResponseDto result = sellerService.updateSeller(1, updateDto);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getSellerId()).isEqualTo(1);

        // VERIFY
        verify(sellerRepository).findById(1);
        verify(sellerRepository).save(any(Seller.class));
    }

    /**
     * TEST: Update Seller - Email Conflict
     * 
     * Test Scenario:
     * - Seller exists
     * - New email already used by another seller
     * - Should throw exception
     */
    @Test
    @DisplayName("Should throw exception when updating to existing email")
    void givenExistingEmail_whenUpdateSeller_thenThrowsException() {
        // ARRANGE
        SellerRequestDto updateDto = new SellerRequestDto();
        updateDto.setName("John Doe");
        updateDto.setEmail("jane@example.com");  // Email already used by another seller
        updateDto.setStatus(SellerStatus.ACTIVE);
        updateDto.setJoiningDate(LocalDate.of(2024, 1, 15));

        when(sellerRepository.findById(1)).thenReturn(Optional.of(seller));
        when(sellerRepository.existsByEmail("jane@example.com")).thenReturn(true);

        // ACT & ASSERT
        assertThatThrownBy(() -> sellerService.updateSeller(1, updateDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Email already exists");

        // VERIFY
        verify(sellerRepository, never()).save(any(Seller.class));
    }

    /**
     * TEST: Delete Seller - Success
     * 
     * Test Scenario:
     * - Seller exists
     * - Should delete successfully
     */
    @Test
    @DisplayName("Should delete seller when ID exists")
    void givenValidId_whenDeleteSeller_thenDeletesSuccessfully() {
        // ARRANGE
        when(sellerRepository.existsById(1)).thenReturn(true);
        doNothing().when(sellerRepository).deleteById(1);

        // ACT
        sellerService.deleteSeller(1);

        // VERIFY
        verify(sellerRepository).existsById(1);
        verify(sellerRepository).deleteById(1);
    }

    /**
     * TEST: Delete Seller - Not Found
     * 
     * Test Scenario:
     * - Seller doesn't exist
     * - Should throw exception
     */
    @Test
    @DisplayName("Should throw exception when deleting non-existent seller")
    void givenInvalidId_whenDeleteSeller_thenThrowsException() {
        // ARRANGE
        when(sellerRepository.existsById(999)).thenReturn(false);

        // ACT & ASSERT
        assertThatThrownBy(() -> sellerService.deleteSeller(999))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Seller not found");

        // VERIFY
        verify(sellerRepository, never()).deleteById(anyInt());
    }

    /**
     * TEST: Search Sellers
     * 
     * Test Scenario:
     * - Search with keyword "john"
     * - Should return matching sellers
     */
    @Test
    @DisplayName("Should return sellers matching search keyword")
    void givenKeyword_whenSearchSellers_thenReturnsMatchingSellers() {
        // ARRANGE
        when(sellerRepository.searchSellers("john")).thenReturn(List.of(seller));

        // ACT
        List<SellerResponseDto> result = sellerService.searchSellers("john");

        // ASSERT
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("John Doe");

        // VERIFY
        verify(sellerRepository).searchSellers("john");
    }

    /**
     * TEST: Get Sellers By Status
     * 
     * Test Scenario:
     * - Get all ACTIVE sellers
     * - Should return only ACTIVE sellers
     */
    @Test
    @DisplayName("Should return sellers with specified status")
    void givenStatus_whenGetSellersByStatus_thenReturnsMatchingSellers() {
        // ARRANGE
        when(sellerRepository.findByStatus(SellerStatus.ACTIVE))
                .thenReturn(List.of(seller));

        // ACT
        List<SellerResponseDto> result = sellerService.getSellersByStatus(SellerStatus.ACTIVE);

        // ASSERT
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(SellerStatus.ACTIVE);

        // VERIFY
        verify(sellerRepository).findByStatus(SellerStatus.ACTIVE);
    }

    /**
     * TEST: Count Sellers By Status
     * 
     * Test Scenario:
     * - Count ACTIVE sellers
     * - Should return correct count
     */
    @Test
    @DisplayName("Should return count of sellers with specified status")
    void givenStatus_whenCountSellersByStatus_thenReturnsCount() {
        // ARRANGE
        when(sellerRepository.countByStatus(SellerStatus.ACTIVE)).thenReturn(5L);

        // ACT
        long count = sellerService.countSellersByStatus(SellerStatus.ACTIVE);

        // ASSERT
        assertThat(count).isEqualTo(5L);

        // VERIFY
        verify(sellerRepository).countByStatus(SellerStatus.ACTIVE);
    }
}

