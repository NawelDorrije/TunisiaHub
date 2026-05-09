import { Component, OnInit } from '@angular/core';
<<<<<<< HEAD
import { inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
=======
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Router } from '@angular/router';
>>>>>>> origin/feature/integrated-app-event
import { CarpoolingDataService } from '../services/carpooling-data.service';

@Component({
  selector: 'app-report-complaint',
  templateUrl: './report-complaint.component.html',
  styleUrls: ['./report-complaint.component.css'],
})
export class ReportComplaintComponent implements OnInit {
<<<<<<< HEAD
  private readonly fb = inject(FormBuilder);

  success = '';
  error = '';

  readonly complaintForm = this.fb.nonNullable.group({
    tripId: [0, [Validators.required, Validators.min(1)]],
    bookingId: [0],
    description: ['', [Validators.required, Validators.minLength(10)]],
  });

  constructor(
    private readonly route: ActivatedRoute,
    private readonly dataService: CarpoolingDataService,
  ) {}
=======
  success = '';
  error = '';
  tripId = 0;
  bookingId?: number;

  complaintForm!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly dataService: CarpoolingDataService,
  ) {
    this.complaintForm = this.fb.group({
      description: ['', [Validators.required, Validators.minLength(10)]],
    });
  }
>>>>>>> origin/feature/integrated-app-event

  ngOnInit(): void {
    const tripId = Number(this.route.snapshot.queryParamMap.get('tripId') ?? 0);
    const bookingId = Number(
      this.route.snapshot.queryParamMap.get('bookingId') ?? 0,
    );

<<<<<<< HEAD
    this.complaintForm.patchValue({
      tripId: Number.isFinite(tripId) ? tripId : 0,
      bookingId: Number.isFinite(bookingId) ? bookingId : 0,
    });
=======
    this.tripId = Number.isFinite(tripId) ? tripId : 0;
    this.bookingId =
      Number.isFinite(bookingId) && bookingId > 0 ? bookingId : undefined;

    if (this.tripId <= 0) {
      this.error = 'Missing or invalid trip reference in URL.';
    }
>>>>>>> origin/feature/integrated-app-event
  }

  submit(): void {
    this.success = '';
    this.error = '';

<<<<<<< HEAD
    if (this.complaintForm.invalid) {
      this.complaintForm.markAllAsTouched();
=======
    if (this.tripId <= 0) {
      this.error = 'Missing or invalid trip reference in URL.';
      return;
    }

    if (this.complaintForm.invalid) {
      this.complaintForm.markAllAsTouched();
      const descriptionControl = this.complaintForm.get('description');
      if (descriptionControl?.hasError('required')) {
        this.error = 'Please describe the issue before submitting.';
      } else if (descriptionControl?.hasError('minlength')) {
        this.error = 'Description must contain at least 10 characters.';
      } else {
        this.error = 'Complaint form is invalid.';
      }
>>>>>>> origin/feature/integrated-app-event
      return;
    }

    const form = this.complaintForm.getRawValue();
    this.dataService
      .submitComplaint({
<<<<<<< HEAD
        tripId: form.tripId,
        bookingId: form.bookingId > 0 ? form.bookingId : undefined,
=======
        tripId: this.tripId,
        bookingId: this.bookingId,
>>>>>>> origin/feature/integrated-app-event
        description: form.description,
      })
      .subscribe({
        next: () => {
          this.success = 'Complaint submitted successfully.';
          this.complaintForm.patchValue({ description: '' });
        },
        error: () => {
          this.error = 'Unable to submit complaint from backend endpoint.';
        },
      });
  }
<<<<<<< HEAD
=======

  goBack(): void {
    this.router.navigate(['/carpooling/my-bookings']);
  }
>>>>>>> origin/feature/integrated-app-event
}
