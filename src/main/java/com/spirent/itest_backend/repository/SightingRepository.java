package com.spirent.itest_backend.repository;

import com.spirent.itest_backend.repository.entity.SightingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface SightingRepository extends JpaRepository<SightingEntity, Long> {

    // Find by bird name with pagination
    Page<SightingEntity> findByBirdNameContainingIgnoreCase(String birdName, Pageable pageable);
    
    // Find by location with pagination
    Page<SightingEntity> findByLocationContainingIgnoreCase(String location, Pageable pageable);
    
    // Find by date range with pagination
    Page<SightingEntity> findByDateTimeBetween(OffsetDateTime start, OffsetDateTime end, Pageable pageable);
    
    // Find by bird name and location with pagination
    Page<SightingEntity> findByBirdNameContainingIgnoreCaseAndLocationContainingIgnoreCase(String birdName, String location, Pageable pageable);
    
    // Find by bird name and date range with pagination
    Page<SightingEntity> findByBirdNameContainingIgnoreCaseAndDateTimeBetween(String birdName, OffsetDateTime start, OffsetDateTime end, Pageable pageable);
    
    // Find by location and date range with pagination
    Page<SightingEntity> findByLocationContainingIgnoreCaseAndDateTimeBetween(String location, OffsetDateTime start, OffsetDateTime end, Pageable pageable);
    
    // Find by all filters with pagination
    Page<SightingEntity> findByBirdNameContainingIgnoreCaseAndLocationContainingIgnoreCaseAndDateTimeBetween(String birdName, String location, OffsetDateTime start, OffsetDateTime end, Pageable pageable);
}
