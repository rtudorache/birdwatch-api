package com.spirent.itest_backend.repository;

import com.spirent.itest_backend.repository.entity.BirdEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BirdRepository extends JpaRepository<BirdEntity, Long> {

    Page<BirdEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<BirdEntity> findByColorContainingIgnoreCase(String color, Pageable pageable);

    Page<BirdEntity> findByNameContainingIgnoreCaseAndColorContainingIgnoreCase(String name, String color, Pageable pageable);
}
