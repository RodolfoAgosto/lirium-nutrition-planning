package com.lirium.nutrition.model.entity;

import com.lirium.nutrition.model.enums.RestrictionCategory;
import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a dietary restriction, e.g., gluten-free, low-sodium.
 * Code is unique and used as a natural identifier.
 */

@Entity
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Builder
@EqualsAndHashCode(of = "id") // asegura consistencia en colecciones
public class Restriction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, unique = false, length = 80)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RestrictionCategory category;

    @Column(nullable = false, unique = false, length = 255)
    private String description;

}
