package com.webstore.entity;

import static com.webstore.constant.DatabaseConstants.SCHEMA_NAME;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Seller Entity - Represents a seller/vendor in the system
 * 
 * This entity is used to manage seller information in the admin dashboard.
 * Sellers are standalone and not linked to products (Option 1: Independent Management).
 * 
 * Key Features:
 * - Auto-generated primary key (seller_id)
 * - Unique email constraint
 * - Status tracking (ACTIVE/INACTIVE)
 * - Audit fields inherited from BasicEntities (createdAt, updatedAt, etc.)
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "sellers", schema = SCHEMA_NAME)
public class Seller extends BasicEntities {

    /**
     * Primary Key - Auto-generated seller ID
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

    /**
     * Seller's full name
     * Required field, max 100 characters
     */
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    /**
     * Seller's email address
     * Required, unique constraint - no two sellers can have the same email
     */
    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    /**
     * Seller's current status
     * Stored as STRING in database ("ACTIVE" or "INACTIVE")
     * Defaults to ACTIVE when a new seller is created
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private SellerStatus status = SellerStatus.ACTIVE;

    /**
     * Date when the seller joined the platform
     * Required field, uses LocalDate (date only, no time)
     */
    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    /**
     * Enum for Seller Status
     * ACTIVE: Seller can operate normally
     * INACTIVE: Seller account is disabled
     */
    public enum SellerStatus {
        ACTIVE,
        INACTIVE
    }
}
