package org.example.backend_tunisiahub.Repositories.Carpooling;

import org.example.backend_tunisiahub.Entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarpoolingReviewRepository extends JpaRepository<Review, Long> {

    Review findByReservationId(Long reservationId);

    @Query("select r from ReservationReview r where r.reservation.trip.driver.id = :driverId order by r.date desc")
    List<Review> findByDriverId(@Param("driverId") Long driverId);

    @Query("select r from ReservationReview r where r.reservation.trip is not null and r.rating <= :maxRating order by r.date desc")
    List<Review> findBadReviews(@Param("maxRating") Integer maxRating);

    @Query("select avg(r.rating) from ReservationReview r where r.reservation.trip.driver.id = :driverId")
    Double findAverageRatingByDriverId(@Param("driverId") Long driverId);

    long countByReservation_Trip_Driver_Id(Long driverId);
}
