package org.example.backend_tunisiahub.dto.event;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventSlotRecommendationRequest {
    private String date;
    private String time;
    private String type;
    private String target_audience;
}
