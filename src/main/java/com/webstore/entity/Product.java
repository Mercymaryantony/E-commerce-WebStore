package com.webstore.entity;

import static com.webstore.constant.DatabaseConstants.SCHEMA_NAME;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "products", schema = SCHEMA_NAME)
public class Product extends BasicEntities {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_generator")
    @SequenceGenerator(
            name = "product_generator",
            sequenceName = SCHEMA_NAME + ".seq_products_id",
            allocationSize = 1
    )
    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "product_name", length = 50, nullable = false, unique = true)
    private String productName;

    @Column(name = "product_description", length = 100)
    private String productDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalogue_category_id", nullable = false)
    private CatalogueCategory catalogueCategory;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductPrice> productPrices = new ArrayList<>();
}
