import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Review } from '../../../../models/accommodations/review.model';
import { ReviewService } from '../../services/review.service';

@Component({
  selector: 'app-review-form',
  templateUrl: './review-form.component.html',
  styleUrls: ['./review-form.component.css']
})
export class ReviewFormComponent implements OnChanges {

  @Input() accommodationId!: number;
  @Input() reviewToEdit: Review | null = null;   // if set → edit mode

  @Output() reviewAdded = new EventEmitter<Review>();
  @Output() reviewUpdated = new EventEmitter<Review>();
  @Output() cancelled = new EventEmitter<void>();

  errorMessage: string = '';
  successMessage: string = '';
  isLoading: boolean = false;

  reviewForm = new FormGroup({
    rating: new FormControl<number | null>(null, [Validators.required, Validators.min(1), Validators.max(5)]),
    comment: new FormControl('', [Validators.required, Validators.minLength(5)])
  });

  get f() {
    return this.reviewForm.controls;
  }

  get isEditMode(): boolean {
    return this.reviewToEdit !== null;
  }

  constructor(private reviewService: ReviewService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['reviewToEdit'] && this.reviewToEdit) {
      this.reviewForm.patchValue({
        rating: this.reviewToEdit.rating,
        comment: this.reviewToEdit.comment
      });
    }
  }

  onSubmit(): void {
    if (this.reviewForm.invalid) {
      this.reviewForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const review: Review = {
      rating: this.reviewForm.value.rating!,
      comment: this.reviewForm.value.comment!
    };

    if (this.isEditMode) {
      this.reviewService.updateReview(this.reviewToEdit!.id!, review).subscribe({
        next: (updated) => {
          this.successMessage = '✅ Review edited successfully!';
          this.isLoading = false;
          this.reviewUpdated.emit(updated);
          this.resetForm();
        },
        error: (err) => {
          this.errorMessage = this.extractErrorMessage(err, '❌ Failed to edit review. Please try again.');
          this.isLoading = false;
        }
      });
    } else {
      this.reviewService.addReview(this.accommodationId, review).subscribe({
        next: (added) => {
          this.successMessage = '✅ Review added successfully!';
          this.isLoading = false;
          this.reviewAdded.emit(added);
          this.resetForm();
        },
        error: (err) => {
          this.errorMessage = this.extractErrorMessage(err, '❌ Failed to add review.');
          this.isLoading = false;
        }
      });
    }
  }

  resetForm(): void {
    this.reviewForm.reset();
    this.errorMessage = '';
  }

  onCancel(): void {
    this.resetForm();
    this.cancelled.emit();
  }

  private extractErrorMessage(err: any, fallback: string): string {
    if (typeof err?.error === 'string') return err.error;
    if (typeof err?.error?.message === 'string') return err.error.message;
    if (typeof err?.message === 'string') return err.message;
    return fallback;
  }
}