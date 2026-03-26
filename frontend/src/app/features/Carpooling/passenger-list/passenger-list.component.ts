import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { BookingWithContext } from '../../../models/Carpooling/carpooling';
import { CarpoolingDataService } from '../services/carpooling-data.service';

@Component({
  selector: 'app-passenger-list',
  templateUrl: './passenger-list.component.html',
  styleUrls: ['./passenger-list.component.css'],
})
export class PassengerListComponent implements OnInit {
  passengers: BookingWithContext[] = [];
  tripId = 0;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly dataService: CarpoolingDataService,
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.tripId = id;
    this.dataService.getPassengersForTrip(id).subscribe({
      next: (passengers) => {
        this.passengers = passengers;
      },
      error: () => {
        this.passengers = [];
      },
    });
  }
}
