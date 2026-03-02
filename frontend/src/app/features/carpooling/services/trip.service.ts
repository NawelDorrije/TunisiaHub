import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Trip, TripCreateRequest, TripSearchParams, TripUpdateRequest } from '../models/trip.model';

@Injectable({ providedIn: 'root' })
export class TripService {
  private readonly http = inject(HttpClient);
  private readonly endpoint = `${environment.apiUrl}/carpooling/trips`;

  listTrips(search?: TripSearchParams): Observable<Trip[]> {
    let params = new HttpParams();

    if (search?.departurePoint?.trim()) {
      params = params.set('departurePoint', search.departurePoint.trim());
    }

    if (search?.destination?.trim()) {
      params = params.set('destination', search.destination.trim());
    }

    if (search?.date?.trim()) {
      params = params.set('date', search.date.trim());
    }

    return this.http.get<Trip[]>(this.endpoint, { params });
  }

  getTripById(id: number): Observable<Trip> {
    return this.http.get<Trip>(`${this.endpoint}/${id}`);
  }

  createTrip(payload: TripCreateRequest): Observable<Trip> {
    return this.http.post<Trip>(this.endpoint, payload);
  }

  updateTrip(id: number, payload: TripUpdateRequest): Observable<Trip> {
    return this.http.put<Trip>(`${this.endpoint}/${id}`, payload);
  }

  getMyTrips(): Observable<Trip[]> {
    return this.http.get<Trip[]>(`${this.endpoint}/me`);
  }

  cancelTrip(id: number): Observable<Trip> {
    return this.http.patch<Trip>(`${this.endpoint}/${id}/cancel`, {});
  }
}
