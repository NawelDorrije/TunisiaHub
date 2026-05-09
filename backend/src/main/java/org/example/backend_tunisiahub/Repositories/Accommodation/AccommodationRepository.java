package org.example.backend_tunisiahub.Repositories.Accommodation;

import org.example.backend_tunisiahub.Entities.Accommodation.Accommodation;
import org.springframework.data.jpa.repository.JpaRepository;
<<<<<<< HEAD

import java.util.List;

public interface AccommodationRepository extends JpaRepository<Accommodation,Long> {
    List<Accommodation> findByType(String type);
    List<Accommodation> findByPriceLessThanEqual(double price);
    List<Accommodation> findByCapaciteGreaterThanEqual(int capacite);
    List<Accommodation> findByTitleContainingIgnoreCase(String keyword);
=======
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface AccommodationRepository extends JpaRepository<Accommodation,Long>, JpaSpecificationExecutor<Accommodation> {
  List<Accommodation> findByType(String type);
  List<Accommodation> findByPriceLessThanEqual(double price);
  List<Accommodation> findByCapaciteGreaterThanEqual(int capacite);
  List<Accommodation> findByTitleContainingIgnoreCase(String keyword);
>>>>>>> origin/feature/integrated-app-event
}
