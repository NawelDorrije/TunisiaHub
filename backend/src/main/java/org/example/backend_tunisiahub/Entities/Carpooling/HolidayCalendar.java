package org.example.backend_tunisiahub.Entities.Carpooling;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HolidayCalendar {

    private Integer yearValue;

    private String holidayName;

    private LocalDate holidayDate;

    private String holidayType;

    private String holidayCategory;

    private Boolean criticalForCarpooling;

    private Boolean tentative;

    private String sourceUrl;

    private LocalDateTime syncedAt;
}
