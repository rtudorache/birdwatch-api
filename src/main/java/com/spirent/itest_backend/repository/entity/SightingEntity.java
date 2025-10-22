package com.spirent.itest_backend.repository.entity;

import lombok.*;
import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "sightings", schema = "birds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SightingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bird_id", nullable = false)
    private BirdEntity bird;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private OffsetDateTime dateTime;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = true)
    private OffsetDateTime updatedAt;
}
