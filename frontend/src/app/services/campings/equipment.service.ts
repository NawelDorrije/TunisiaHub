import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Equipment } from '../../models/campings/equipment';

@Injectable({ providedIn: 'root' })
export class EquipmentService {
  private API_URL = 'http://localhost:8089/api/equipements';
  constructor(private http: HttpClient) {}

  getAllEquipment(): Observable<Equipment[]> {
    return this.http.get<Equipment[]>(this.API_URL);
  }
  getEquipmentById(id: number): Observable<Equipment> {
    return this.http.get<Equipment>(`${this.API_URL}/${id}`);
  }
  getEquipmentBySpot(spotId: number): Observable<Equipment[]> {
    return this.http.get<Equipment[]>(`${this.API_URL}/spot/${spotId}`);
  }
  getByCondition(condition: string): Observable<Equipment[]> {
    return this.http.get<Equipment[]>(`${this.API_URL}/condition`, {
      params: new HttpParams().set('condition', condition)
    });
  }
  createEquipment(equipment: Equipment): Observable<Equipment> {
    return this.http.post<Equipment>(this.API_URL, equipment);
  }
  updateEquipment(id: number, equipment: Equipment): Observable<Equipment> {
    return this.http.put<Equipment>(`${this.API_URL}/${id}`, equipment);
  }
  deleteEquipment(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
  getEquipmentByCamping(campingId: number): Observable<Equipment[]> {
  return this.http.get<Equipment[]>(`${this.API_URL}/camping/${campingId}`);
}
}
