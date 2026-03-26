import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-carpooling-home',
  templateUrl: './carpooling-home.component.html',
  styleUrls: ['./carpooling-home.component.css'],
})
export class CarpoolingHomeComponent implements OnInit {
  searchForm!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private router: Router,
  ) {}

  ngOnInit(): void {
    const today = this.formatDate(new Date());

    this.searchForm = this.fb.group({
      departure: [''],
      destination: [''],
      date: [today, Validators.required],
      returnDate: [''],
      seatsNeeded: [1, [Validators.required, Validators.min(1), Validators.max(8)]],
    });
  }

  get f() {
    return this.searchForm.controls;
  }

  searchRide(): void {
    if (this.searchForm.invalid) {
      this.searchForm.markAllAsTouched();
      return;
    }

    this.router.navigate(['/carpooling/search-rides'], {
      queryParams: {
        departure: this.searchForm.value.departure || undefined,
        destination: this.searchForm.value.destination || undefined,
        date: this.searchForm.value.date || undefined,
        seatsNeeded: this.searchForm.value.seatsNeeded || 1,
      },
    });
  }

  publishRide(): void {
    this.router.navigate(['/carpooling/publish']);
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = `${date.getMonth() + 1}`.padStart(2, '0');
    const day = `${date.getDate()}`.padStart(2, '0');

    return `${year}-${month}-${day}`;
  }
}
