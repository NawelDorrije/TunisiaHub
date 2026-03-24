import { Component } from '@angular/core';
import { inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-carpooling-home',
  templateUrl: './carpooling-home.component.html',
  styleUrls: ['./carpooling-home.component.css'],
})
export class CarpoolingHomeComponent {
  private readonly fb = inject(FormBuilder);

  readonly searchForm = this.fb.nonNullable.group({
    departure: [''],
    destination: [''],
    date: [''],
    seatsNeeded: [
      1,
      [Validators.required, Validators.min(1), Validators.max(8)],
    ],
  });

  constructor(private readonly router: Router) {}

  searchRide(): void {
    const form = this.searchForm.getRawValue();
    this.router.navigate(['/carpooling/search-rides'], {
      queryParams: {
        departure: form.departure || undefined,
        destination: form.destination || undefined,
        date: form.date || undefined,
        seatsNeeded: form.seatsNeeded || 1,
      },
    });
  }

  publishRide(): void {
    this.router.navigate(['/carpooling/publish']);
  }
}
