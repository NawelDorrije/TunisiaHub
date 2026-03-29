import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Lieu, ActiviteLieu } from '../models/trendy-places/lieu.model';

@Injectable({
  providedIn: 'root'
})
export class TrendyPlacesService {
  private BASE_URL = 'http://localhost:8089';

  constructor(private http: HttpClient) {}

  getAllLieux(): Observable<Lieu[]> {
    return this.http.get<Lieu[]>(`${this.BASE_URL}/api/lieux`);
  }

  getLieuById(id: number): Observable<Lieu> {
    return this.http.get<Lieu>(`${this.BASE_URL}/api/lieux/${id}`);
  }

  getActivitesByLieu(lieuId: number): Observable<ActiviteLieu[]> {
    return this.http.get<ActiviteLieu[]>(`${this.BASE_URL}/api/activites/lieu/${lieuId}`);
  }
}