import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Shop {
  id: number;
  name: string;
  description?: string;
  owner?: {
    id: number;
    nom: string;
    prenom: string;
  };
  createdAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AdminShopService {
  private apiUrl = 'http://localhost:8089/api/souvenir-shops/shops';

  constructor(private http: HttpClient) { }

  getAllShops(): Observable<Shop[]> {
    return this.http.get<Shop[]>(this.apiUrl);
  }

  getShopById(id: number): Observable<Shop> {
    return this.http.get<Shop>(`${this.apiUrl}/${id}`);
  }

  deleteShop(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`, { responseType: 'text' as 'json' });
  }
}
