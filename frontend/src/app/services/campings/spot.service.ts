import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Spot } from '../../models/campings/spot';

@Injectable({
  providedIn: 'root'
})
export class SpotService {

  private BASE_URL = 'http://localhost:8089';
  private API_URL = this.BASE_URL + '/api/spots';

  constructor(private http: HttpClient) { }

  // GET all spots
  getAllSpots(): Observable<Spot[]> {
    return this.http.get<Spot[]>(this.API_URL);
  }

  // GET spot by id
  getSpotById(id: number): Observable<Spot> {
    return this.http.get<Spot>(`${this.API_URL}/${id}`);
  }

  // CREATE spot
  createSpot(spot: Spot): Observable<Spot> {
    return this.http.post<Spot>(this.API_URL, spot);
  }

  // UPDATE spot
  updateSpot(spot: Spot): Observable<Spot> {
    return this.http.put<Spot>(this.API_URL, spot);
  }

  // DELETE spot
  deleteSpot(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

}
