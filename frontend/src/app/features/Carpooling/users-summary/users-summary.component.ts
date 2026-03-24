import { Component, OnInit } from '@angular/core';
import { CarpoolingDataService } from '../services/carpooling-data.service';

interface UserSummaryRow {
  userName: string;
  tripsCreated: number;
  bookingsMade: number;
  complaintsSubmitted: number;
  complaintsReceived: number;
}

@Component({
  selector: 'app-users-summary',
  templateUrl: './users-summary.component.html',
  styleUrls: ['./users-summary.component.css'],
})
export class UsersSummaryComponent implements OnInit {
  rows: UserSummaryRow[] = [];

  constructor(private readonly dataService: CarpoolingDataService) {}

  ngOnInit(): void {
    this.dataService.getUsersSummary().subscribe({
      next: (rows) => {
        this.rows = rows.map((item) => ({
          userName: item.user.fullName,
          tripsCreated: item.tripsCreated,
          bookingsMade: item.bookingsMade,
          complaintsSubmitted: item.complaintsSubmitted,
          complaintsReceived: item.complaintsReceived,
        }));
      },
      error: () => {
        this.rows = [];
      },
    });
  }
}
