import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ReservationRequest, ReservationResponse, ReservedDateRange } from '../../../models/accommodations/reservation.model';
import { FeedbackRequest, FeedbackResponse } from '../../../models/accommodations/feedback.model';
import { AccommodationStats } from '../../../models/accommodations/statistics.model';

@Injectable({
  providedIn: 'root'
})
export class ReservationService {

  private baseUrl = 'http://localhost:8089/api/accommodation-reservations';

  constructor(private http: HttpClient) {}

  addReservation(accommodationId: number, reservation: ReservationRequest): Observable<ReservationResponse> {
    return this.http.post<ReservationResponse>(`${this.baseUrl}/add/${accommodationId}`, reservation);
  }

  getReservedDates(accommodationId: number): Observable<ReservedDateRange[]> {
    return this.http.get<ReservedDateRange[]>(`${this.baseUrl}/reserved-dates/${accommodationId}`);
  }

  checkAvailability(accommodationId: number, startDate: string, endDate: string): Observable<boolean> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.get<boolean>(`${this.baseUrl}/check-availability/${accommodationId}`, { params });
  }

  cancelReservation(reservationId: number): Observable<ReservationResponse> {
    return this.http.put<ReservationResponse>(`${this.baseUrl}/cancel/${reservationId}`, {});
  }
  getMyReservations(): Observable<ReservationResponse[]> {
  return this.http.get<ReservationResponse[]>(`${this.baseUrl}/my-reservations`);
}

editReservation(reservationId: number, reservation: ReservationRequest): Observable<ReservationResponse> {
  return this.http.put<ReservationResponse>(`${this.baseUrl}/edit/${reservationId}`, reservation);
}
submitFeedback(
  accommodationId: number,
  reservationId: number,
  feedback: FeedbackRequest
): Observable<FeedbackResponse> {
  return this.http.post<FeedbackResponse>(
    `http://localhost:8089/api/feedback/add/${accommodationId}/${reservationId}`,
    feedback
  );
}

hasFeedback(reservationId: number): Observable<boolean> {
  return this.http.get<boolean>(
    `http://localhost:8089/api/feedback/has-feedback/${reservationId}`
  );
}
getStatistics(): Observable<AccommodationStats> {
  return this.http.get<AccommodationStats>(
    'http://localhost:8089/api/accommodation-reservations/statistics'
  );
}
}