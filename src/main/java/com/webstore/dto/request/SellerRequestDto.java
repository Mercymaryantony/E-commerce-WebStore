package com.webstore.dto.request;

import com.webstore.entity.Seller.SellerStatus;
import com.webstore.validation.SellerValidation;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor//generated no-argument constructor
@AllArgsConstructor//Generated constructor with all fields
public class SellerRequestDto {

    //Validating the seller name
    @NotNull(groups=SellerValidation.class, message="Seller name is required")
    @NotBlank(groups=SellerValidation.class, message="Seller name should not be blank")
    @Size(min=2, max=100, groups=SellerValidation.class, message="Seller name must be between 2 and 100 characters")
    private String name;

    //Validating the seller mail id
    @NotNull(groups = SellerValidation.class, message = "Seller mail id is required")
    @NotBlank(groups = SellerValidation.class, message = "Seller mail id should not be blank")
    @Email(groups = SellerValidation.class, message = "Email should be valid")
    @Size(max = 100, groups = SellerValidation.class, message = "Email must not exceed 100 characters")
    private String email;

    /*Seller status (ACTIVE or INACTIVE)
     * Optional - defaults to ACTIVE in entity if not provided
     */
    private SellerStatus status;

    //Validating the seller joining date
    @NotNull(groups = SellerValidation.class, message = "Joining date is required")
    @PastOrPresent(groups = SellerValidation.class, message = "Joining date cannot be in the future")
    private LocalDate joiningDate;
    
}
