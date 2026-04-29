import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Router } from '@angular/router';
import { CarpoolingDataService } from '../services/carpooling-data.service';

@Component({
  selector: 'app-report-complaint',
  templateUrl: './report-complaint.component.html',
  styleUrls: ['./report-complaint.component.css'],
})
export class ReportComplaintComponent implements OnInit {
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

  ngOnInit(): void {
    const tripId = Number(this.route.snapshot.queryParamMap.get('tripId') ?? 0);
    const bookingId = Number(
      this.route.snapshot.queryParamMap.get('bookingId') ?? 0,
    );

    this.tripId = Number.isFinite(tripId) ? tripId : 0;
    this.bookingId =
      Number.isFinite(bookingId) && bookingId > 0 ? bookingId : undefined;

    if (this.tripId <= 0) {
      this.error = 'Missing or invalid trip reference in URL.';
    }
  }

  submit(): void {
    this.success = '';
    this.error = '';

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
      return;
    }

    const form = this.complaintForm.getRawValue();
    this.dataService
      .submitComplaint({
        tripId: this.tripId,
        bookingId: this.bookingId,
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

  goBack(): void {
    this.router.navigate(['/carpooling/my-bookings']);
  }
}
