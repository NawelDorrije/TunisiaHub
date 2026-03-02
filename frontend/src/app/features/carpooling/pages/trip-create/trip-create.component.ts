import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize, timeout } from 'rxjs/operators';
import { SessionService } from '../../../../core/services/session.service';
import { TripService } from '../../services/trip.service';

@Component({
  selector: 'app-trip-create',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './trip-create.component.html',
  styleUrl: './trip-create.component.css',
})
export class TripCreateComponent {
  private readonly fb = inject(FormBuilder);
  private readonly tripService = inject(TripService);
  private readonly router = inject(Router);
  private readonly sessionService = inject(SessionService);

  protected readonly form = this.fb.group({
    departurePoint: ['', [Validators.required]],
    destination: ['', [Validators.required]],
    departureDateTime: ['', [Validators.required]],
    price: [0, [Validators.min(0)]],
    seatsTotal: [1, [Validators.required, Validators.min(1)]],
  });

  protected loading = false;
  protected errorMessage = '';

  protected get isDriver(): boolean {
    return this.sessionService.role() === 'DRIVER';
  }

  submit(): void {
    if (!this.isDriver) {
      this.errorMessage = 'Only DRIVER can create trips.';
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    const payload = this.form.getRawValue();
    console.info('[TripCreate] Submitting create-trip request', {
      role: this.sessionService.role(),
      userId: this.sessionService.userId(),
      departurePoint: payload.departurePoint,
      destination: payload.destination,
      departureDateTime: payload.departureDateTime,
    });

    this.tripService
      .createTrip({
        departurePoint: payload.departurePoint ?? '',
        destination: payload.destination ?? '',
        departureDateTime: payload.departureDateTime ?? '',
        price: payload.price,
        seatsTotal: payload.seatsTotal ?? 1,
      })
      .pipe(
        timeout(15000),
        finalize(() => {
          this.loading = false;
        }),
      )
      .subscribe({
        next: () => {
          console.info('[TripCreate] Create-trip succeeded');
          this.router.navigate(['/carpooling/my-trips']);
        },
        error: (error) => {
          console.error('[TripCreate] Create-trip failed', error);
          this.errorMessage = error?.error?.message ?? 'Failed to create trip';
        },
      });
  }
}
