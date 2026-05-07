import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { DriverReview } from '../../../models/Carpooling/carpooling';
import { CarpoolingDataService } from '../services/carpooling-data.service';

@Component({
  selector: 'app-review-driver',
  templateUrl: './review-driver.component.html',
  styleUrls: ['./review-driver.component.css'],
})
export class ReviewDriverComponent implements OnInit {
  success = '';
  error = '';
  tripId = 0;
  bookingId = 0;
  existingReview?: DriverReview;
  reviewForm!: FormGroup;

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly dataService: CarpoolingDataService,
  ) {
    this.reviewForm = this.fb.group({
      rating: [5, [Validators.required, Validators.min(1), Validators.max(5)]],
      comment: ['', [Validators.required, Validators.minLength(5)]],
    });
  }

  ngOnInit(): void {
    const tripId = Number(this.route.snapshot.queryParamMap.get('tripId') ?? 0);
    const bookingId = Number(
      this.route.snapshot.queryParamMap.get('bookingId') ?? 0,
    );

    this.tripId = Number.isFinite(tripId) ? tripId : 0;
    this.bookingId = Number.isFinite(bookingId) ? bookingId : 0;

    if (this.tripId <= 0 || this.bookingId <= 0) {
      this.error = 'Missing or invalid trip or booking reference in URL.';
      return;
    }

    this.dataService.getReservationReview(this.bookingId).subscribe({
      next: (review) => {
        if (!review) {
          return;
        }

        this.existingReview = review;
        this.reviewForm.patchValue({
          rating: review.rating,
          comment: review.comment,
        });
      },
      error: () => {
        this.existingReview = undefined;
      },
    });
  }

  submit(): void {
    this.success = '';
    this.error = '';

    if (this.tripId <= 0 || this.bookingId <= 0) {
      this.error = 'Missing or invalid trip or booking reference in URL.';
      return;
    }

    if (this.reviewForm.invalid) {
      this.reviewForm.markAllAsTouched();
      this.error = 'Please complete the review form correctly.';
      return;
    }

    const form = this.reviewForm.getRawValue();
    const payload = {
      rating: Number(form.rating),
      comment: `${form.comment}`.trim(),
    };
    const existingReview = this.existingReview;
    const isUpdate = !!existingReview;

    const request = isUpdate
      ? this.dataService.updateReservationReview(existingReview!.id, payload)
      : this.dataService.addReservationReview(this.bookingId, payload);

    request.subscribe({
      next: (review) => {
        this.existingReview = review;
        this.success = isUpdate
          ? 'Review updated successfully.'
          : 'Review submitted successfully.';
      },
      error: () => {
        this.error = 'Unable to save review from backend endpoint.';
      },
    });
  }

  goBack(): void {
    this.router.navigate(['/carpooling/my-bookings']);
  }
}
