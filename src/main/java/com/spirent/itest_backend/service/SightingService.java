package com.spirent.itest_backend.service;

import com.spirent.birdwatch.model.Sighting;
import com.spirent.birdwatch.model.SightingRequest;
import com.spirent.birdwatch.model.PaginatedSightings;
import com.spirent.itest_backend.exception.ResourceNotFoundException;
import com.spirent.itest_backend.repository.BirdRepository;
import com.spirent.itest_backend.repository.SightingRepository;
import com.spirent.itest_backend.repository.entity.BirdEntity;
import com.spirent.itest_backend.repository.entity.SightingEntity;
import com.spirent.itest_backend.utils.BirdSightingsMapper;
import com.spirent.itest_backend.utils.PaginationMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class SightingService {

    private static final Logger logger = LogManager.getLogger(SightingService.class);
    private final BirdRepository birdRepository;
    private final SightingRepository sightingRepository;
    private final BirdSightingsMapper birdSightingsMapper;
    private final PaginationMapper paginationMapper;

    public SightingService(BirdRepository birdRepository, SightingRepository sightingRepository, 
                         BirdSightingsMapper birdSightingsMapper, PaginationMapper paginationMapper) {
        this.birdRepository = birdRepository;
        this.sightingRepository = sightingRepository;
        this.birdSightingsMapper = birdSightingsMapper;
        this.paginationMapper = paginationMapper;
    }

    public Sighting createSighting(SightingRequest sightingRequest) {
        logger.info("Creating sighting for bird ID: {} at location: {}", 
                   sightingRequest.getBirdId(), sightingRequest.getLocation());
        
        // Find the bird entity
        BirdEntity birdEntity = birdRepository.findById(sightingRequest.getBirdId())
                .orElseThrow(() -> new ResourceNotFoundException("Bird not found with id: " + sightingRequest.getBirdId()));
        
        // Create sighting entity
        SightingEntity sightingEntity = new SightingEntity();
        sightingEntity.setBird(birdEntity);
        sightingEntity.setLocation(sightingRequest.getLocation());
        sightingEntity.setDateTime(sightingRequest.getDateTime());
        sightingEntity.setCreatedAt(OffsetDateTime.now());
        
        // Save the sighting
        SightingEntity savedSighting = sightingRepository.save(sightingEntity);
        logger.info("Successfully created sighting with ID: {}", savedSighting.getId());
        
        // Convert to DTO and return
        return birdSightingsMapper.entityToSighting(savedSighting);
    }

    public PaginatedSightings findSightingsWithFilters(String birdName, String location, 
                                                     OffsetDateTime startDateTime, OffsetDateTime endDateTime,
                                                     Integer page, Integer size, String sort, String direction) {
        logger.info("Finding sightings with filters - birdName: {}, location: {}, startDateTime: {}, endDateTime: {}, page: {}, size: {}, sort: {}, direction: {}", 
                   birdName, location, startDateTime, endDateTime, page, size, sort, direction);
        
        // Set default values
        int pageNumber = (page != null) ? page : 0;
        int pageSize = (size != null) ? size : 20;
        String sortField = (sort != null) ? sort : "dateTime";
        String sortDirection = (direction != null) ? direction : "desc";
        
        // Create sort object
        Sort.Direction sortDir = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sortObj = Sort.by(sortDir, sortField);
        
        // Create pageable object
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortObj);
        
        // Execute query based on filters
        Page<SightingEntity> sightingPage;
        
        if (birdName != null && location != null && startDateTime != null && endDateTime != null) {
            sightingPage = sightingRepository.findByBirdNameContainingIgnoreCaseAndLocationContainingIgnoreCaseAndDateTimeBetween(
                    birdName, location, startDateTime, endDateTime, pageable);
        } else if (birdName != null && location != null) {
            sightingPage = sightingRepository.findByBirdNameContainingIgnoreCaseAndLocationContainingIgnoreCase(
                    birdName, location, pageable);
        } else if (birdName != null && startDateTime != null && endDateTime != null) {
            sightingPage = sightingRepository.findByBirdNameContainingIgnoreCaseAndDateTimeBetween(
                    birdName, startDateTime, endDateTime, pageable);
        } else if (location != null && startDateTime != null && endDateTime != null) {
            sightingPage = sightingRepository.findByLocationContainingIgnoreCaseAndDateTimeBetween(
                    location, startDateTime, endDateTime, pageable);
        } else if (birdName != null) {
            sightingPage = sightingRepository.findByBirdNameContainingIgnoreCase(birdName, pageable);
        } else if (location != null) {
            sightingPage = sightingRepository.findByLocationContainingIgnoreCase(location, pageable);
        } else if (startDateTime != null && endDateTime != null) {
            sightingPage = sightingRepository.findByDateTimeBetween(startDateTime, endDateTime, pageable);
        } else {
            sightingPage = sightingRepository.findAll(pageable);
        }
        
        // Convert to DTOs and create paginated response
        PaginatedSightings result = paginationMapper.mapToPaginatedSightings(sightingPage, birdSightingsMapper::entityToSighting);
        logger.info("Found {} sightings out of {} total", result.getContent().size(), sightingPage.getTotalElements());
        return result;
    }
}
