import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Event } from '../../../models/events/event.model';
import { AiEventChatResponse } from '../../../models/events/ai-event-chat.model';
import { catchError, of } from 'rxjs';
//import { WeatherDTO } from '../../../models/weather.model';
//import { Reservation } from '../../../models/reservation.model';
import {
  WeatherDTO,
  ForecastResponse
} from '../../../models/weather.model';

@Injectable({
  providedIn: 'root'
})
export class EventService {

   private apiUrl = 'http://localhost:8089/event';
   private apiEventsUrl = 'http://localhost:8089/api/events';
   private weatherUrl = 'http://localhost:8089/weather';

  constructor(private http: HttpClient) {}

  getAllEvents(): Observable<Event[]> {
    return this.http.get<Event[]>(`${this.apiUrl}/all`);
  }

  getEventById(id: number): Observable<Event> {
    return this.http.get<Event>(`${this.apiUrl}/${id}`);
  }

  addEvent(event: Event): Observable<Event> {
    return this.http.post<Event>(this.apiEventsUrl, event);
  }

  updateEvent(event: Event): Observable<Event> {
    return this.http.put<Event>(`${this.apiUrl}/update`, event);
  }

  deleteEvent(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/delete/${id}`);
  }
  reserveEvent(userId: number, eventId: number): Observable<any> {
  return this.http.post<any>(
    `http://localhost:8089/api/reservations/reserve?userId=${userId}&eventId=${eventId}`,
    {}
  );
}
createPendingReservation(userId: number, eventId: number) {
  return this.http.post(
    `http://localhost:8089/api/reservations/create-pending?userId=${userId}&eventId=${eventId}`,
    {}
  );
}
  confirmReservation(reservationId: number) {
    return this.http.post(
      `http://localhost:8089/api/reservations/confirm/${reservationId}`,
      {}
    );
  }


 getUserReservation(userId: number, eventId: number) {
  return this.http.get<any>(
    `http://localhost:8089/api/reservations/user/${userId}/event/${eventId}`
  ).pipe(
    catchError(() => of(null))
  );
}



getWeather(lat: number, lon: number) {
  return this.http.get<WeatherDTO>(
    `/weather?lat=${lat}&lon=${lon}`
  );
}
getWeeklyWeather(lat: number, lon: number) {
  return this.http.get<any>(
    `http://localhost:8089/weather/weekly?lat=${lat}&lon=${lon}`
  );
}
 // 📅 météo sur 7 jours
  getWeeklyForecast(lat: number, lon: number): Observable<ForecastResponse> {
    return this.http.get<ForecastResponse>(
      `${this.weatherUrl}/weekly?lat=${lat}&lon=${lon}`
    );
  }
  searchEvents(keyword: string) {
  return this.http.get<Event[]>(`http://localhost:8089/event/search?keyword=${keyword}`);
}

filterByType(type: string) {
  return this.http.get<Event[]>(`http://localhost:8089/event/filter?type=${type}`);
}

  chatWithAi(message: string): Observable<AiEventChatResponse> {
    return this.http.post<AiEventChatResponse>('http://localhost:8089/ai/events/chat', {
      message
    }).pipe(
      catchError((err) => {
        console.error('AI ERROR:', err);
        return of({
          message: 'Backend error: ' + (err?.error?.message || err?.message || 'Unknown error'),
          events: []
        });
      })
    );
  }


}
