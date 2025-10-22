package com.spirent.itest_backend.api;

import com.spirent.birdwatch.api.SightingsApi;
import com.spirent.birdwatch.model.Sighting;
import com.spirent.birdwatch.model.SightingRequest;
import com.spirent.birdwatch.model.PaginatedSightings;
import com.spirent.itest_backend.service.SightingService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
public class SightingsController implements SightingsApi {

    private static final Logger logger = LogManager.getLogger(SightingsController.class);
    private final SightingService sightingService;

    public SightingsController(SightingService sightingService) {
        this.sightingService = sightingService;
    }

    @Override
    public ResponseEntity<Sighting> addSighting(SightingRequest sightingRequest) {
        logger.info("Creating new sighting for bird ID: {} at location: {}", 
                   sightingRequest.getBirdId(), sightingRequest.getLocation());
        
        Sighting createdSighting = sightingService.createSighting(sightingRequest);
        logger.info("Successfully created sighting with ID: {}", createdSighting.getId());
        
        return ResponseEntity.status(201).body(createdSighting);
    }

    @Override
    public ResponseEntity<PaginatedSightings> findSightings(String birdName, String location,
                                                            OffsetDateTime startDateTime,
                                                            OffsetDateTime endDateTime, Integer page, Integer size, String sort, String direction) {
        logger.info("Searching for sightings with birdName: {}, location: {}, startDateTime: {}, endDateTime: {}, page: {}, size: {}, sort: {}, direction: {}", 
                   birdName, location, startDateTime, endDateTime, page, size, sort, direction);
        
        PaginatedSightings paginatedSightings = sightingService.findSightingsWithFilters(
                birdName, location, startDateTime, endDateTime, page, size, sort, direction);
        
        logger.info("Found {} sightings", paginatedSightings.getContent().size());
        return ResponseEntity.ok(paginatedSightings);
    }
}
