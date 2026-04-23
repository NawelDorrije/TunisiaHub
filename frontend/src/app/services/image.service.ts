import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ImageDescriptionResponse {
  imageUrl: string;
  suggestedDescription: string;
}

@Injectable({
  providedIn: 'root'
})
export class ImageService {
  private baseUrl = 'http://localhost:8089/api/images';

  constructor(private http: HttpClient) {}

  describeShopImage(file: File, name?: string, category?: string, city?: string): Observable<ImageDescriptionResponse> {
    const formData = new FormData();
    formData.append('file', file);
    if (name) formData.append('name', name);
    if (category) formData.append('category', category);
    if (city) formData.append('city', city);
    return this.http.post<ImageDescriptionResponse>(`${this.baseUrl}/shop/describe`, formData);
  }

  describeProductImage(file: File, name?: string, shopName?: string, price?: string | number): Observable<ImageDescriptionResponse> {
    const formData = new FormData();
    formData.append('file', file);
    if (name) formData.append('name', name);
    if (shopName) formData.append('shopName', shopName);
    if (price) formData.append('price', String(price));
    return this.http.post<ImageDescriptionResponse>(`${this.baseUrl}/product/describe`, formData);
  }
}
