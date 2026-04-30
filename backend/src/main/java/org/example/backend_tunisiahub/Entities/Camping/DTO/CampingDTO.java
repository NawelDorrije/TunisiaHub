package org.example.backend_tunisiahub.Entities.Camping.DTO;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.backend_tunisiahub.Entities.Camping.Enums.CampingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CampingDTO {

    // Read-only: populated in responses
    Long id;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    // Owner reference
    @NotNull(message = "ownerId is required")
    Long ownerId;

    String ownerName; // populated on response only

    // Core fields
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    String name;

    @NotBlank(message = "Address is required")
    String address;

    @NotBlank(message = "Governorate is required")
    String governorate;

    @DecimalMin(value = "-90.0", message = "Latitude must be valid")
    @DecimalMax(value = "90.0", message = "Latitude must be valid")
    BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be valid")
    @DecimalMax(value = "180.0", message = "Longitude must be valid")
    BigDecimal longitude;

    BigDecimal averageRating;

    Integer numberOfSpots;

    @NotNull(message = "Maximum capacity is required")
    @Positive(message = "Maximum capacity must be positive")
    Integer maxCapacity;

    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be greater than or equal to 0")
    BigDecimal price;

    @NotNull(message = "Status is required")
    CampingStatus status;

    String rules;

    LocalTime checkInTime;
    LocalTime checkOutTime;


    String description;

    LocalDate startDate;
    LocalDate endDate;

    List<String> photos;
}
