package org.example.backend_tunisiahub.Entities.Camping.DTO;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.backend_tunisiahub.Entities.Camping.Enums.SpotStatus;
import org.example.backend_tunisiahub.Entities.Camping.Enums.SpotType;
import org.example.backend_tunisiahub.Entities.Camping.Enums.ViewType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SpotDTO {

    // Read-only fields
    Long id;
    LocalDateTime createdAt;

    // --- Camping reference ---
    @NotNull(message = "Camping ID is required")
    Long campingId;

    String campingName;

    // --- Core fields ---
    @NotBlank(message = "Name is required")
    @Size(max = 50, message = "Name must not exceed 50 characters")
    String name;

    @NotNull(message = "Type is required")
    SpotType type;

    @NotNull(message = "Capacity is required")
    @Positive(message = "Capacity must be positive")
    Integer capacity;

    @DecimalMin(
            value = "0.0",
            inclusive = false,
            message = "Area must be positive"
    )
    BigDecimal area;

    String description;

    @NotNull(message = "Base price is required")
    @DecimalMin(
            value = "0.0",
            inclusive = false,
            message = "Base price must be positive"
    )
    BigDecimal basePrice;

    @NotNull(message = "Status is required")
    SpotStatus status;

    BigDecimal positionX;
    BigDecimal positionY;

    ViewType viewType;

    @NotNull
    Boolean hasShade;

    @NotNull
    Boolean accessibleForDisabled;

    @NotNull
    Boolean active;

    List<String> photos;
}