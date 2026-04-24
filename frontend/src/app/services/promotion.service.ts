import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ShopData {
  id: number;
  name: string;
  rating?: number;
  city?: string;
  category?: string;
  products?: ProductData[];
}

export interface ProductData {
  id: number;
  name: string;
  price?: number;
}

export interface GeneratePromotionRequest {
  shopId?: number | null;
  productId?: number | null;
  language: string;
  platform: string;
  tone: string;
  colorTheme?: string;
  mood?: string;
  focus?: string;
}

export interface PromotionCaptionResponse {
  caption: string;
}

export interface PromotionImageResponse {
  imageUrl: string;
}

@Injectable({
  providedIn: 'root'
})
export class PromotionService {
  private BASE_URL = 'http://localhost:8089';

  constructor(private http: HttpClient) { }

  generateCaption(request: GeneratePromotionRequest): Observable<PromotionCaptionResponse> {
    return this.http.post<PromotionCaptionResponse>(
      `${this.BASE_URL}/api/souvenir-shops/promotions/caption`,
      request
    );
  }

  generateImage(request: GeneratePromotionRequest): Observable<PromotionImageResponse> {
    return this.http.post<PromotionImageResponse>(
      `${this.BASE_URL}/api/souvenir-shops/promotions/image`,
      request
    );
  }
}
