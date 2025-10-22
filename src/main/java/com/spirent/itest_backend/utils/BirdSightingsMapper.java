package com.spirent.itest_backend.utils;

import com.spirent.birdwatch.model.Sighting;
import com.spirent.itest_backend.repository.entity.SightingEntity;
import org.springframework.stereotype.Component;

@Component
public class BirdSightingsMapper {

    public Sighting entityToSighting(SightingEntity sightingEntity) {
        Sighting sighting = new Sighting();
        sighting.setId(sightingEntity.getId());
        sighting.setLocation(sightingEntity.getLocation());
        sighting.setDateTime(sightingEntity.getDateTime());

        if (sightingEntity.getBird() != null) {
            sighting.setBirdId(sightingEntity.getBird().getId());
        }
        sighting.setCreatedAt(sightingEntity.getCreatedAt());
        sighting.setUpdatedAt(sightingEntity.getUpdatedAt());
        return sighting;
    }
}
