import { Component, OnInit } from '@angular/core';
import { BookingWithContext } from '../models';
import { CarpoolingDataService } from '../services/carpooling-data.service';

@Component({
  selector: 'app-all-bookings',
  templateUrl: './all-bookings.component.html',
  styleUrls: ['./all-bookings.component.css'],
})
export class AllBookingsComponent implements OnInit {
  bookings: BookingWithContext[] = [];

  constructor(private readonly dataService: CarpoolingDataService) {}

  ngOnInit(): void {
    this.dataService.getAllBookingsWithContext().subscribe({
      next: (bookings) => {
        this.bookings = bookings;
      },
      error: () => {
        this.bookings = [];
      },
    });
  }
}
