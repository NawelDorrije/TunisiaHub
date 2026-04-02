ALTER TABLE trips ADD COLUMN departure VARCHAR(255) NULL;

UPDATE trips
SET departure = CASE
    WHEN departure_point IS NOT NULL AND departure_point <> '' THEN departure_point
    ELSE departure
END;

ALTER TABLE trips DROP COLUMN departure_point;
ALTER TABLE trips DROP COLUMN pickup_point;
ALTER TABLE trips DROP COLUMN dropoff_point;
