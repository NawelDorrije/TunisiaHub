import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Review } from '../../../models/accommodations/review.model';

@Injectable({
  providedIn: 'root'
})
export class ReviewService {

  private baseUrl = 'http://localhost:8089/api/reviews';

  constructor(private http: HttpClient) {}

  getAllReviews(): Observable<Review[]> {
    return this.http.get<Review[]>(`${this.baseUrl}/getAll`);
  }

  getReviewById(id: number): Observable<Review> {
    return this.http.get<Review>(`${this.baseUrl}/get/${id}`);
  }

  addReview(accommodationId: number, review: Review): Observable<Review> {
    return this.http.post<Review>(`${this.baseUrl}/add/${accommodationId}`, review);
  }

  updateReview(id: number, review: Review): Observable<Review> {
    return this.http.put<Review>(`${this.baseUrl}/update/${id}`, review);
  }

  deleteReview(id: number): Observable<string> {
    return this.http.delete(`${this.baseUrl}/delete/${id}`, { responseType: 'text' });
}

  getReviewsByAccommodation(accommodationId: number): Observable<Review[]> {
    return this.http.get<Review[]>(`${this.baseUrl}/accommodation/${accommodationId}`);
  }
}