package org.example.backend_tunisiahub.carpooling.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class TripCreateRequest {

    @NotBlank(message = "departurePoint is required")
    private String departurePoint;

    @NotBlank(message = "destination is required")
    private String destination;

    @NotNull(message = "departureDateTime is required")
    @Future(message = "departureDateTime must be in the future")
    private LocalDateTime departureDateTime;

    @DecimalMin(value = "0.0", inclusive = true, message = "price must be >= 0")
    private BigDecimal price;

    @Min(value = 1, message = "seatsTotal must be > 0")
    private int seatsTotal;
}
