package org.example.backend_tunisiahub.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RecommendedSlotDto {
    private String date;
    private String time;
    private int score;
    private String engagement;
    private String reason;
    private int improvementPercent;
}
