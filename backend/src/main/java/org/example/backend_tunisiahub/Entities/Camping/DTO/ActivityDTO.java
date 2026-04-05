package org.example.backend_tunisiahub.Entities.Camping.DTO;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ActivityDTO {

    // Read-only — returned in responses, ignored on create
    Long id;

    @NotBlank(message = "Name is required")
    String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
    BigDecimal price;

    @Positive(message = "Duration must be positive")
    Double duration;

    Boolean active;

    // Camping is required — every activity must belong to a camping
    @NotNull(message = "campingId is required")
    Long campingId;

    // Spot is optional — activity may be specific to a spot or general to camping
    Long spotId;

    // Read-only — returned in responses for display
    String campingName;
    String spotName;
}