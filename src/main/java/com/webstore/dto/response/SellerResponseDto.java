package com.webstore.dto.response;

import com.webstore.entity.Seller.SellerStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerResponseDto {

    private Integer sellerId;
    private String name;
    private String email;
    private SellerStatus status;
    private LocalDate joiningDate;
    //Audit - fileds inherited from BasicEntities - shows who created and updated the record
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    
}
