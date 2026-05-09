package org.example.backend_tunisiahub.Entities.Camping.DTO;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.backend_tunisiahub.Entities.Camping.Enums.EquipmentCondition;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EquipementDTO {

    // Read-only
    Long id;

    @NotBlank(message = "Name is required")
    String name;

    @Size(max = 500)
    String description;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be zero or positive")
    Integer quantity;

    @NotNull(message = "Available is required")
    Boolean available;

    @NotNull(message = "Condition is required")
    EquipmentCondition condition;

    // Input
    @NotNull(message = "spotId is required")
    Long spotId;

    // Read-only (response)
    String spotName;
}