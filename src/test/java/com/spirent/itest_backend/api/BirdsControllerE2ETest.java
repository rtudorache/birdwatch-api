package com.spirent.itest_backend.api;

import com.spirent.birdwatch.model.Bird;
import com.spirent.birdwatch.model.BirdRequest;
import com.spirent.birdwatch.model.PaginatedBirds;
import com.spirent.birdwatch.model.UpdateBirdRequest;
import com.spirent.itest_backend.config.AbstractIntegrationTest;
import com.spirent.itest_backend.testdata.BirdTestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "classpath:test-cleanup.sql",
executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class BirdsControllerE2ETest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/birds";
    }

    @Test
    void shouldCreateBirdSuccessfully() {
        // Given
        BirdRequest birdRequest = BirdTestDataBuilder.robinRequest();

        // When
        ResponseEntity<Bird> response = restTemplate.postForEntity(baseUrl, birdRequest, Bird.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("American Robin");
        assertThat(response.getBody().getColor()).isEqualTo("Red");
        assertThat(response.getBody().getWeight()).isEqualTo(77.5);
        assertThat(response.getBody().getHeight()).isEqualTo(25.0);
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getCreatedAt()).isNotNull();
    }

    @Test
    void shouldGetBirdByIdSuccessfully() {
        // Given
        BirdRequest birdRequest = BirdTestDataBuilder.robinRequest();
        ResponseEntity<Bird> createResponse = restTemplate.postForEntity(baseUrl, birdRequest, Bird.class);
        Long birdId = createResponse.getBody().getId();

        // When
        ResponseEntity<Bird> response = restTemplate.getForEntity(baseUrl + "/" + birdId, Bird.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(birdId);
        assertThat(response.getBody().getName()).isEqualTo("American Robin");
        assertThat(response.getBody().getColor()).isEqualTo("Red");
    }

    @Test
    void shouldReturn404WhenBirdNotFound() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/999", String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldUpdateBirdSuccessfully() {
        // Given
        BirdRequest birdRequest = BirdTestDataBuilder.robinRequest();
        ResponseEntity<Bird> createResponse = restTemplate.postForEntity(baseUrl, birdRequest, Bird.class);
        Long birdId = createResponse.getBody().getId();

        UpdateBirdRequest updateRequest = BirdTestDataBuilder.updateBirdRequest();
        updateRequest.setName("Updated Robin");
        updateRequest.setColor("Orange");
        updateRequest.setWeight(80.0);
        updateRequest.setHeight(26.0);

        // When
        ResponseEntity<Bird> response = restTemplate.exchange(
                baseUrl + "/" + birdId,
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
                Bird.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(birdId);
        assertThat(response.getBody().getName()).isEqualTo("Updated Robin");
        assertThat(response.getBody().getColor()).isEqualTo("Orange");
        assertThat(response.getBody().getWeight()).isEqualTo(80.0);
        assertThat(response.getBody().getHeight()).isEqualTo(26.0);
        assertThat(response.getBody().getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldUpdateBirdPartially() {
        // Given
        BirdRequest birdRequest = BirdTestDataBuilder.robinRequest();
        ResponseEntity<Bird> createResponse = restTemplate.postForEntity(baseUrl, birdRequest, Bird.class);
        Long birdId = createResponse.getBody().getId();

        UpdateBirdRequest updateRequest = BirdTestDataBuilder.updateBirdRequest();
        updateRequest.setName("Partially Updated Robin");
        updateRequest.setColor(null); // Don't update color
        updateRequest.setWeight(null); // Don't update weight
        updateRequest.setHeight(null); // Don't update height

        // When
        ResponseEntity<Bird> response = restTemplate.exchange(
                baseUrl + "/" + birdId,
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
                Bird.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(birdId);
        assertThat(response.getBody().getName()).isEqualTo("Partially Updated Robin");
        assertThat(response.getBody().getColor()).isEqualTo("Red");
        assertThat(response.getBody().getWeight()).isEqualTo(77.5);
        assertThat(response.getBody().getHeight()).isEqualTo(25.0);
    }

    @Test
    void shouldDeleteBirdSuccessfully() {
        // Given
        BirdRequest birdRequest = BirdTestDataBuilder.robinRequest();
        ResponseEntity<Bird> createResponse = restTemplate.postForEntity(baseUrl, birdRequest, Bird.class);
        Long birdId = createResponse.getBody().getId();

        // When
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/" + birdId,
                HttpMethod.DELETE,
                null,
                Void.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify bird is deleted
        ResponseEntity<String> getResponse = restTemplate.getForEntity(baseUrl + "/" + birdId, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentBird() {
        // When
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/999",
                HttpMethod.DELETE,
                null,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldFindBirdsWithFilters() {
        // Given - Create multiple birds
        restTemplate.postForEntity(baseUrl, BirdTestDataBuilder.robinRequest(), Bird.class);
        restTemplate.postForEntity(baseUrl, BirdTestDataBuilder.cardinalRequest(), Bird.class);
        restTemplate.postForEntity(baseUrl, BirdTestDataBuilder.blueJayRequest(), Bird.class);
        
        // Create a second robin bird for testing
        BirdRequest testRobinRequest = new BirdRequest();
        testRobinRequest.setName("Test Robin");
        testRobinRequest.setColor("Brown");
        testRobinRequest.setWeight(60.0);
        testRobinRequest.setHeight(20.0);
        restTemplate.postForEntity(baseUrl, testRobinRequest, Bird.class);

        // When - Search by name
        ResponseEntity<PaginatedBirds> response = restTemplate.getForEntity(
                baseUrl + "?name=robin", PaginatedBirds.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(2);
        assertThat(response.getBody().getContent().get(0).getName()).contains("American Robin");
        assertThat(response.getBody().getContent().get(1).getName()).contains("Test Robin");
    }

    @Test
    void shouldFindBirdsWithColorFilter() {
        // Given - Create multiple birds
        restTemplate.postForEntity(baseUrl, BirdTestDataBuilder.robinRequest(), Bird.class);
        restTemplate.postForEntity(baseUrl, BirdTestDataBuilder.cardinalRequest(), Bird.class);
        restTemplate.postForEntity(baseUrl, BirdTestDataBuilder.blueJayRequest(), Bird.class);
        
        // Create additional red birds for testing
        BirdRequest redBird1 = new BirdRequest();
        redBird1.setName("Red Finch");
        redBird1.setColor("Red");
        redBird1.setWeight(15.0);
        redBird1.setHeight(12.0);
        restTemplate.postForEntity(baseUrl, redBird1, Bird.class);
        
        BirdRequest redBird2 = new BirdRequest();
        redBird2.setName("Red Warbler");
        redBird2.setColor("Red");
        redBird2.setWeight(20.0);
        redBird2.setHeight(15.0);
        restTemplate.postForEntity(baseUrl, redBird2, Bird.class);

        // When - Search by color
        ResponseEntity<PaginatedBirds> response = restTemplate.getForEntity(
                baseUrl + "?color=red", PaginatedBirds.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(4);
        assertThat(response.getBody().getContent()).allMatch(bird -> bird.getColor().equalsIgnoreCase("red"));
    }

    @Test
    void shouldFindBirdsWithWeightRange() {
        // Given - Create multiple birds
        restTemplate.postForEntity(baseUrl, BirdTestDataBuilder.robinRequest(), Bird.class);
        restTemplate.postForEntity(baseUrl, BirdTestDataBuilder.cardinalRequest(), Bird.class);
        restTemplate.postForEntity(baseUrl, BirdTestDataBuilder.blueJayRequest(), Bird.class);

        // When - Search by weight range
        ResponseEntity<PaginatedBirds> response = restTemplate.getForEntity(
                baseUrl + "?weightMin=40&weightMax=80", PaginatedBirds.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(2); // Robin (77.5) and Cardinal (45.0)
        assertThat(response.getBody().getContent()).allMatch(bird -> 
                bird.getWeight() >= 40.0 && bird.getWeight() <= 80.0);
    }

    @Test
    void shouldFindBirdsWithHeightRange() {
        // Given - Create multiple birds
        restTemplate.postForEntity(baseUrl, BirdTestDataBuilder.robinRequest(), Bird.class);
        restTemplate.postForEntity(baseUrl, BirdTestDataBuilder.cardinalRequest(), Bird.class);
        restTemplate.postForEntity(baseUrl, BirdTestDataBuilder.blueJayRequest(), Bird.class);

        // When - Search by height range
        ResponseEntity<PaginatedBirds> response = restTemplate.getForEntity(
                baseUrl + "?heightMin=20&heightMax=25", PaginatedBirds.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(2); // Robin (25.0) and Cardinal (22.0)
        assertThat(response.getBody().getContent()).allMatch(bird -> 
                bird.getHeight() >= 20.0 && bird.getHeight() <= 25.0);
    }

    @Test
    void shouldFindBirdsWithPagination() {
        // Given - Create multiple birds
        restTemplate.postForEntity(baseUrl, BirdTestDataBuilder.robinRequest(), Bird.class);
        restTemplate.postForEntity(baseUrl, BirdTestDataBuilder.cardinalRequest(), Bird.class);
        restTemplate.postForEntity(baseUrl, BirdTestDataBuilder.blueJayRequest(), Bird.class);

        // When - Get first page with size 2
        ResponseEntity<PaginatedBirds> response = restTemplate.getForEntity(
                baseUrl + "?page=0&size=2", PaginatedBirds.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(2);
        assertThat(response.getBody().getPage()).isEqualTo(0);
        assertThat(response.getBody().getSize()).isEqualTo(2);
        assertThat(response.getBody().getTotalElements()).isEqualTo(3);
        assertThat(response.getBody().getTotalPages()).isEqualTo(2);
        assertThat(response.getBody().getFirst()).isTrue();
        assertThat(response.getBody().getLast()).isFalse();
    }

    @Test
    void shouldFindBirdsWithSorting() {
        // Given - Create multiple birds
        restTemplate.postForEntity(baseUrl, BirdTestDataBuilder.robinRequest(), Bird.class);
        restTemplate.postForEntity(baseUrl, BirdTestDataBuilder.cardinalRequest(), Bird.class);
        restTemplate.postForEntity(baseUrl, BirdTestDataBuilder.blueJayRequest(), Bird.class);

        // When - Sort by name descending
        ResponseEntity<PaginatedBirds> response = restTemplate.getForEntity(
                baseUrl + "?sort=name&direction=desc", PaginatedBirds.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(3);
        
        // Verify sorting (should be in descending order by name)
        var birds = response.getBody().getContent();
        assertThat(birds.get(0).getName()).isEqualTo("Northern Cardinal");
        assertThat(birds.get(1).getName()).isEqualTo("Blue Jay");
        assertThat(birds.get(2).getName()).isEqualTo("American Robin");
    }

    @Test
    void shouldFindBirdsWithMultipleFilters() {
        // Given - Create multiple birds
        restTemplate.postForEntity(baseUrl, BirdTestDataBuilder.robinRequest(), Bird.class);
        restTemplate.postForEntity(baseUrl, BirdTestDataBuilder.cardinalRequest(), Bird.class);
        restTemplate.postForEntity(baseUrl, BirdTestDataBuilder.blueJayRequest(), Bird.class);

        // When - Search with multiple filters
        ResponseEntity<PaginatedBirds> response = restTemplate.getForEntity(
                baseUrl + "?color=red&weightMin=40&weightMax=80", PaginatedBirds.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(2); // Robin and Cardinal
        assertThat(response.getBody().getContent()).allMatch(bird -> 
                bird.getColor().equalsIgnoreCase("red") && 
                bird.getWeight() >= 40.0 && bird.getWeight() <= 80.0);
    }

    @Test
    void shouldReturnEmptyResultWhenNoBirdsMatch() {
        // Given - Create birds
        restTemplate.postForEntity(baseUrl, BirdTestDataBuilder.robinRequest(), Bird.class);
        restTemplate.postForEntity(baseUrl, BirdTestDataBuilder.cardinalRequest(), Bird.class);

        // When - Search for non-existent bird
        ResponseEntity<PaginatedBirds> response = restTemplate.getForEntity(
                baseUrl + "?name=eagle", PaginatedBirds.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isEmpty();
        assertThat(response.getBody().getTotalElements()).isEqualTo(0);
    }
}
