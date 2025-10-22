CREATE SCHEMA IF NOT EXISTS birds;

DROP TABLE IF EXISTS birds.sightings;
DROP TABLE IF EXISTS birds.birds;
DROP INDEX IF EXISTS idx_sightings_bird_id;

CREATE TABLE IF NOT EXISTS birds.birds (
   id BIGSERIAL PRIMARY KEY,
   name VARCHAR(255) NOT NULL,
   color VARCHAR(255) NOT NULL,
   weight DOUBLE PRECISION NOT NULL,
   height DOUBLE PRECISION NOT NULL,
   created_at TIMESTAMP NOT NULL default now(),
   updated_at TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS birds.sightings (
    id BIGSERIAL PRIMARY KEY,
    bird_id BIGSERIAL NOT NULL,
    location VARCHAR(255) NOT NULL,
    date_time TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL default now(),
    updated_at TIMESTAMP NULL,

    CONSTRAINT fk_bird
        FOREIGN KEY (bird_id)
        REFERENCES birds.birds (id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_sightings_bird_id ON birds.sightings (bird_id);
