import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ReservationService } from '../../services/reservation.service';
import { AccommodationStats } from '../../../../models/accommodations/statistics.model';
import {
  ApexChart,
  ApexNonAxisChartSeries,
  ApexAxisChartSeries,
  ApexXAxis,
  ApexDataLabels,
  ApexLegend,
  ApexPlotOptions,
  ApexTitleSubtitle
} from 'ng-apexcharts';

@Component({
  selector: 'app-accommodation-statistics',
  templateUrl: './accommodation-statistics.component.html',
  styleUrls: ['./accommodation-statistics.component.css']
})
export class AccommodationStatisticsComponent implements OnInit {

  stats!: AccommodationStats;
  isLoading = true;
  errorMessage = '';

  // Pie chart — accommodation types
  typeChartSeries: ApexNonAxisChartSeries = [];
  typeChartLabels: string[] = [];
  typeChart: ApexChart = {
    type: 'pie', height: 300,
    toolbar: { show: false }
  };
  statusChartColors: string[] = ['#28a745', '#dc3545'];
  priceChartColors: string[] = ['#0d6efd'];
  reservedChartColors: string[] = ['#6f42c1'];
  capacityChartColors: string[] = ['#fd7e14'];

  // Donut chart — reservation status
  statusChartSeries: ApexNonAxisChartSeries = [];
  statusChart: ApexChart = {
    type: 'donut', height: 300,
    toolbar: { show: false }
  };
  statusChartLabels: string[] = ['Confirmed', 'Cancelled'];

  // Bar chart — price distribution
  priceChartSeries: ApexAxisChartSeries = [];
  priceChart: ApexChart = {
    type: 'bar', height: 300,
    toolbar: { show: false }
  };
  priceChartXAxis: ApexXAxis = {
    categories: ['< 100 TND', '100-200 TND', '200-300 TND', '> 300 TND']
  };

  // Bar chart — top reserved
  reservedChartSeries: ApexAxisChartSeries = [];
  reservedChart: ApexChart = {
    type: 'bar', height: 300,
    toolbar: { show: false }
  };
  reservedChartXAxis: ApexXAxis = { categories: [] };

  // Bar chart — capacity distribution
  capacityChartSeries: ApexAxisChartSeries = [];
  capacityChart: ApexChart = {
    type: 'bar', height: 300,
    toolbar: { show: false }
  };
  capacityChartXAxis: ApexXAxis = {
    categories: ['1-2 persons', '3-5 persons', '6-10 persons', '10+ persons']
  };

  dataLabels: ApexDataLabels = { enabled: true };
  legend: ApexLegend = { show: true };
  plotOptions: ApexPlotOptions = {
    bar: { borderRadius: 6, columnWidth: '50%' }
  };

  constructor(
    private reservationService: ReservationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadStatistics();
  }

  loadStatistics(): void {
    this.reservationService.getStatistics().subscribe({
      next: (data) => {
        this.stats = data;
        this.buildCharts();
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Failed to load statistics.';
        this.isLoading = false;
      }
    });
  }

  buildCharts(): void {
    // Type distribution pie chart
    this.typeChartLabels = Object.keys(this.stats.accommodationsByType);
    this.typeChartSeries = Object.values(this.stats.accommodationsByType);

    // Reservation status donut
    this.statusChartSeries = [
      this.stats.confirmedReservations,
      this.stats.cancelledReservations
    ];

    // Price distribution bar
    this.priceChartSeries = [{
      name: 'Accommodations',
      data: [
        this.stats.under100,
        this.stats.between100and200,
        this.stats.between200and300,
        this.stats.above300
      ]
    }];

    // Capacity distribution bar
    this.capacityChartSeries = [{
      name: 'Accommodations',
      data: [
        this.stats.capacity1to2,
        this.stats.capacity3to5,
        this.stats.capacity6to10,
        this.stats.capacityAbove10
      ]
    }];

    // Top reserved bar
    this.reservedChartXAxis = {
      categories: this.stats.topReservedAccommodations
        .map(a => a['title'])
    };
    this.reservedChartSeries = [{
      name: 'Reservations',
      data: this.stats.topReservedAccommodations
        .map(a => a['count'])
    }];
  }

  goBack(): void {
    this.router.navigate(['/accommodations/admin']);
  }
}