package com.spirent.itest_backend.repository.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Set;
import java.time.OffsetDateTime;

@Entity
@Table(name = "birds", schema = "birds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BirdEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String color;

    @Column(nullable = false)
    private Double weight;

    @Column(nullable = false)
    private Double height;

    @OneToMany(mappedBy = "bird", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SightingEntity> sightings;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = true)
    private OffsetDateTime updatedAt;
}
