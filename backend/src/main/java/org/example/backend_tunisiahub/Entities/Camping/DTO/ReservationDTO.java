package org.example.backend_tunisiahub.Entities.Camping.DTO;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.backend_tunisiahub.Entities.Camping.Enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReservationDTO {

    // Read-only
    Long id;
    BigDecimal totalPrice;
    ReservationStatus status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    // Input
    @NotNull(message = "userId is required")
    Long userId;

    @NotNull(message = "spotId is required")
    Long spotId;

    List<Long> activityIds; // optional

    @NotNull(message = "checkIn date is required")
    LocalDate checkIn;

    @NotNull(message = "checkOut date is required")
    LocalDate checkOut;

    @NotNull(message = "Number of guests is required")
    @Min(value = 1, message = "At least 1 guest required")
    Integer numberOfGuests;

    String notes;

    // Read-only (response enrichment)
    String spotName;
    String campingName;
    String userName;
    List<String> activityNames;
}