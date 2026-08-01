package com.lirium.nutrition.model.entity;

import com.lirium.nutrition.model.enums.FoodTag;
import com.lirium.nutrition.model.enums.RestrictionCategory;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a dietary restriction, e.g., gluten-free, low-sodium.
 * Code is unique and used as a natural identifier.
 */

@Entity
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Builder
@EqualsAndHashCode(of = "id") // asegura consistencia en colecciones
@Table(name = "restrictions")
public class Restriction extends DateAuditable {

    @Id
    @SequenceGenerator(
            name = "restriction_seq",
            sequenceName = "restriction_seq",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "restriction_seq")
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 80)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private RestrictionCategory category;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "restriction_excluded_tags",
            joinColumns = @JoinColumn(name = "restriction_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "food_tag")
    private Set<FoodTag> excludedTags = new HashSet<>();

}
