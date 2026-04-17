import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Event } from '../../../models/events/event.model';
import { catchError, of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class EventService {

   private apiUrl = 'http://localhost:8089/event';

  constructor(private http: HttpClient) {}

  getAllEvents(): Observable<Event[]> {
    return this.http.get<Event[]>(`${this.apiUrl}/all`);
  }

  getEventById(id: number): Observable<Event> {
    return this.http.get<Event>(`${this.apiUrl}/${id}`);
  }

  addEvent(event: Event): Observable<Event> {
    return this.http.post<Event>(`${this.apiUrl}/add`, event);
  }

  updateEvent(event: Event): Observable<Event> {
    return this.http.put<Event>(`${this.apiUrl}/update`, event);
  }

  deleteEvent(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/delete/${id}`);
  }
  reserveEvent(userId: number, eventId: number) {
  return this.http.post(
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
addReview(userId: number, reservationId: number, comment: string, rating: number) {
    return this.http.post(
      `http://localhost:8089/review/add?userId=${userId}&reservationId=${reservationId}&comment=${comment}&rating=${rating}`,
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
}