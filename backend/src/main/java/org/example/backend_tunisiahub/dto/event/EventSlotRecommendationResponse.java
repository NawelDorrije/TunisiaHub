package org.example.backend_tunisiahub.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class EventSlotRecommendationResponse {
    private int originalScore;
    private String originalEngagement;
    private String originalReason;
    private List<RecommendedSlotDto> recommendations;
}
