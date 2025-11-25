package com.webstore.entity;

import static com.webstore.constant.DatabaseConstants.SCHEMA_NAME;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "sellers", schema = SCHEMA_NAME)
public class Seller extends BasicEntities {

    /*Primary Key - Auto-generated seller ID
     * Uses PostgreSQL sequence for ID generation
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seller_generator")
    @SequenceGenerator(
            name = "seller_generator",
            sequenceName = SCHEMA_NAME + ".seq_seller_id",
            allocationSize = 1  // Increment by 1 for each new seller
    )
    @Column(name = "seller_id")
    private Integer sellerId;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    
    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    /*Defaults to ACTIVE when a new seller is created
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private SellerStatus status = SellerStatus.ACTIVE;

    // uses LocalDate (date only, no time)
     
    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    
    public enum SellerStatus {
        ACTIVE,
        INACTIVE
    }
}
