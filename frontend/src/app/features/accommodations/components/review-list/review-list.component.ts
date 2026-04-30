import { Component, Input, OnChanges, SimpleChanges, OnInit } from '@angular/core';
import { ReviewService } from '../../services/review.service';
import { AuthService } from '../../../auth/services/auth.service';
import { Review } from '../../../admin-dashboard/services/admin-review.service';

@Component({
  selector: 'app-review-list',
  templateUrl: './review-list.component.html',
  styleUrls: ['./review-list.component.css']
})
export class ReviewListComponent implements OnChanges,OnInit {

  @Input() accommodationId!: number;

  reviews: Review[] = [];
  errorMessage: string = '';
  isLoading: boolean = true;
  reviewToEdit: Review | null = null;
  hasUserReviewed: boolean = false;


  constructor(private reviewService: ReviewService,public authService: AuthService) {}

ngOnInit(): void {
    if (this.accommodationId) {
      this.loadReviews();
    }
  }
ngOnChanges(changes: SimpleChanges): void {
  if (changes['accommodationId'] && this.accommodationId) {
    this.loadReviews();
  }
}
loadReviews(): void {
  this.isLoading = true;
  this.reviewService.getReviewsByAccommodation(this.accommodationId).subscribe({
    next: (data) => {
      this.reviews = data;
      this.isLoading = false;

      // Check if current user already reviewed
      const email = this.authService.getEmail();
      if (email) {
        this.hasUserReviewed = this.reviews.some(
          r => r.user?.email === email
        );
      }
    },
    error: () => {
      this.errorMessage = 'Failed to load reviews.';
      this.isLoading = false;
    }
  });
}

  get averageRating(): number {
    if (this.reviews.length === 0) return 0;
    const sum = this.reviews.reduce((acc, r) => acc + r.rating, 0);
    return Math.round((sum / this.reviews.length) * 10) / 10;
  }

  get filledStars(): number[] {
    return Array(Math.floor(this.averageRating)).fill(0);
  }

  get emptyStars(): number[] {
    return Array(5 - Math.floor(this.averageRating)).fill(0);
  }

  onReviewAdded(review: Review): void {
    this.reviews.unshift(review);
    this.hasUserReviewed = true;
  }

  onReviewUpdated(updated: Review): void {
    const index = this.reviews.findIndex(r => r.id === updated.id);
    if (index !== -1) this.reviews[index] = updated;
    this.reviewToEdit = null;
  }

  onReviewDeleted(id: number): void {
    this.reviews = this.reviews.filter(r => r.id !== id);
    const email = this.authService.getEmail();
    this.hasUserReviewed = !!email && this.reviews.some(r => r.user?.email === email);
  }

  onReviewEdit(review: Review): void {
    this.reviewToEdit = review;
    // scroll to form smoothly
    setTimeout(() => {
      document.getElementById('review-form-section')?.scrollIntoView({ behavior: 'smooth' });
    }, 100);
  }

  onCancelled(): void {
    this.reviewToEdit = null;
  }
}