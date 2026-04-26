import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Review } from '../../../../models/accommodations/review.model';
import { ReviewService } from '../../services/review.service';
import { AuthService } from '../../../auth/services/auth.service';

@Component({
  selector: 'app-review-item',
  templateUrl: './review-item.component.html',
  styleUrls: ['./review-item.component.css']
})
export class ReviewItemComponent {

  @Input() review!: Review;
  @Output() reviewDeleted = new EventEmitter<number>();
  @Output() reviewEdit = new EventEmitter<Review>();

  errorMessage: string = '';
  showDeleteConfirm = false;

  constructor(
    private reviewService: ReviewService,
    private authService: AuthService
  ) {}

  get stars(): number[] {
    return Array(this.review.rating).fill(0);
  }

  get emptyStars(): number[] {
    return Array(5 - this.review.rating).fill(0);
  }

  // ← check if logged in user owns this review
  get isOwner(): boolean {
    const email = this.authService.getEmail();
    return !!email && !!this.review.user && this.review.user.email === email;
  }

  onEdit(): void {
    this.reviewEdit.emit(this.review);
  }

  onDelete(): void {
    this.showDeleteConfirm = true;
  }

  confirmDelete(): void {
    this.reviewService.deleteReview(this.review.id!).subscribe({
      next: () => {
        this.reviewDeleted.emit(this.review.id!);
        this.closeDeleteModal();
      },
      error: () => {
        this.errorMessage = 'Failed to delete review.';
        this.closeDeleteModal();
      }
    });
  }

  closeDeleteModal(): void {
    this.showDeleteConfirm = false;
  }
}