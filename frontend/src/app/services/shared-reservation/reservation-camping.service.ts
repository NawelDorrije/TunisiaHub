import { Injectable } from '@angular/core';
import { Reservation } from '../../models/shared-reservation/reservation';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ReservationCampingService {

 private BASE_URL = 'http://localhost:8089/api/reservations';

  constructor(private http: HttpClient) { }

  // GET all reservations
  getAllReservations(): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(this.BASE_URL);
  }

  // GET reservation by ID
  getReservationById(id: number): Observable<Reservation> {
    return this.http.get<Reservation>(`${this.BASE_URL}/${id}`);
  }

  // POST - create a reservation
  createReservation(reservation: Reservation): Observable<Reservation> {
    return this.http.post<Reservation>(this.BASE_URL, reservation);
  }

  // PUT - update a reservation
  updateReservation(reservation: Reservation): Observable<Reservation> {
    return this.http.put<Reservation>(this.BASE_URL, reservation);
  }

  // DELETE - delete a reservation
  deleteReservation(id: number): Observable<void> {
    return this.http.delete<void>(`${this.BASE_URL}/${id}`);
  }
}
