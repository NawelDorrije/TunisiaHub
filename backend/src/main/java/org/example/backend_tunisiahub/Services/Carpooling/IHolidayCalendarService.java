package org.example.backend_tunisiahub.Services.Carpooling;

import org.example.backend_tunisiahub.Entities.Carpooling.HolidayCalendar;

import java.util.List;

public interface IHolidayCalendarService {

    List<HolidayCalendar> retrieveHolidaysByYear(Integer year);
}
