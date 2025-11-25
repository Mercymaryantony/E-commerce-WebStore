package com.webstore.entity;

import static com.webstore.constant.DatabaseConstants.SCHEMA_NAME;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "categories", schema = SCHEMA_NAME)
public class Category extends BasicEntities {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "category_generator")
    @SequenceGenerator(
            name = "category_generator",
            sequenceName = SCHEMA_NAME + ".seq_categories_id",
            allocationSize = 1
    )
    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "category_name", length = 50, nullable = false, unique = true)
    private String categoryName;

    @Column(name = "category_description", length = 100)
    private String categoryDescription;

    // Products are now linked through CatalogueCategory, not directly
    // @OneToMany(mappedBy = "category") removed

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<CatalogueCategory> catalogueCategories = new HashSet<>();
}
