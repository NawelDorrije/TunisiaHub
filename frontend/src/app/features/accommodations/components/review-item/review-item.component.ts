import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Review } from '../../../../models/accommodations/review.model';
import { ReviewService } from '../../services/review.service';

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

  constructor(private reviewService: ReviewService) {}

  get stars(): number[] {
    return Array(this.review.rating).fill(0);
  }

  get emptyStars(): number[] {
    return Array(5 - this.review.rating).fill(0);
  }

  onEdit(): void {
    this.reviewEdit.emit(this.review);
  }

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
  }
}