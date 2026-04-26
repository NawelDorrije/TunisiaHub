package org.example.backend_tunisiahub.Entities.Restaurant;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TableSelectionRequest {

    @NotNull(message = "Table ID is required")
    private Long tableId;

    // Optional: if you want to link directly during reservation creation
    private Long reservationId;   // can be null if used during creation
}
