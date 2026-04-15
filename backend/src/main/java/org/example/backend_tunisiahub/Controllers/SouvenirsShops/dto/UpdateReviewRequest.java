package org.example.backend_tunisiahub.Controllers.SouvenirsShops.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateReviewRequest {
    private Integer rating;
    private String comment;
}
