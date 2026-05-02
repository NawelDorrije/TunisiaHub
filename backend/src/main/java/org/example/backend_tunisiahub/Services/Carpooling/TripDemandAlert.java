package org.example.backend_tunisiahub.Services.Carpooling;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TripDemandAlert {

    private String departure;
    private String destination;
    private String weekLabel;
    private String demandLevel;
    private Double predictedSeatsBooked;
    private Double referenceSeats;
    private Double predictedOccupancyRate;
    private Integer trainingSamples;
    private String modelName;
    private Boolean holidayCriticalWarning;
    private String passengerAlert;
    private String driverAlert;
    private String suggestedDateFrom;
    private String suggestedDateTo;
    private Double suggestedPredictedOccupancyRate;
}
