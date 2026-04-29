import { Component, OnInit } from '@angular/core';
import {
  AdminBadReview,
  AdminComplaintReport,
  AdminDriver,
  BookingWithContext,
  Trip,
  TripSearchFilters,
} from '../../../models/Carpooling/carpooling';
import { CarpoolingDataService } from '../services/carpooling-data.service';

@Component({
  selector: 'app-admin-carpooling',
  templateUrl: './admin-carpooling.component.html',
  styleUrls: ['./admin-carpooling.component.css'],
})
export class AdminCarpoolingComponent implements OnInit {
  trips: Trip[] = [];
  bookings: BookingWithContext[] = [];
  drivers: AdminDriver[] = [];
  complaints: AdminComplaintReport[] = [];
  badReviews: AdminBadReview[] = [];
  analyzingReportIds: number[] = [];

  activeTab: string = 'trips';
  isLoading: boolean = false;
  errorMessage: string = '';
  successMessage: string = '';

  filters: TripSearchFilters = {
    status: '',
    departure: '',
    destination: '',
    dateFrom: '',
    dateTo: '',
    driverId: undefined,
  };

  bookingTripId?: number;
  bookingStatus: string = '';

  constructor(private carpoolingService: CarpoolingDataService) {}

  ngOnInit(): void {
    this.loadTrips();
    this.loadBookings();
    this.loadDrivers();
    this.loadReports();
  }

  loadTrips(): void {
    this.isLoading = true;
    this.carpoolingService.getAdminTrips(this.filters).subscribe({
      next: (data) => {
        this.trips = data;
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Failed to load trips. Please check that the backend is running and that you are logged in as ADMIN.';
        this.isLoading = false;
      },
    });
  }

  loadBookings(): void {
    this.carpoolingService
      .getAdminBookingsWithContext(this.bookingTripId, this.bookingStatus)
      .subscribe({
        next: (data) => {
          this.bookings = data;
        },
        error: () => {
          this.errorMessage = 'Failed to load reservations. Please check that the backend is running and that you are logged in as ADMIN.';
        },
      });
  }

  loadDrivers(): void {
    this.carpoolingService.getAdminDrivers().subscribe({
      next: (data) => {
        this.drivers = data;
      },
      error: () => {
        this.errorMessage = 'Failed to load drivers. Please check that the backend is running and that you are logged in as ADMIN.';
      },
    });
  }

  loadReports(): void {
    this.carpoolingService.getAdminComplaintReports().subscribe({
      next: (data) => {
        this.complaints = data;
        this.analyzeMissingReports();
      },
      error: () => {
        this.errorMessage = 'Failed to load reports. Please check that the backend is running and that you are logged in as ADMIN.';
      },
    });

    this.carpoolingService.getAdminBadReviews().subscribe({
      next: (data) => {
        this.badReviews = data;
      },
      error: () => {
        this.errorMessage = 'Failed to load bad reviews. Please check that the backend is running and that you are logged in as ADMIN.';
      },
    });
  }

  searchTrips(): void {
    this.loadTrips();
  }

  resetFilters(): void {
    this.filters = {
      status: '',
      departure: '',
      destination: '',
      dateFrom: '',
      dateTo: '',
      driverId: undefined,
    };
    this.loadTrips();
  }

  showBookings(trip: Trip): void {
    this.activeTab = 'bookings';
    this.bookingTripId = trip.id;
    this.loadBookings();
  }

  showTripFromReport(report: AdminComplaintReport): void {
    if (!report.tripId) {
      return;
    }

    this.showTripById(report.tripId);
  }

  showTripFromReview(review: AdminBadReview): void {
    if (!review.tripId) {
      return;
    }

    this.showTripById(review.tripId);
  }

  showTripById(tripId: number): void {
    this.activeTab = 'trips';
    const trip = this.trips.find((item) => item.id === tripId);
    if (trip) {
      this.trips = [trip];
    } else {
      this.loadTrips();
    }
  }

  showDriverTrips(driver: AdminDriver): void {
    this.activeTab = 'trips';
    this.filters = {
      status: '',
      departure: '',
      destination: '',
      dateFrom: '',
      dateTo: '',
      driverId: driver.driver.id,
    };
    this.loadTrips();
  }

  resetBookingFilters(): void {
    this.bookingTripId = undefined;
    this.bookingStatus = '';
    this.loadBookings();
  }

  cancelTrip(trip: Trip): void {
    if (!confirm('Cancel this trip?')) {
      return;
    }

    this.carpoolingService.adminCancelTrip(trip.id).subscribe({
      next: (result) => {
        if (result.ok) {
          this.successMessage = 'Trip canceled successfully.';
          this.errorMessage = '';
          this.loadTrips();
          this.loadDrivers();
        } else {
          this.errorMessage = result.error || 'Unable to cancel trip.';
          this.successMessage = '';
        }
      },
    });
  }

  reactivateTrip(trip: Trip): void {
    if (!confirm('Reactivate this trip?')) {
      return;
    }

    this.carpoolingService.adminReactivateTrip(trip.id).subscribe({
      next: (result) => {
        if (result.ok) {
          this.successMessage = 'Trip reactivated successfully.';
          this.errorMessage = '';
          this.loadTrips();
          this.loadBookings();
          this.loadDrivers();
        } else {
          this.errorMessage = result.error || 'Unable to reactivate trip.';
          this.successMessage = '';
        }
      },
    });
  }

  removeTrip(trip: Trip): void {
    if (!confirm('Remove this trip?')) {
      return;
    }

    this.carpoolingService.adminRemoveTrip(trip.id).subscribe({
      next: (result) => {
        if (result.ok) {
          this.successMessage = 'Trip removed successfully.';
          this.errorMessage = '';
          this.trips = this.trips.filter((item) => item.id !== trip.id);
          this.loadDrivers();
        } else {
          this.errorMessage = result.error || 'Unable to remove trip.';
          this.successMessage = '';
        }
      },
    });
  }

  analyzeMissingReports(): void {
    this.complaints
      .filter((report) => !report.aiSummary && !this.isAnalyzingReport(report.id))
      .forEach((report) => this.analyzeComplaint(report, true));
  }

  analyzeComplaint(report: AdminComplaintReport, automatic: boolean = false): void {
    if (this.isAnalyzingReport(report.id)) {
      return;
    }

    if (!automatic) {
      this.successMessage = '';
      this.errorMessage = '';
    }

    this.analyzingReportIds.push(report.id);

    this.carpoolingService.analyzeAdminComplaint(report.id).subscribe({
      next: (result) => {
        this.complaints = this.complaints.map((item) =>
          item.id === result.id ? result : item,
        );
        if (!automatic) {
          this.successMessage = 'Report analyzed successfully.';
        }
      },
      error: () => {
        if (!automatic) {
          this.errorMessage = 'AI analysis failed. Check GEMINI_API_KEY in the backend.';
        }
      },
      complete: () => {
        this.analyzingReportIds = this.analyzingReportIds.filter((id) => id !== report.id);
      },
    });
  }

  isAnalyzingReport(reportId: number): boolean {
    return this.analyzingReportIds.includes(reportId);
  }

  getReportDecision(report: AdminComplaintReport): string {
    const text = this.reportText(report);

    if (text.includes('suspend driver')) {
      return 'Suspend driver';
    }
    if (text.includes('warn driver')) {
      return 'Warn driver';
    }
    if (text.includes('ask details') || text.includes('unclear') || text.includes('unintelligible')) {
      return 'Ask details';
    }
    if (
      text.includes('unsafe') ||
      text.includes('dangerous') ||
      text.includes('speeding') ||
      text.includes('seatbelt') ||
      text.includes('phone use')
    ) {
      return 'Suspend driver';
    }
    if (
      text.includes('cash') ||
      text.includes('extra payment') ||
      text.includes('pickup') ||
      text.includes('location change')
    ) {
      return 'Warn driver';
    }
    if (!report.aiSummary) {
      return 'Waiting';
    }
    return 'Review';
  }

  getDecisionClass(report: AdminComplaintReport): string {
    const decision = this.getReportDecision(report);
    if (decision === 'Suspend driver') {
      return 'decision-danger';
    }
    if (decision === 'Warn driver') {
      return 'decision-warning';
    }
    if (decision === 'Ask details') {
      return 'decision-info';
    }
    return 'decision-secondary';
  }

  getMainProblem(report: AdminComplaintReport): string {
    if (!report.aiSummary) {
      return 'Waiting for AI analysis';
    }

    return report.aiSummary
      .replace(/^SUSPEND DRIVER\s*[-:;]\s*/i, '')
      .replace(/^WARN DRIVER\s*[-:;]\s*/i, '')
      .replace(/^ASK DETAILS\s*[-:;]\s*/i, '')
      .replace(/^REVIEW\s*[-:;]\s*/i, '');
  }

  getMainProblemList(report: AdminComplaintReport): string[] {
    return this.toBulletList(this.getMainProblem(report));
  }

  getAdminActionList(report: AdminComplaintReport): string[] {
    return this.toBulletList(report.aiSolutions || '-');
  }

  getDriverName(trip: Trip): string {
    return trip.ownerFullName || 'User ' + trip.ownerUserId;
  }

  private reportText(report: AdminComplaintReport): string {
    return (
      (report.aiSummary || '') +
      ' ' +
      (report.aiKeywords || '') +
      ' ' +
      (report.aiSolutions || '') +
      ' ' +
      (report.description || '')
    ).toLowerCase();
  }

  private toBulletList(value: string): string[] {
    if (!value || value === '-') {
      return ['-'];
    }

    return value
      .split(';')
      .map((item) => item.trim())
      .filter((item) => item.length > 0);
  }

  getStatusClass(status: string): string {
    if (status === 'CANCELED') {
      return 'bg-danger';
    }
    if (status === 'PENDING') {
      return 'bg-warning text-dark';
    }
    return 'bg-success';
  }
}
