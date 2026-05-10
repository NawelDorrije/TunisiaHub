import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Shop, NearbyShopResponse } from '../../models/souvenirs-shops/shop.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ShopService {

  private apiUrl = `${environment.apiUrl}/shops`;

  constructor(private http: HttpClient) {}

  getAllShops(): Observable<Shop[]> {
    return this.http.get<Shop[]>(`${this.apiUrl}`);
  }

  getShopById(id: number): Observable<Shop> {
    return this.http.get<Shop>(`${this.apiUrl}/${id}`);
  }

  getShopsByOwner(ownerId: number): Observable<Shop[]> {
    return this.http.get<Shop[]>(`${this.apiUrl}/owner/${ownerId}`);
  }

  getProductsByShop(shopId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${shopId}/products`);
  }

  getOrdersByShop(shopId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${shopId}/orders`);
  }

  getReviewsByShop(shopId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${shopId}/reviews`);
  }

  addShop(shop: Shop): Observable<Shop> {
    return this.http.post<Shop>(`${this.apiUrl}`, shop);
  }

  updateShop(shop: Shop): Observable<Shop> {
    return this.http.put<Shop>(`${this.apiUrl}`, shop);
  }

  deleteShop(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getNearbyShops(latitude: number, longitude: number, radiusKm: number = 5): Observable<NearbyShopResponse[]> {
    const params = new HttpParams()
      .set('latitude', latitude.toString())
      .set('longitude', longitude.toString())
      .set('radiusKm', radiusKm.toString());
    
    return this.http.get<NearbyShopResponse[]>(`${this.apiUrl}/nearby`, { params });
  }
}