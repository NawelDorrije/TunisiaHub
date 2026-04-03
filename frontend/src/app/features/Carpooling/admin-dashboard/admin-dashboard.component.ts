import { Component, OnInit } from '@angular/core';
import { AdminStats } from '../models';
import { CarpoolingDataService } from '../services/carpooling-data.service';

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css'],
})
export class AdminDashboardComponent implements OnInit {
  stats: AdminStats = {
    totalTrips: 0,
    totalBookings: 0,
    totalComplaints: 0,
    activeUsers: 0,
  };

  constructor(private readonly dataService: CarpoolingDataService) {}

  ngOnInit(): void {
    this.dataService.getAdminStats().subscribe({
      next: (stats) => {
        this.stats = stats;
      },
      error: () => {
        this.stats = {
          totalTrips: 0,
          totalBookings: 0,
          totalComplaints: 0,
          activeUsers: 0,
        };
      },
    });
  }
}
