package org.example.backend_tunisiahub.Controllers.SouvenirsShops.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateReviewRequest {
    private Integer rating;
    private String comment;
}
