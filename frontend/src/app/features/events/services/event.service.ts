import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Event } from '../../../models/events/event.model';
import { AiEventChatResponse } from '../../../models/events/ai-event-chat.model';
import { catchError, map, of } from 'rxjs';
import {
  EventRecommendationRequest,
  EventRecommendationResponse
} from '../../../models/events/event-recommendation.model';
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

  private readonly backendBaseUrl = 'http://localhost:8089';
   private apiUrl = 'http://localhost:8089/event';
   private apiEventsUrl = 'http://localhost:8089/api/events';
  private weatherUrl = 'http://localhost:8089/weather';
  private recommendationUrl = 'http://localhost:8089/events/recommendation';

  constructor(private http: HttpClient) {}

  getAllEvents(): Observable<Event[]> {
    return this.http.get<Event[]>(`${this.apiUrl}/all`).pipe(
      map((events) => events.map((event) => this.normalizeEventImageUrl(event)))
    );
  }

  getEventById(id: number): Observable<Event> {
    return this.http.get<Event>(`${this.apiUrl}/${id}`).pipe(
      map((event) => this.normalizeEventImageUrl(event))
    );
  }

  addEvent(event: Event): Observable<Event> {
    return this.http.post<Event>(this.apiEventsUrl, event).pipe(
      map((createdEvent) => this.normalizeEventImageUrl(createdEvent))
    );
  }

  updateEvent(event: Event): Observable<Event> {
    return this.http.put<Event>(`${this.apiUrl}/update`, event);
  }

  deleteEvent(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/delete/${id}`);
  }
  reserveEvent(userId: number, eventId: number): Observable<any> {
  return this.http.post<any>(
    `http://localhost:8089/api/event-reservations/reserve?userId=${userId}&eventId=${eventId}`,
    {}
  );
}
createPendingReservation(userId: number, eventId: number) {
  return this.http.post(
    `http://localhost:8089/api/event-reservations/create-pending?userId=${userId}&eventId=${eventId}`,
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
    `http://localhost:8089/api/event-reservations/user/${userId}/event/${eventId}`
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
  return this.http.get<Event[]>(`http://localhost:8089/event/search?keyword=${keyword}`).pipe(
    map((events) => events.map((event) => this.normalizeEventImageUrl(event)))
  );
}

filterByType(type: string) {
  return this.http.get<Event[]>(`http://localhost:8089/event/filter?type=${type}`).pipe(
    map((events) => events.map((event) => this.normalizeEventImageUrl(event)))
  );
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

  getEventRecommendations(payload: EventRecommendationRequest): Observable<EventRecommendationResponse> {
    return this.http.post<EventRecommendationResponse>(this.recommendationUrl, payload);
  }
getShareDebug(id: number): Observable<any> {
  return this.http.get<any>(`http://localhost:8089/share/event/${id}/debug`);
}

publishToFacebook(eventId: number): Observable<string> {
  return this.http.post(`http://localhost:8089/event/${eventId}/publish-facebook`, null, {
    responseType: 'text'
  });
}

  private normalizeEventImageUrl(event: Event): Event {
    if (!event?.image) {
      return event;
    }

    const normalizedImage = this.resolveImageUrl(event.image);
    if (!normalizedImage) {
      return event;
    }

    return { ...event, image: normalizedImage };
  }

  private resolveImageUrl(raw: string | undefined | null): string {
    const value = (raw ?? '').trim();
    if (!value) {
      return '';
    }

    // Keep inline/base64 or blob URLs untouched.
    if (value.startsWith('data:') || value.startsWith('blob:')) {
      return value;
    }

    const slashNormalized = value.replace(/\\/g, '/');
    const lower = slashNormalized.toLowerCase();
    const uploadsIdx = lower.indexOf('/uploads/');

    if (uploadsIdx >= 0) {
      return `${this.backendBaseUrl}${slashNormalized.substring(uploadsIdx)}`;
    }

    if (slashNormalized.startsWith('http://') || slashNormalized.startsWith('https://')) {
      return slashNormalized;
    }

    // If backend returns a protocol-relative localhost URL, keep HTTP to match backend config.
    if (slashNormalized.startsWith('//localhost') || slashNormalized.startsWith('//127.0.0.1')) {
      return `http:${slashNormalized}`;
    }

    // Common case after Windows-path normalization: //uploads/... should be /uploads/...
    if (lower.startsWith('//uploads/')) {
      return `${this.backendBaseUrl}${slashNormalized.substring(1)}`;
    }

    if (lower.startsWith('uploads/')) {
      return `${this.backendBaseUrl}/${slashNormalized}`;
    }

    if (slashNormalized.startsWith('/')) {
      return `${this.backendBaseUrl}${slashNormalized}`;
    }

    return `${this.backendBaseUrl}/${slashNormalized}`;
  }

}
