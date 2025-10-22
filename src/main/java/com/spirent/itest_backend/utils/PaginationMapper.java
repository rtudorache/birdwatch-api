package com.spirent.itest_backend.utils;

import com.spirent.birdwatch.model.PaginatedBirds;
import com.spirent.birdwatch.model.PaginatedSightings;
import com.spirent.birdwatch.model.Bird;
import com.spirent.birdwatch.model.Sighting;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility class for mapping Spring Data Page objects to paginated response DTOs
 */
@Component
public class PaginationMapper {

    /**
     * Maps a Spring Data Page to PaginatedBirds response
     *
     * @param birdPage The Spring Data Page containing BirdEntity objects
     * @param mapperFunction Function to convert BirdEntity to Bird DTO
     * @return PaginatedBirds response
     */
    public <T> PaginatedBirds mapToPaginatedBirds(Page<T> birdPage, Function<T, Bird> mapperFunction) {
        List<Bird> birds = birdPage.getContent().stream()
                .map(mapperFunction)
                .collect(Collectors.toList());
        
        PaginatedBirds paginatedBirds = new PaginatedBirds();
        paginatedBirds.setContent(birds);
        paginatedBirds.setPage(birdPage.getNumber());
        paginatedBirds.setSize(birdPage.getSize());
        paginatedBirds.setTotalElements((int) birdPage.getTotalElements());
        paginatedBirds.setTotalPages(birdPage.getTotalPages());
        paginatedBirds.setFirst(birdPage.isFirst());
        paginatedBirds.setLast(birdPage.isLast());
        paginatedBirds.setNumberOfElements(birdPage.getNumberOfElements());
        
        return paginatedBirds;
    }

    /**
     * Maps a Spring Data Page to PaginatedSightings response
     *
     * @param sightingPage The Spring Data Page containing SightingEntity objects
     * @param mapperFunction Function to convert SightingEntity to Sighting DTO
     * @return PaginatedSightings response
     */
    public <T> PaginatedSightings mapToPaginatedSightings(Page<T> sightingPage, Function<T, Sighting> mapperFunction) {
        List<Sighting> sightings = sightingPage.getContent().stream()
                .map(mapperFunction)
                .collect(Collectors.toList());
        
        PaginatedSightings paginatedSightings = new PaginatedSightings();
        paginatedSightings.setContent(sightings);
        paginatedSightings.setPage(sightingPage.getNumber());
        paginatedSightings.setSize(sightingPage.getSize());
        paginatedSightings.setTotalElements((int) sightingPage.getTotalElements());
        paginatedSightings.setTotalPages(sightingPage.getTotalPages());
        paginatedSightings.setFirst(sightingPage.isFirst());
        paginatedSightings.setLast(sightingPage.isLast());
        paginatedSightings.setNumberOfElements(sightingPage.getNumberOfElements());
        
        return paginatedSightings;
    }
}
