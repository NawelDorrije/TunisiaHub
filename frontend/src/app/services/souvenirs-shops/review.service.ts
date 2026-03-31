import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Review } from '../../models/souvenirs-shops/review.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ReviewService {

  private apiUrl = `${environment.apiUrl}/reviews`;

  constructor(private http: HttpClient) {}

  getAllReviews(): Observable<Review[]> { return this.http.get<Review[]>(`${this.apiUrl}`); }
  getReviewById(id: number): Observable<Review> { return this.http.get<Review>(`${this.apiUrl}/${id}`); }
  getReviewsByUser(userId: number): Observable<Review[]> { return this.http.get<Review[]>(`${this.apiUrl}/user/${userId}`); }
  getReviewsByShop(shopId: number): Observable<Review[]> { return this.http.get<Review[]>(`${this.apiUrl}/shop/${shopId}`); }
  getReviewsByProduct(productId: number): Observable<Review[]> { return this.http.get<Review[]>(`${this.apiUrl}/product/${productId}`); }

  addReview(review: Review): Observable<Review> { return this.http.post<Review>(`${this.apiUrl}`, review); }
  updateReview(review: Review): Observable<Review> { return this.http.put<Review>(`${this.apiUrl}`, review); }
  deleteReview(id: number): Observable<void> { return this.http.delete<void>(`${this.apiUrl}/${id}`); }
}