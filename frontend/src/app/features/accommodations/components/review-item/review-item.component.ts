import { Component, EventEmitter, Input, Output } from '@angular/core';
<<<<<<< HEAD
import { Review } from '../../../../models/accommodations/review.model';
import { ReviewService } from '../../services/review.service';
=======
import { ReviewService } from '../../services/review.service';
import { AuthService } from '../../../auth/services/auth.service';
import { Review } from '../../../admin-dashboard/services/admin-review.service';
>>>>>>> origin/feature/integrated-app-event

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
<<<<<<< HEAD

  constructor(private reviewService: ReviewService) {}
=======
  showDeleteConfirm = false;

  constructor(
    private reviewService: ReviewService,
    private authService: AuthService
  ) {}
>>>>>>> origin/feature/integrated-app-event

  get stars(): number[] {
    return Array(this.review.rating).fill(0);
  }

  get emptyStars(): number[] {
    return Array(5 - this.review.rating).fill(0);
  }

<<<<<<< HEAD
=======
  // ← check if logged in user owns this review
  get isOwner(): boolean {
    const email = this.authService.getEmail();
    return !!email && !!this.review.user && this.review.user.email === email;
  }

>>>>>>> origin/feature/integrated-app-event
  onEdit(): void {
    this.reviewEdit.emit(this.review);
  }

<<<<<<< HEAD
 onDelete(): void {
    if (confirm('Are you sure you want to delete this review?')) {
      this.reviewService.deleteReview(this.review.id!).subscribe({
        next: () => {
          this.reviewDeleted.emit(this.review.id!);
        },
        error: () => {
          this.errorMessage = 'Failed to delete review.';
        }
      });
    }
=======
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
>>>>>>> origin/feature/integrated-app-event
  }
}