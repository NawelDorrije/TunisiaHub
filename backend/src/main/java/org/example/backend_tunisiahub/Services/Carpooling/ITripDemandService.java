package org.example.backend_tunisiahub.Services.Carpooling;

import java.time.LocalDate;

public interface ITripDemandService {

    TripDemandAlert retrieveDemandAlert(String departure, String destination, LocalDate dateFrom, LocalDate dateTo);
}
