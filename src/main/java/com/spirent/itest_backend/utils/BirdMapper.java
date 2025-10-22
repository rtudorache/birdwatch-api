package com.spirent.itest_backend.utils;

import com.spirent.birdwatch.model.Bird;
import com.spirent.birdwatch.model.BirdRequest;
import com.spirent.itest_backend.repository.entity.BirdEntity;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class BirdMapper {

    private final BirdSightingsMapper birdSightingsMapper;

    public BirdMapper(BirdSightingsMapper birdSightingsMapper) {
        this.birdSightingsMapper = birdSightingsMapper;
    }

    public Bird entityToBird(@NonNull final BirdEntity birdEntity) {
        Bird bird = new Bird();
        bird.setId(birdEntity.getId());
        bird.setName(birdEntity.getName());
        bird.setColor(birdEntity.getColor());
        bird.setHeight(birdEntity.getHeight());
        bird.setWeight(birdEntity.getWeight());
        if (birdEntity.getSightings() != null) {
            birdEntity.getSightings().forEach(sightingEntity -> bird.addSightingsItem(birdSightingsMapper.entityToSighting(sightingEntity)));
        }
        bird.setCreatedAt(birdEntity.getCreatedAt());
        bird.setUpdatedAt(birdEntity.getUpdatedAt());

        return bird;
    }

    public BirdEntity birdRequestToEntity(@NonNull final BirdRequest birdRequest) {
        BirdEntity birdEntity = new BirdEntity();

        birdEntity.setColor(birdRequest.getColor());
        birdEntity.setHeight(birdRequest.getHeight());
        birdEntity.setWeight(birdRequest.getWeight());
        birdEntity.setName(birdRequest.getName());
        birdEntity.setCreatedAt(OffsetDateTime.now());

        return birdEntity;
    }
}
