package com.spirent.itest_backend.api;

import com.spirent.birdwatch.model.Bird;
import com.spirent.birdwatch.model.BirdRequest;
import com.spirent.birdwatch.model.PaginatedSightings;
import com.spirent.birdwatch.model.Sighting;
import com.spirent.birdwatch.model.SightingRequest;
import com.spirent.itest_backend.config.AbstractIntegrationTest;
import com.spirent.itest_backend.testdata.BirdTestDataBuilder;
import com.spirent.itest_backend.testdata.SightingTestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "classpath:test-cleanup.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class SightingsControllerE2ETest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    private String birdsUrl;
    private String sightingsUrl;

    @BeforeEach
    void setUp() {
        birdsUrl = "http://localhost:" + port + "/api/v1/birds";
        sightingsUrl = "http://localhost:" + port + "/api/v1/sightings";
    }

    @Test
    void shouldCreateSightingSuccessfully() {
        // Given
        BirdRequest birdRequest = BirdTestDataBuilder.robinRequest();
        ResponseEntity<Bird> birdResponse = restTemplate.postForEntity(birdsUrl, birdRequest, Bird.class);
        Long birdId = birdResponse.getBody().getId();

        SightingRequest sightingRequest = SightingTestDataBuilder.centralParkSighting();
        sightingRequest.setBirdId(birdId);

        // When
        ResponseEntity<Sighting> response = restTemplate.postForEntity(sightingsUrl, sightingRequest, Sighting.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getLocation()).isEqualTo("Central Park, New York");
        assertThat(response.getBody().getDateTime()).isNotNull();
        assertThat(response.getBody().getBirdId()).isEqualTo(birdId);
        assertThat(response.getBody().getCreatedAt()).isNotNull();
    }

    @Test
    void shouldReturn404WhenCreatingSightingForNonExistentBird() {
        // Given
        SightingRequest sightingRequest = SightingTestDataBuilder.centralParkSighting();
        sightingRequest.setBirdId(999L); // Non-existent bird ID

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(sightingsUrl, sightingRequest, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldFindSightingsByBirdName() {
        // Given - Create birds and sightings
        BirdRequest robinRequest = BirdTestDataBuilder.robinRequest();
        BirdRequest cardinalRequest = BirdTestDataBuilder.cardinalRequest();
        
        ResponseEntity<Bird> robinResponse = restTemplate.postForEntity(birdsUrl, robinRequest, Bird.class);
        ResponseEntity<Bird> cardinalResponse = restTemplate.postForEntity(birdsUrl, cardinalRequest, Bird.class);
        
        Long robinId = robinResponse.getBody().getId();
        Long cardinalId = cardinalResponse.getBody().getId();

        // Create sightings
        SightingRequest sighting1 = SightingTestDataBuilder.centralParkSighting();
        sighting1.setBirdId(robinId);
        restTemplate.postForEntity(sightingsUrl, sighting1, Sighting.class);
        
        SightingRequest sighting2 = SightingTestDataBuilder.goldenGateSighting();
        sighting2.setBirdId(cardinalId);
        restTemplate.postForEntity(sightingsUrl, sighting2, Sighting.class);

        // When - Search by bird name
        ResponseEntity<PaginatedSightings> response = restTemplate.getForEntity(
                sightingsUrl + "?birdName=robin", PaginatedSightings.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getBirdId()).isEqualTo(robinId);
    }

    @Test
    void shouldFindSightingsByLocation() {
        // Given - Create birds and sightings
        BirdRequest birdRequest = BirdTestDataBuilder.robinRequest();
        ResponseEntity<Bird> birdResponse = restTemplate.postForEntity(birdsUrl, birdRequest, Bird.class);
        Long birdId = birdResponse.getBody().getId();

        // Create sightings at different locations
        SightingRequest sighting1 = SightingTestDataBuilder.centralParkSighting();
        sighting1.setBirdId(birdId);
        restTemplate.postForEntity(sightingsUrl, sighting1, Sighting.class);
        
        SightingRequest sighting2 = SightingTestDataBuilder.goldenGateSighting();
        sighting2.setBirdId(birdId);
        restTemplate.postForEntity(sightingsUrl, sighting2, Sighting.class);

        // When - Search by location
        ResponseEntity<PaginatedSightings> response = restTemplate.getForEntity(
                sightingsUrl + "?location=central", PaginatedSightings.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getLocation()).contains("Central Park");
    }

    @Test
    void shouldFindSightingsByDateRange() {
        // Given - Create birds and sightings
        BirdRequest birdRequest = BirdTestDataBuilder.robinRequest();
        ResponseEntity<Bird> birdResponse = restTemplate.postForEntity(birdsUrl, birdRequest, Bird.class);
        Long birdId = birdResponse.getBody().getId();

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime yesterday = now.minusDays(1);
        OffsetDateTime twoDaysAgo = now.minusDays(2);

        // Create sightings at different times
        SightingRequest sighting1 = SightingTestDataBuilder.centralParkSighting();
        sighting1.setBirdId(birdId);
        sighting1.setDateTime(now);
        restTemplate.postForEntity(sightingsUrl, sighting1, Sighting.class);
        
        SightingRequest sighting2 = SightingTestDataBuilder.goldenGateSighting();
        sighting2.setBirdId(birdId);
        sighting2.setDateTime(yesterday);
        restTemplate.postForEntity(sightingsUrl, sighting2, Sighting.class);
        
        SightingRequest sighting3 = SightingTestDataBuilder.londonSighting();
        sighting3.setBirdId(birdId);
        sighting3.setDateTime(twoDaysAgo);
        restTemplate.postForEntity(sightingsUrl, sighting3, Sighting.class);

        DateTimeFormatter fmt = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        String start = yesterday.withNano(0).format(fmt).replace("+", "%2B");
        String end = now.withNano(0).format(fmt).replace("+", "%2B");

        URI uri = UriComponentsBuilder.fromUriString(sightingsUrl)
                .queryParam("startDateTime", start)
                .queryParam("endDateTime", end)
                .build(true)
                .encode()
                .toUri();

        // When - Search by date range (last 2 days)
        ResponseEntity<PaginatedSightings> response = restTemplate.getForEntity(uri, PaginatedSightings.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void shouldFindSightingsWithMultipleFilters() {
        // Given - Create birds and sightings
        BirdRequest robinRequest = BirdTestDataBuilder.robinRequest();
        BirdRequest cardinalRequest = BirdTestDataBuilder.cardinalRequest();
        
        ResponseEntity<Bird> robinResponse = restTemplate.postForEntity(birdsUrl, robinRequest, Bird.class);
        ResponseEntity<Bird> cardinalResponse = restTemplate.postForEntity(birdsUrl, cardinalRequest, Bird.class);
        
        Long robinId = robinResponse.getBody().getId();
        Long cardinalId = cardinalResponse.getBody().getId();

        OffsetDateTime now = OffsetDateTime.now();

        // Create sightings
        SightingRequest sighting1 = SightingTestDataBuilder.centralParkSighting();
        sighting1.setBirdId(robinId);
        sighting1.setDateTime(now);
        restTemplate.postForEntity(sightingsUrl, sighting1, Sighting.class);
        
        SightingRequest sighting2 = SightingTestDataBuilder.goldenGateSighting();
        sighting2.setBirdId(cardinalId);
        sighting2.setDateTime(now);
        restTemplate.postForEntity(sightingsUrl, sighting2, Sighting.class);

        // When - Search with multiple filters
        ResponseEntity<PaginatedSightings> response = restTemplate.getForEntity(
                sightingsUrl + "?birdName=robin&location=central", PaginatedSightings.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getBirdId()).isEqualTo(robinResponse.getBody().getId());
        assertThat(response.getBody().getContent().get(0).getLocation()).contains("Central Park");
    }

    @Test
    void shouldFindSightingsWithPagination() {
        // Given - Create birds and multiple sightings
        BirdRequest birdRequest = BirdTestDataBuilder.robinRequest();
        ResponseEntity<Bird> birdResponse = restTemplate.postForEntity(birdsUrl, birdRequest, Bird.class);
        Long birdId = Objects.requireNonNull(birdResponse.getBody()).getId();

        // Create multiple sightings
        for (int i = 0; i < 5; i++) {
            SightingRequest sighting = SightingTestDataBuilder.centralParkSighting();
            sighting.setBirdId(birdId);
            sighting.setLocation("Location " + i);
            restTemplate.postForEntity(sightingsUrl, sighting, Sighting.class);
        }

        // When - Get first page with size 3
        ResponseEntity<PaginatedSightings> response = restTemplate.getForEntity(
                sightingsUrl + "?page=0&size=3", PaginatedSightings.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(3);
        assertThat(response.getBody().getPage()).isEqualTo(0);
        assertThat(response.getBody().getSize()).isEqualTo(3);
        assertThat(response.getBody().getTotalElements()).isEqualTo(5);
        assertThat(response.getBody().getTotalPages()).isEqualTo(2);
        assertThat(response.getBody().getFirst()).isTrue();
        assertThat(response.getBody().getLast()).isFalse();
    }

    @Test
    void shouldFindSightingsWithSorting() {
        // Given - Create birds and sightings
        BirdRequest birdRequest = BirdTestDataBuilder.robinRequest();
        ResponseEntity<Bird> birdResponse = restTemplate.postForEntity(birdsUrl, birdRequest, Bird.class);
        Long birdId = Objects.requireNonNull(birdResponse.getBody()).getId();

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime yesterday = now.minusDays(1);
        OffsetDateTime twoDaysAgo = now.minusDays(2);

        // Create sightings at different times
        SightingRequest sighting1 = SightingTestDataBuilder.centralParkSighting();
        sighting1.setBirdId(birdId);
        sighting1.setDateTime(now);
        sighting1.setLocation("Location A");
        restTemplate.postForEntity(sightingsUrl, sighting1, Sighting.class);
        
        SightingRequest sighting2 = SightingTestDataBuilder.goldenGateSighting();
        sighting2.setBirdId(birdId);
        sighting2.setDateTime(yesterday);
        sighting2.setLocation("Location B");
        restTemplate.postForEntity(sightingsUrl, sighting2, Sighting.class);
        
        SightingRequest sighting3 = SightingTestDataBuilder.londonSighting();
        sighting3.setBirdId(birdId);
        sighting3.setDateTime(twoDaysAgo);
        sighting3.setLocation("Location C");
        restTemplate.postForEntity(sightingsUrl, sighting3, Sighting.class);

        // When - Sort by dateTime descending (most recent first)
        ResponseEntity<PaginatedSightings> response = restTemplate.getForEntity(
                sightingsUrl + "?sort=dateTime&direction=desc", PaginatedSightings.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(3);
        
        // Verify sorting (should be in descending order by dateTime)
        var sightings = response.getBody().getContent();
        assertThat(sightings.get(0).getLocation()).isEqualTo("Location A"); // Most recent
        assertThat(sightings.get(1).getLocation()).isEqualTo("Location B");
        assertThat(sightings.get(2).getLocation()).isEqualTo("Location C"); // Oldest
    }

    @Test
    void shouldFindSightingsWithAllFilters() {
        // Given - Create birds and sightings
        BirdRequest robinRequest = BirdTestDataBuilder.robinRequest();
        BirdRequest cardinalRequest = BirdTestDataBuilder.cardinalRequest();
        
        ResponseEntity<Bird> robinResponse = restTemplate.postForEntity(birdsUrl, robinRequest, Bird.class);
        ResponseEntity<Bird> cardinalResponse = restTemplate.postForEntity(birdsUrl, cardinalRequest, Bird.class);
        
        Long robinId = Objects.requireNonNull(robinResponse.getBody()).getId();
        Long cardinalId = Objects.requireNonNull(cardinalResponse.getBody()).getId();

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime yesterday = now.minusDays(1);

        // Create sightings
        SightingRequest sighting1 = SightingTestDataBuilder.centralParkSighting();
        sighting1.setBirdId(robinId);
        sighting1.setDateTime(now);
        restTemplate.postForEntity(sightingsUrl, sighting1, Sighting.class);
        
        SightingRequest sighting2 = SightingTestDataBuilder.goldenGateSighting();
        sighting2.setBirdId(cardinalId);
        sighting2.setDateTime(yesterday);
        restTemplate.postForEntity(sightingsUrl, sighting2, Sighting.class);

        DateTimeFormatter fmt = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        String start = yesterday.minusHours(1).withNano(0).format(fmt).replace("+", "%2B");
        String end = now.plusHours(1).withNano(0).format(fmt).replace("+", "%2B");

        URI uri = UriComponentsBuilder.fromUriString(sightingsUrl)
                .queryParam("birdName", "robin")
                .queryParam("location", "central")
                .queryParam("startDateTime", start)
                .queryParam("endDateTime", end)
                .build(true)
                .encode()
                .toUri();

        // When - Search with all filters
        ResponseEntity<PaginatedSightings> response = restTemplate.getForEntity(uri, PaginatedSightings.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getLocation()).contains("Central Park");
    }

    @Test
    void shouldReturnEmptyResultWhenNoSightingsMatch() {
        // Given - Create birds and sightings
        BirdRequest birdRequest = BirdTestDataBuilder.robinRequest();
        ResponseEntity<Bird> birdResponse = restTemplate.postForEntity(birdsUrl, birdRequest, Bird.class);
        Long birdId = birdResponse.getBody().getId();

        SightingRequest sighting = SightingTestDataBuilder.centralParkSighting();
        sighting.setBirdId(birdId);
        restTemplate.postForEntity(sightingsUrl, sighting, Sighting.class);

        // When - Search for non-existent bird
        ResponseEntity<PaginatedSightings> response = restTemplate.getForEntity(
                sightingsUrl + "?birdName=eagle", PaginatedSightings.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isEmpty();
        assertThat(response.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    void shouldFindAllSightingsWhenNoFiltersProvided() {
        // Given - Create birds and sightings
        BirdRequest robinRequest = BirdTestDataBuilder.robinRequest();
        BirdRequest cardinalRequest = BirdTestDataBuilder.cardinalRequest();
        
        ResponseEntity<Bird> robinResponse = restTemplate.postForEntity(birdsUrl, robinRequest, Bird.class);
        ResponseEntity<Bird> cardinalResponse = restTemplate.postForEntity(birdsUrl, cardinalRequest, Bird.class);
        
        Long robinId = Objects.requireNonNull(robinResponse.getBody()).getId();
        Long cardinalId = Objects.requireNonNull(cardinalResponse.getBody()).getId();

        // Create sightings
        SightingRequest sighting1 = SightingTestDataBuilder.centralParkSighting();
        sighting1.setBirdId(robinId);
        restTemplate.postForEntity(sightingsUrl, sighting1, Sighting.class);
        
        SightingRequest sighting2 = SightingTestDataBuilder.goldenGateSighting();
        sighting2.setBirdId(cardinalId);
        restTemplate.postForEntity(sightingsUrl, sighting2, Sighting.class);

        // When - Get all sightings
        ResponseEntity<PaginatedSightings> response = restTemplate.getForEntity(sightingsUrl, PaginatedSightings.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(2);
        assertThat(response.getBody().getTotalElements()).isEqualTo(2);
    }
}