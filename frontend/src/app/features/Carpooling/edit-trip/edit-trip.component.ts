import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Trip } from '../../../models/Carpooling/carpooling';
import { CarpoolingDataService } from '../services/carpooling-data.service';

@Component({
  selector: 'app-edit-trip',
  templateUrl: './edit-trip.component.html',
})
export class EditTripComponent implements OnInit {
  trip?: Trip;
  error = '';
  success = '';

  editForm!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly dataService: CarpoolingDataService,
  ) {
    this.editForm = this.fb.group({
      departure: ['', [Validators.required]],
      destination: ['', [Validators.required]],
      departureDateTime: ['', [Validators.required]],
      pricePerSeat: [0, [Validators.required, Validators.min(0)]],
      seatsTotal: [1, [Validators.required, Validators.min(1)]],
    });
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.dataService.getTripById(id).subscribe({
      next: (trip) => {
        if (!trip) {
          this.error = 'Trip not found.';
          return;
        }

        const editable = this.dataService.canEditTrip(trip);
        if (!editable.allowed) {
          this.error = editable.reason ?? 'Trip is not editable.';
          return;
        }

        this.trip = trip;
        this.editForm.patchValue({
          departure: trip.departure,
          destination: trip.destination,
          departureDateTime: this.toDateTimeLocal(trip.departureDateTime),
          pricePerSeat: trip.pricePerSeat,
          seatsTotal: trip.seatsTotal,
        });
      },
      error: () => {
        this.error = 'Unable to load trip.';
      },
    });
  }

  save(): void {
    this.error = '';
    this.success = '';

    if (!this.trip) {
      this.error = 'Trip not found.';
      return;
    }

    if (this.editForm.invalid) {
      this.editForm.markAllAsTouched();
      return;
    }

    const form = this.editForm.getRawValue();
    this.dataService
      .updateTrip(this.trip.id, {
        departure: form.departure,
        destination: form.destination,
        departureDateTime: `${form.departureDateTime}:00`,
        pricePerSeat: form.pricePerSeat,
        seatsTotal: form.seatsTotal,
      })
      .subscribe({
        next: (result) => {
          if (!result.ok) {
            this.error = result.error ?? 'Unable to update trip.';
            return;
          }
          this.success = 'Trip updated successfully.';
        },
        error: () => {
          this.error = 'Unable to update trip.';
        },
      });
  }

  goToDetails(): void {
    if (this.trip) {
      this.router.navigate(['/carpooling/trip', this.trip.id]);
    }
  }

  private toDateTimeLocal(value: string): string {
    const date = new Date(value);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  }
}
