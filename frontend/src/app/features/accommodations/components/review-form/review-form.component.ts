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
     this.reviewService.addReview(this.accommodationId, review).subscribe({
  next: (added) => {
    this.successMessage = '✅ Review added successfully!';
    this.isLoading = false;
    this.reviewAdded.emit(added);
    this.resetForm();
  },
  error: (err) => {
    // Read the error message from backend correctly
    if (err.error && typeof err.error === 'string') {
      this.errorMessage = err.error;
    } else if (err.error && err.error.message) {
      this.errorMessage = err.error.message;
    } else {
      this.errorMessage = '❌ Failed to add review. Please try again.';
    }
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
  console.log('Full error:', err); // ← add this temporarily to see what we get
  
  if (typeof err.error === 'string') {
    this.errorMessage = err.error;
  } else if (err.error?.message) {
    this.errorMessage = err.error.message;
  } else if (err.message) {
    this.errorMessage = err.message;
  } else {
    this.errorMessage = '❌ Failed to add review.';
  }
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
}