package com.spirent.itest_backend.testdata;

import com.spirent.birdwatch.model.Bird;
import com.spirent.birdwatch.model.BirdRequest;
import com.spirent.birdwatch.model.UpdateBirdRequest;

import java.time.OffsetDateTime;

public class BirdTestDataBuilder {

    public static BirdRequest robinRequest() {
        BirdRequest request = new BirdRequest();
        request.setName("American Robin");
        request.setColor("Red");
        request.setWeight(77.5);
        request.setHeight(25.0);
        return request;
    }

    public static BirdRequest cardinalRequest() {
        BirdRequest request = new BirdRequest();
        request.setName("Northern Cardinal");
        request.setColor("Red");
        request.setWeight(45.0);
        request.setHeight(22.0);
        return request;
    }

    public static BirdRequest blueJayRequest() {
        BirdRequest request = new BirdRequest();
        request.setName("Blue Jay");
        request.setColor("Blue");
        request.setWeight(85.0);
        request.setHeight(30.0);
        return request;
    }

    public static UpdateBirdRequest updateBirdRequest() {
        UpdateBirdRequest request = new UpdateBirdRequest();
        request.setName("Updated Bird Name");
        request.setColor("Updated Color");
        request.setWeight(100.0);
        request.setHeight(35.0);
        return request;
    }

    public static Bird robin() {
        Bird bird = new Bird();
        bird.setId(1L);
        bird.setName("American Robin");
        bird.setColor("Red");
        bird.setWeight(77.5);
        bird.setHeight(25.0);
        bird.setCreatedAt(OffsetDateTime.now());
        bird.setUpdatedAt(OffsetDateTime.now());
        return bird;
    }

    public static Bird cardinal() {
        Bird bird = new Bird();
        bird.setId(2L);
        bird.setName("Northern Cardinal");
        bird.setColor("Red");
        bird.setWeight(45.0);
        bird.setHeight(22.0);
        bird.setCreatedAt(OffsetDateTime.now());
        bird.setUpdatedAt(OffsetDateTime.now());
        return bird;
    }

    public static Bird blueJay() {
        Bird bird = new Bird();
        bird.setId(3L);
        bird.setName("Blue Jay");
        bird.setColor("Blue");
        bird.setWeight(85.0);
        bird.setHeight(30.0);
        bird.setCreatedAt(OffsetDateTime.now());
        bird.setUpdatedAt(OffsetDateTime.now());
        return bird;
    }
}
