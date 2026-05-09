package org.example.backend_tunisiahub.Services.Carpooling;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DriverReviewSummary {

    private double averageRating;

    private long reviewsCount;
}
