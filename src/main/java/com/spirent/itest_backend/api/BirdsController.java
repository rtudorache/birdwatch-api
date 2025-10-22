package com.spirent.itest_backend.api;

import com.spirent.birdwatch.api.BirdsApi;
import com.spirent.birdwatch.model.Bird;
import com.spirent.birdwatch.model.BirdRequest;
import com.spirent.birdwatch.model.PaginatedBirds;
import com.spirent.birdwatch.model.UpdateBirdRequest;
import com.spirent.itest_backend.service.BirdService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class BirdsController implements BirdsApi {

    private static final Logger logger = LogManager.getLogger(BirdsController.class);
    private final BirdService birdService;

    public BirdsController(BirdService birdService) {
        this.birdService = birdService;
    }

    @Override
    public ResponseEntity<Bird> addBird(BirdRequest birdRequest) {
        logger.info("Creating new bird with name: {}", birdRequest.getName());
        Bird createdBird = birdService.createBird(birdRequest);
        logger.info("Successfully created bird with ID: {}", createdBird.getId());
        return ResponseEntity.status(201).body(createdBird);
    }

    @Override
    public ResponseEntity<Bird> updateBird(Long birdId, UpdateBirdRequest updateBirdRequest) {
        logger.info("Updating bird with ID: {}", birdId);
        Bird updatedBird = birdService.updateBird(birdId, updateBirdRequest);
        logger.info("Successfully updated bird with ID: {}", updatedBird.getId());
        return ResponseEntity.ok(updatedBird);
    }

    @Override
    public ResponseEntity<Void> deleteBird(Long birdId) {
        logger.info("Deleting bird with ID: {}", birdId);
        birdService.deleteBird(birdId);
        logger.info("Successfully deleted bird with ID: {}", birdId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Bird> getBirdById(Long birdId) {
        logger.info("Getting bird with ID: {}", birdId);
        Bird bird = birdService.getBirdById(birdId);
        logger.info("Successfully got bird with ID: {}", bird.getId());
        return ResponseEntity.ok(bird);
    }

    @Override
    public ResponseEntity<PaginatedBirds> findBirds(String name, String color, Double weightMin, Double weightMax, Double heightMin, Double heightMax, Integer page, Integer size, String sort, String direction) {
        logger.info("Searching for birds with name: {}, color: {}, weightMin: {}, weightMax: {}, heightMin: {}, heightMax: {}, page: {}, size: {}, sort: {}, direction: {}", 
                   name, color, weightMin, weightMax, heightMin, heightMax, page, size, sort, direction);
        
        PaginatedBirds paginatedBirds = birdService.findBirdsWithFilters(name, color, weightMin, weightMax, heightMin, heightMax, page, size, sort, direction);
        return ResponseEntity.ok(paginatedBirds);
    }
}
