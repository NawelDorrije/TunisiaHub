import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Review {
  id: number;
  rating: number;
  comment: string;
  shop?: {
    id: number;
    name: string;
  };
  product?: {
    id: number;
    name: string;
  };
  user?: {
    id: number;
    nom: string;
    prenom: string;
    email: string;
  };
  createdAt: string;
  updatedAt?: string;
}

export interface OwnerReviewInsights {
  ownerId: number;
  ownerName: string;
  totalReviews: number;
  shopReviewCount: number;
  productReviewCount: number;
  averageShopRating: number;
  averageProductRating: number;
  summary: string;
  mainProblem: string;
  mostFrequentProblem: string;
  bestFeatures: string[];
  generatedWithAi: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class AdminReviewService {
  private apiUrl = 'http://localhost:8089/api/souvenir-shops/reviews';

  constructor(private http: HttpClient) { }

  getReviewsByShop(shopId: number): Observable<Review[]> {
    return this.http.get<Review[]>(`${this.apiUrl}/shop/${shopId}`);
  }

  getReviewsByProduct(productId: number): Observable<Review[]> {
    return this.http.get<Review[]>(`${this.apiUrl}/product/${productId}`);
  }

  deleteReview(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`, { responseType: 'text' as 'json' });
  }

  getOwnerReviewInsights(shopId?: number, productIds?: number[]): Observable<OwnerReviewInsights> {
    let params = new HttpParams();
    if (shopId) {
      params = params.set('shopId', shopId.toString());
    }
    if (productIds && productIds.length > 0) {
      params = params.set('productIds', productIds.join(','));
    }
    return this.http.get<OwnerReviewInsights>(`${this.apiUrl}/owner-summary`, { params });
  }

  // Get all reviews across all shops and products
  getAllReviews(): Observable<Review[]> {
    return this.http.get<Review[]>(`${this.apiUrl}/all`);
  }
}
