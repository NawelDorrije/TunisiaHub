import { Component, OnInit } from '@angular/core';
import { AdminReviewService, Review } from '../../services/admin-review.service';

@Component({
  selector: 'app-admin-reviews',
  templateUrl: './reviews.component.html',
  styleUrls: ['./reviews.component.css']
})
export class ReviewsComponent implements OnInit {
  reviews: Review[] = [];
  isLoading = false;
  error: string | null = null;

  constructor(
    private reviewService: AdminReviewService
  ) {}

  ngOnInit(): void {
    this.loadReviews();
  }

  loadReviews(): void {
    this.isLoading = true;
    this.error = null;

    this.reviewService.getAllReviews().subscribe({
      next: (reviews: Review[]) => {
        this.reviews = reviews;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading reviews:', err);
        this.error = 'Failed to load reviews. Please try again.';
        this.isLoading = false;
      }
    });
  }

  deleteReview(id: number): void {
    if (confirm('Are you sure you want to delete this review?')) {
      this.reviewService.deleteReview(id).subscribe({
        next: () => {
          this.reviews = this.reviews.filter(r => r.id !== id);
        },
        error: (err) => {
          console.error('Error deleting review:', err);
          const errorMessage = err.error?.message || err.message || (typeof err.error === 'string' ? err.error : 'Unknown error');
          alert('Failed to delete review: ' + errorMessage);
        }
      });
    }
  }

  getStars(rating: number): string[] {
    return Array(rating).fill('⭐');
  }
}
