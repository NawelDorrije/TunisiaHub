import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ReservationService } from '../../services/reservation.service';

@Component({
  selector: 'app-feedback-popup',
  templateUrl: './feedback-popup.component.html',
  styleUrls: ['./feedback-popup.component.css']
})
export class FeedbackPopupComponent implements OnInit {

  @Input() accommodationId!: number;
  @Input() reservationId!: number;
  @Input() accommodationTitle!: string;
  @Output() closed = new EventEmitter<void>();

  isVisible = false;
  isLoading = false;
  isSubmitted = false;
  errorMessage = '';

  feedbackForm = new FormGroup({
    rating: new FormControl<number | null>(null, [
      Validators.required,
      Validators.min(1),
      Validators.max(5)
    ]),
    comment: new FormControl('')
  });

  get f() { return this.feedbackForm.controls; }

  constructor(private reservationService: ReservationService) {}

  ngOnInit(): void {
    // Show popup after 2 seconds
    setTimeout(() => {
      this.isVisible = true;
    }, 2000);
  }

  setRating(star: number): void {
    this.feedbackForm.patchValue({ rating: star });
  }

  onSubmit(): void {
    if (this.feedbackForm.invalid) {
      this.feedbackForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.reservationService.submitFeedback(
      this.accommodationId,
      this.reservationId,
      {
        rating: this.feedbackForm.value.rating!,
        comment: this.feedbackForm.value.comment || ''
      }
    ).subscribe({
      next: () => {
        this.isSubmitted = true;
        this.isLoading = false;
        setTimeout(() => this.close(), 2000);
      },
      error: () => {
        this.errorMessage = 'Failed to submit feedback.';
        this.isLoading = false;
      }
    });
  }

  close(): void {
    this.isVisible = false;
    this.closed.emit();
  }
}