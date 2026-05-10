import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Camping } from '../../models/campings/camping';

@Injectable({ providedIn: 'root' })
export class CampingService {
  private BASE_URL = 'http://localhost:8089';
  private API_URL = `${this.BASE_URL}/api/campings`;

  constructor(private http: HttpClient) {}

  // ── CRUD ──────────────────────────────────────────────
  getAllCampings(): Observable<Camping[]> {
    return this.http.get<Camping[]>(this.API_URL);

  }
 

  getCampingById(id: number): Observable<Camping> {
    return this.http.get<Camping>(`${this.API_URL}/${id}`);
  }

  createCamping(formData: FormData): Observable<Camping> {
    return this.http.post<Camping>(this.API_URL, formData);
  }

  updateCamping(id: number, formData: FormData): Observable<Camping> {
    return this.http.put<Camping>(`${this.API_URL}/${id}`, formData);
  }

  deleteCamping(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  // ── FILTERS ───────────────────────────────────────────
  searchByKeyword(keyword: string): Observable<Camping[]> {
    return this.http.get<Camping[]>(`${this.API_URL}/search`, {
      params: new HttpParams().set('keyword', keyword)
    });
  }

  getAvailable(governorate?: string, maxPrice?: number, minCapacity?: number): Observable<Camping[]> {
    let params = new HttpParams();
    if (governorate) params = params.set('governorate', governorate);
    if (maxPrice) params = params.set('maxPrice', maxPrice.toString());
    if (minCapacity) params = params.set('minCapacity', minCapacity.toString());
    return this.http.get<Camping[]>(`${this.API_URL}/available`, { params });
  }

  getAvailableForDates(checkIn: string, checkOut: string, governorate?: string): Observable<Camping[]> {
    let params = new HttpParams().set('checkIn', checkIn).set('checkOut', checkOut);
    if (governorate) params = params.set('governorate', governorate);
    return this.http.get<Camping[]>(`${this.API_URL}/available-for-dates`, { params });
  }

  getByRating(minRating: number): Observable<Camping[]> {
    return this.http.get<Camping[]>(`${this.API_URL}/rating`, {
      params: new HttpParams().set('minRating', minRating.toString())
    });
  }

  getByOwner(ownerId: number): Observable<Camping[]> {
    return this.http.get<Camping[]>(`${this.API_URL}/owner/${ownerId}`);
  }

  getByStatus(status: string): Observable<Camping[]> {
    return this.http.get<Camping[]>(`${this.API_URL}/status`, {
      params: new HttpParams().set('status', status)
    });
  }

  getByGovernorate(governorate: string): Observable<Camping[]> {
    return this.http.get<Camping[]>(`${this.API_URL}/governorate`, {
      params: new HttpParams().set('governorate', governorate)
    });
  }

  buildFormData(camping: Partial<Camping>, photos?: File[]): FormData {
    const formData = new FormData();
    formData.append('camping', new Blob([JSON.stringify(camping)], { type: 'application/json' }));
    if (photos) {
      photos.forEach(photo => formData.append('photos', photo));
    }
    return formData;
  }
}
