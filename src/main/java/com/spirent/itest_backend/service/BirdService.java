package com.spirent.itest_backend.service;

import com.spirent.birdwatch.model.Bird;
import com.spirent.birdwatch.model.BirdRequest;
import com.spirent.birdwatch.model.PaginatedBirds;
import com.spirent.birdwatch.model.UpdateBirdRequest;
import com.spirent.itest_backend.exception.ResourceNotFoundException;
import com.spirent.itest_backend.repository.BirdRepository;
import com.spirent.itest_backend.repository.entity.BirdEntity;
import com.spirent.itest_backend.utils.BirdMapper;
import com.spirent.itest_backend.utils.PaginationMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BirdService {

    private static final Logger logger = LogManager.getLogger(BirdService.class);
    private final BirdRepository birdRepository;
    private final BirdMapper birdMapper;
    private final PaginationMapper paginationMapper;

    public BirdService(BirdRepository birdRepository, BirdMapper birdMapper, PaginationMapper paginationMapper) {
        this.birdRepository = birdRepository;
        this.birdMapper = birdMapper;
        this.paginationMapper = paginationMapper;
    }

    public PaginatedBirds findBirdsWithFilters(String name, String color, Double weightMin, Double weightMax, 
                                             Double heightMin, Double heightMax, Integer page, Integer size, 
                                             String sort, String direction) {
        logger.info("Finding birds with filters - name: {}, color: {}, weightMin: {}, weightMax: {}, heightMin: {}, heightMax: {}, page: {}, size: {}, sort: {}, direction: {}", 
                   name, color, weightMin, weightMax, heightMin, heightMax, page, size, sort, direction);
        
        // Set default values
        int pageNumber = (page != null) ? page : 0;
        int pageSize = (size != null) ? size : 20;
        String sortField = (sort != null) ? sort : "name";
        String sortDirection = (direction != null) ? direction : "asc";
        
        // Create sort object
        Sort.Direction sortDir = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sortObj = Sort.by(sortDir, sortField);
        
        // Create pageable object
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortObj);
        
        // Execute query based on filters
        Page<BirdEntity> birdPage;
        
        if (name != null && color != null) {
            birdPage = birdRepository.findByNameContainingIgnoreCaseAndColorContainingIgnoreCase(name, color, pageable);
        } else if (name != null) {
            birdPage = birdRepository.findByNameContainingIgnoreCase(name, pageable);
        } else if (color != null) {
            birdPage = birdRepository.findByColorContainingIgnoreCase(color, pageable);
        } else {
            birdPage = birdRepository.findAll(pageable);
        }
        
        // Apply weight and height filters if provided
        if (weightMin != null || weightMax != null || heightMin != null || heightMax != null) {
            List<BirdEntity> filteredBirds = birdPage.getContent().stream()
                    .filter(bird -> {
                        boolean weightMatch = true;
                        boolean heightMatch = true;
                        
                        if (weightMin != null && bird.getWeight() < weightMin) {
                            weightMatch = false;
                        }
                        if (weightMax != null && bird.getWeight() > weightMax) {
                            weightMatch = false;
                        }
                        if (heightMin != null && bird.getHeight() < heightMin) {
                            heightMatch = false;
                        }
                        if (heightMax != null && bird.getHeight() > heightMax) {
                            heightMatch = false;
                        }
                        
                        return weightMatch && heightMatch;
                    })
                    .collect(Collectors.toList());
            
            // Create a custom page with filtered results
            Page<BirdEntity> customPage = new org.springframework.data.domain.PageImpl<>(
                    filteredBirds, pageable, filteredBirds.size());
            birdPage = customPage;
        }
        
        // Convert to DTOs and create paginated response
        PaginatedBirds result = paginationMapper.mapToPaginatedBirds(birdPage, birdMapper::entityToBird);
        logger.info("Found {} birds out of {} total", result.getContent().size(), birdPage.getTotalElements());
        return result;
    }

    public Bird getBirdById(Long id) {
        BirdEntity birdEntity = birdRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bird not found with id: " + id));

        return birdMapper.entityToBird(birdEntity);
    }

    public Bird createBird(BirdRequest birdRequest) {
        logger.info("Creating bird with name: {}", birdRequest.getName());
        BirdEntity birdEntity = birdMapper.birdRequestToEntity(birdRequest);
        BirdEntity saved = birdRepository.save(birdEntity);
        logger.info("Successfully created bird with ID: {}", saved.getId());
        return birdMapper.entityToBird(saved);
    }

    public Bird updateBird(Long birdId, UpdateBirdRequest updateBirdRequest) {
        logger.info("Updating bird with ID: {} with new data - name: {}, color: {}, weight: {}, height: {}", 
                   birdId, updateBirdRequest.getName(), updateBirdRequest.getColor(), 
                   updateBirdRequest.getWeight(), updateBirdRequest.getHeight());
        
        // Find existing bird
        BirdEntity existingBird = birdRepository.findById(birdId)
                .orElseThrow(() -> new ResourceNotFoundException("Bird not found with id: " + birdId));
        
        // Update fields only if they are provided (not null)
        if (updateBirdRequest.getName() != null) {
            existingBird.setName(updateBirdRequest.getName());
            existingBird.setUpdatedAt(OffsetDateTime.now());
        }
        if (updateBirdRequest.getColor() != null) {
            existingBird.setColor(updateBirdRequest.getColor());
            existingBird.setUpdatedAt(OffsetDateTime.now());
        }
        if (updateBirdRequest.getWeight() != null) {
            existingBird.setWeight(updateBirdRequest.getWeight());
            existingBird.setUpdatedAt(OffsetDateTime.now());
        }
        if (updateBirdRequest.getHeight() != null) {
            existingBird.setHeight(updateBirdRequest.getHeight());
            existingBird.setUpdatedAt(OffsetDateTime.now());
        }
        
        // Save updated bird
        BirdEntity updatedBird = birdRepository.save(existingBird);
        logger.info("Successfully updated bird with ID: {}", updatedBird.getId());
        
        return birdMapper.entityToBird(updatedBird);
    }

    public void deleteBird(Long birdId) {
        logger.info("Deleting bird with ID: {}", birdId);
        
        // Check if bird exists
        if (!birdRepository.existsById(birdId)) {
            logger.warn("Attempted to delete non-existent bird with ID: {}", birdId);
            throw new ResourceNotFoundException("Bird not found with id: " + birdId);
        }
        
        // Delete the bird
        birdRepository.deleteById(birdId);
        logger.info("Successfully deleted bird with ID: {}", birdId);
    }
}
