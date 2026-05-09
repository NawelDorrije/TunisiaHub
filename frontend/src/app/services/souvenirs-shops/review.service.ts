import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Review, CreateReviewRequest, UpdateReviewRequest, ReviewEligibilityResponse } from '../../models/souvenirs-shops/review.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ReviewService {

  private apiUrl = `${environment.apiUrl}/reviews`;

  constructor(private http: HttpClient) {}

  // Get reviews for a shop
  getReviewsByShop(shopId: number): Observable<Review[]> {
    return this.http.get<Review[]>(`${this.apiUrl}/shop/${shopId}`);
  }

  // Get reviews for a product
  getReviewsByProduct(productId: number): Observable<Review[]> {
    return this.http.get<Review[]>(`${this.apiUrl}/product/${productId}`);
  }

  // Create review for a shop
  createReviewForShop(shopId: number, review: CreateReviewRequest): Observable<Review> {
    return this.http.post<Review>(`${this.apiUrl}/shop/${shopId}`, review);
  }

  // Create review for a product
  createReviewForProduct(productId: number, review: CreateReviewRequest): Observable<Review> {
    return this.http.post<Review>(`${this.apiUrl}/product/${productId}`, review);
  }

  // Update review
  updateReview(reviewId: number, review: UpdateReviewRequest): Observable<Review> {
    return this.http.put<Review>(`${this.apiUrl}/${reviewId}`, review);
  }

  // Delete review (soft delete)
  deleteReview(reviewId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${reviewId}`);
  }

  // Get reviews with eligibility for a shop (single API call)
  getReviewsWithEligibilityByShop(shopId: number): Observable<ReviewEligibilityResponse> {
    return this.http.get<ReviewEligibilityResponse>(`${this.apiUrl}/shop/${shopId}/with-eligibility`);
  }

  // Get reviews with eligibility for a product (single API call)
  getReviewsWithEligibilityByProduct(productId: number): Observable<ReviewEligibilityResponse> {
    return this.http.get<ReviewEligibilityResponse>(`${this.apiUrl}/product/${productId}/with-eligibility`);
  }
}