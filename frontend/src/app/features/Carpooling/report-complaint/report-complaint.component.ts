import { Component, OnInit } from '@angular/core';
import { inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { CarpoolingDataService } from '../services/carpooling-data.service';

@Component({
  selector: 'app-report-complaint',
  templateUrl: './report-complaint.component.html',
  styleUrls: ['./report-complaint.component.css'],
})
export class ReportComplaintComponent implements OnInit {
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

  ngOnInit(): void {
    const tripId = Number(this.route.snapshot.queryParamMap.get('tripId') ?? 0);
    const bookingId = Number(
      this.route.snapshot.queryParamMap.get('bookingId') ?? 0,
    );

    this.complaintForm.patchValue({
      tripId: Number.isFinite(tripId) ? tripId : 0,
      bookingId: Number.isFinite(bookingId) ? bookingId : 0,
    });
  }

  submit(): void {
    this.success = '';
    this.error = '';

    if (this.complaintForm.invalid) {
      this.complaintForm.markAllAsTouched();
      return;
    }

    const form = this.complaintForm.getRawValue();
    this.dataService
      .submitComplaint({
        tripId: form.tripId,
        bookingId: form.bookingId > 0 ? form.bookingId : undefined,
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
}
