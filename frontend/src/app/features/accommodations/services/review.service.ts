import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
<<<<<<< HEAD
import { Review } from '../../../models/accommodations/review.model';
=======
import { catchError, throwError } from 'rxjs';
import { Review } from '../../admin-dashboard/services/admin-review.service';
>>>>>>> origin/feature/integrated-app-event

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

<<<<<<< HEAD
  addReview(accommodationId: number, review: Review): Observable<Review> {
    return this.http.post<Review>(`${this.baseUrl}/add/${accommodationId}`, review);
  }

  updateReview(id: number, review: Review): Observable<Review> {
=======
  addReview(accommodationId: number, review: Pick<Review, 'rating' | 'comment'>): Observable<Review> {
    return this.http.post<Review>(
      `${this.baseUrl}/add/${accommodationId}`,
      review
    ).pipe(
      catchError((err) => {
        // Extract the plain text error message from backend
        const message = typeof err.error === 'string'
          ? err.error
          : 'Failed to add review.';
        return throwError(() => new Error(message));
      })
    );
  }

  updateReview(id: number, review: Pick<Review, 'rating' | 'comment'>): Observable<Review> {
>>>>>>> origin/feature/integrated-app-event
    return this.http.put<Review>(`${this.baseUrl}/update/${id}`, review);
  }

  deleteReview(id: number): Observable<string> {
    return this.http.delete(`${this.baseUrl}/delete/${id}`, { responseType: 'text' });
}

  getReviewsByAccommodation(accommodationId: number): Observable<Review[]> {
    return this.http.get<Review[]>(`${this.baseUrl}/accommodation/${accommodationId}`);
  }
}