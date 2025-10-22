package com.spirent.itest_backend.testdata;

import com.spirent.birdwatch.model.Sighting;
import com.spirent.birdwatch.model.SightingRequest;

import java.time.OffsetDateTime;

public class SightingTestDataBuilder {

    public static SightingRequest centralParkSighting() {
        SightingRequest request = new SightingRequest();
        request.setBirdId(1L);
        request.setLocation("Central Park, New York");
        request.setDateTime(OffsetDateTime.now());
        return request;
    }

    public static SightingRequest goldenGateSighting() {
        SightingRequest request = new SightingRequest();
        request.setBirdId(1L);
        request.setLocation("Golden Gate Park, San Francisco");
        request.setDateTime(OffsetDateTime.now().minusDays(1));
        return request;
    }

    public static SightingRequest londonSighting() {
        SightingRequest request = new SightingRequest();
        request.setBirdId(1L);
        request.setLocation("Hyde Park, London");
        request.setDateTime(OffsetDateTime.now().minusDays(2));
        return request;
    }

    public static Sighting centralParkSightingWithBird() {
        Sighting sighting = new Sighting();
        sighting.setId(1L);
        sighting.setLocation("Central Park, New York");
        sighting.setDateTime(OffsetDateTime.now());
        sighting.setBirdId(BirdTestDataBuilder.robin().getId());
        sighting.setCreatedAt(OffsetDateTime.now());
        sighting.setUpdatedAt(OffsetDateTime.now());
        return sighting;
    }

    public static Sighting goldenGateSightingWithBird() {
        Sighting sighting = new Sighting();
        sighting.setId(2L);
        sighting.setLocation("Golden Gate Park, San Francisco");
        sighting.setDateTime(OffsetDateTime.now().minusDays(1));
        sighting.setBirdId(BirdTestDataBuilder.cardinal().getId());
        sighting.setCreatedAt(OffsetDateTime.now());
        sighting.setUpdatedAt(OffsetDateTime.now());
        return sighting;
    }

    public static Sighting londonSightingWithBird() {
        Sighting sighting = new Sighting();
        sighting.setId(3L);
        sighting.setLocation("Hyde Park, London");
        sighting.setDateTime(OffsetDateTime.now().minusDays(2));
        sighting.setBirdId(BirdTestDataBuilder.blueJay().getId());
        sighting.setCreatedAt(OffsetDateTime.now());
        sighting.setUpdatedAt(OffsetDateTime.now());
        return sighting;
    }
}
