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

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seller_generator")
    @SequenceGenerator(
            name = "seller_generator",
            sequenceName = SCHEMA_NAME + ".seq_seller_id",
            allocationSize = 1
    )
    @Column(name = "seller_id")
    private Integer sellerId;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private SellerStatus status = SellerStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private SellerRole role = SellerRole.SELLER;

    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    public enum SellerStatus {
        ACTIVE,
        INACTIVE
    }

    public enum SellerRole {
        SELLER
    }
}