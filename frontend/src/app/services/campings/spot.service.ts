import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Spot } from '../../models/campings/spot';

@Injectable({ providedIn: 'root' })
export class SpotService {
  private API_URL = 'http://localhost:8089/api/spots';

  constructor(private http: HttpClient) {}

  // ── CRUD ──────────────────────────────────────────────
  getAllSpots(): Observable<Spot[]> {
    return this.http.get<Spot[]>(this.API_URL);
  }

  getSpotById(id: number): Observable<Spot> {
    return this.http.get<Spot>(`${this.API_URL}/${id}`);
  }

  getSpotsByCamping(campingId: number): Observable<Spot[]> {
    return this.http.get<Spot[]>(`${this.API_URL}/camping/${campingId}`);
  }

  createSpot(formData: FormData): Observable<Spot> {
    return this.http.post<Spot>(this.API_URL, formData);
  }

  updateSpot(id: number, formData: FormData): Observable<Spot> {
    return this.http.put<Spot>(`${this.API_URL}/${id}`, formData);
  }

  deleteSpot(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  // ── FILTERS ───────────────────────────────────────────
  getAvailableByDates(campingId: number, checkIn: string, checkOut: string): Observable<Spot[]> {
    const params = new HttpParams().set('checkIn', checkIn).set('checkOut', checkOut);
    return this.http.get<Spot[]>(`${this.API_URL}/camping/${campingId}/available`, { params });
  }

  searchSpots(campingId: number, filters: {
    checkIn: string; checkOut: string; type?: string; viewType?: string;
    hasShade?: boolean; accessibleForDisabled?: boolean;
    minCapacity?: number; maxPrice?: number;
  }): Observable<Spot[]> {
    let params = new HttpParams()
      .set('checkIn', filters.checkIn)
      .set('checkOut', filters.checkOut);
    if (filters.type) params = params.set('type', filters.type);
    if (filters.viewType) params = params.set('viewType', filters.viewType);
    if (filters.hasShade !== undefined) params = params.set('hasShade', filters.hasShade.toString());
    if (filters.accessibleForDisabled !== undefined) params = params.set('accessibleForDisabled', filters.accessibleForDisabled.toString());
    if (filters.minCapacity) params = params.set('minCapacity', filters.minCapacity.toString());
    if (filters.maxPrice) params = params.set('maxPrice', filters.maxPrice.toString());
    return this.http.get<Spot[]>(`${this.API_URL}/camping/${campingId}/search`, { params });
  }

  getByStatus(campingId: number, status: string): Observable<Spot[]> {
    return this.http.get<Spot[]>(`${this.API_URL}/camping/${campingId}/status`, {
      params: new HttpParams().set('status', status)
    });
  }

  getByType(campingId: number, type: string): Observable<Spot[]> {
    return this.http.get<Spot[]>(`${this.API_URL}/camping/${campingId}/type`, {
      params: new HttpParams().set('type', type)
    });
  }

  getByPriceRange(campingId: number, min: number, max: number): Observable<Spot[]> {
    const params = new HttpParams().set('min', min.toString()).set('max', max.toString());
    return this.http.get<Spot[]>(`${this.API_URL}/camping/${campingId}/price-range`, { params });
  }

  buildFormData(spot: Partial<Spot>, photos?: File[]): FormData {
    const formData = new FormData();
    formData.append('spot', new Blob([JSON.stringify(spot)], { type: 'application/json' }));
    if (photos) {
      photos.forEach(photo => formData.append('photos', photo));
    }
    return formData;
  }
}
