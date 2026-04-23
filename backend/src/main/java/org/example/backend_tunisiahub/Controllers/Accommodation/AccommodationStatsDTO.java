package org.example.backend_tunisiahub.Controllers.Accommodation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccommodationStatsDTO {

    // KPI cards
    long totalAccommodations;
    long totalReservations;
    double totalRevenue;
    double averageRating;
    long totalReviews;

    // Pie chart — type distribution
    Map<String, Long> accommodationsByType;

    // Donut chart — reservation status
    long confirmedReservations;
    long cancelledReservations;

    // Bar chart — price range distribution
    long under100;
    long between100and200;
    long between200and300;
    long above300;

    // Bar chart — capacity distribution
    long capacity1to2;
    long capacity3to5;
    long capacity6to10;
    long capacityAbove10;

    // Top profitable accommodations table
    List<Map<String, Object>> topProfitableAccommodations;

    // Bar chart — reservations per accommodation (top 5)
    List<Map<String, Object>> topReservedAccommodations;
}