import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Reservation } from '../../models/shared-reservation/reservation';

@Injectable({ providedIn: 'root' })
export class ReservationService {
  private API_URL = 'http://localhost:8089/api/reservations';

  constructor(private http: HttpClient) {}

  getAllReservations(): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(this.API_URL);
  }

  getReservationById(id: number): Observable<Reservation> {
    return this.http.get<Reservation>(`${this.API_URL}/${id}`);
  }

  getByUser(userId: number): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(`${this.API_URL}/user/${userId}`);
  }

  getBySpot(spotId: number): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(`${this.API_URL}/spot/${spotId}`);
  }

  getByStatus(status: string): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(`${this.API_URL}/status`, {
      params: new HttpParams().set('status', status)
    });
  }

  createReservation(reservation: Reservation): Observable<Reservation> {
    return this.http.post<Reservation>(this.API_URL, reservation);
  }

  updateStatus(id: number, status: string): Observable<Reservation> {
    return this.http.patch<Reservation>(`${this.API_URL}/${id}/status`, null, {
      params: new HttpParams().set('status', status)
    });
  }

  cancelReservation(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}/cancel`);
  }
}
