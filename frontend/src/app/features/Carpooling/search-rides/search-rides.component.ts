import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Trip } from '../../../models/Carpooling/carpooling';
import { CarpoolingDataService } from '../services/carpooling-data.service';

type SortKey =
  | 'departure-early'
  | 'lowest-price'
  | 'closest-departure'
  | 'closest-arrival'
  | 'shortest-trip';

type TimeSlotKey = 'morning' | 'afternoon' | 'evening';

interface SortOption {
  key: SortKey;
  label: string;
  icon: string;
}

interface TimeSlotOption {
  key: TimeSlotKey;
  label: string;
}

@Component({
  selector: 'app-search-rides',
  templateUrl: './search-rides.component.html',
  styleUrls: ['./search-rides.component.css'],
})
export class SearchRidesComponent implements OnInit {
  searchForm!: FormGroup;

  rides: Trip[] = [];
  displayedRides: Trip[] = [];
  departureSuggestions: Array<{ label: string; value: string; type: 'city' }> =
    [];
  destinationSuggestions: Array<{
    label: string;
    value: string;
    type: 'city';
  }> = [];
  showDepartureSuggestions = false;
  showDestinationSuggestions = false;
  loadingLocations = false;
  locationError = '';
  activeSort: SortKey = 'departure-early';
  selectedTimeSlots: TimeSlotKey[] = [];
  readonly sortOptions: SortOption[] = [
    {
      key: 'departure-early',
      label: 'Earliest departure',
      icon: 'bi bi-clock-history',
    },
    {
      key: 'lowest-price',
      label: 'Lowest price',
      icon: 'bi bi-cash-stack',
    },
    {
      key: 'closest-departure',
      label: 'Closest to departure',
      icon: 'bi bi-geo-alt',
    },
    {
      key: 'closest-arrival',
      label: 'Closest to arrival',
      icon: 'bi bi-geo',
    },
    {
      key: 'shortest-trip',
      label: 'Shortest trip',
      icon: 'bi bi-hourglass-split',
    },
  ];
  readonly timeSlotOptions: TimeSlotOption[] = [
    { key: 'morning', label: '00:00 - 12:00' },
    { key: 'afternoon', label: '12:01 - 18:00' },
    { key: 'evening', label: 'After 18:00' },
  ];

  private searchTimeout: any = null;

  constructor(
    private fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly dataService: CarpoolingDataService,
  ) {
    this.searchForm = this.fb.group({
      departure: [''],
      destination: [''],
      date: [''],
      returnDate: [''],
      seatsNeeded: [1, [Validators.required, Validators.min(1), Validators.max(8)]],
    });
  }

  ngOnInit(): void {
    const params = this.route.snapshot.queryParamMap;
    const departure = params.get('departure') ?? '';
    const destination = params.get('destination') ?? '';
    const date = params.get('date') ?? '';
    const returnDate = params.get('returnDate') ?? '';
    const seatsNeeded = Number(params.get('seatsNeeded') ?? 1);

    this.searchForm.patchValue({
      departure,
      destination,
      date,
      returnDate,
      seatsNeeded:
        Number.isFinite(seatsNeeded) && seatsNeeded > 0 ? seatsNeeded : 1,
    });

    this.search();
  }

  search(): void {
    const form = this.searchForm.getRawValue();
    this.dataService
      .searchTrips({
        departure: form.departure,
        destination: form.destination,
        date: form.date,
        seatsNeeded: form.seatsNeeded,
      })
      .subscribe({
        next: (rides) => {
          this.rides = rides ?? [];
          this.applyClientFilters();
        },
        error: () => {
          this.rides = [];
          this.displayedRides = [];
        },
      });

    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        departure: form.departure || undefined,
        destination: form.destination || undefined,
        date: form.date || undefined,
        returnDate: form.returnDate || undefined,
        seatsNeeded: form.seatsNeeded || 1,
      },
      queryParamsHandling: 'merge',
    });
  }

  viewDetails(tripId: number): void {
    this.router.navigate(['/carpooling/trip', tripId]);
  }

  getDriverName(ride: Trip): string {
    if (ride.ownerUserId === this.dataService.getCurrentUser().id) {
      return 'Your trip';
    }

    return ride.ownerFullName || `User ${ride.ownerUserId}`;
  }

  getDriverInitials(ride: Trip): string {
    const name = ride.ownerFullName || this.getDriverName(ride);
    return name
      .split(' ')
      .filter((part) => !!part)
      .slice(0, 2)
      .map((part) => part.charAt(0).toUpperCase())
      .join('');
  }

  swapLocations(): void {
    const departure = this.searchForm.value.departure || '';
    const destination = this.searchForm.value.destination || '';

    this.searchForm.patchValue({
      departure: destination,
      destination: departure,
    });
  }

  setSort(key: SortKey): void {
    this.activeSort = key;
    this.applyClientFilters();
  }

  toggleTimeSlot(key: TimeSlotKey): void {
    if (this.selectedTimeSlots.includes(key)) {
      this.selectedTimeSlots = this.selectedTimeSlots.filter(
        (slot) => slot !== key,
      );
    } else {
      this.selectedTimeSlots = [...this.selectedTimeSlots, key];
    }

    this.applyClientFilters();
  }

  isTimeSlotSelected(key: TimeSlotKey): boolean {
    return this.selectedTimeSlots.includes(key);
  }

  clearFilters(): void {
    this.activeSort = 'departure-early';
    this.selectedTimeSlots = [];
    this.applyClientFilters();
  }

  getTimeSlotCount(key: TimeSlotKey): number {
    return this.rides.filter((ride) => this.matchesTimeSlot(ride, key)).length;
  }

  getSearchDateLabel(): string {
    const value = this.searchForm.value.date;
    if (!value) {
      return 'Today';
    }

    const date = new Date(value);
    const today = new Date();

    if (
      date.getFullYear() === today.getFullYear() &&
      date.getMonth() === today.getMonth() &&
      date.getDate() === today.getDate()
    ) {
      return 'Today';
    }

    return date.toLocaleDateString('en-GB', {
      day: 'numeric',
      month: 'short',
    });
  }

  getPassengersLabel(): string {
    const seats = Number(this.searchForm.value.seatsNeeded || 1);
    return seats > 1 ? `${seats} passengers` : '1 passenger';
  }

  getResultsTitle(): string {
    if (this.searchForm.value.departure && this.searchForm.value.destination) {
      return `${this.searchForm.value.departure} → ${this.searchForm.value.destination}`;
    }

    if (this.searchForm.value.departure) {
      return `${this.searchForm.value.departure} → All destinations`;
    }

    if (this.searchForm.value.destination) {
      return `To ${this.searchForm.value.destination}`;
    }

    return 'Available trips';
  }

  formatRideTime(value: string): string {
    const date = new Date(value);
    const hours = `${date.getHours()}`.padStart(2, '0');
    const minutes = `${date.getMinutes()}`.padStart(2, '0');
    return `${hours}:${minutes}`;
  }

  formatArrivalTime(ride: Trip): string {
    const departure = new Date(ride.departureDateTime);
    const arrival = new Date(
      departure.getTime() + this.getDurationMinutes(ride) * 60000,
    );
    const hours = `${arrival.getHours()}`.padStart(2, '0');
    const minutes = `${arrival.getMinutes()}`.padStart(2, '0');
    return `${hours}:${minutes}`;
  }

  formatDuration(ride: Trip): string {
    const duration = this.getDurationMinutes(ride);
    const hours = Math.floor(duration / 60);
    const minutes = duration % 60;

    if (hours === 0) {
      return `${minutes} min`;
    }

    if (minutes === 0) {
      return `${hours}h`;
    }

    return `${hours}h${minutes.toString().padStart(2, '0')}`;
  }

  formatPlace(value: string): string {
    if (!value) {
      return '';
    }

    return value.split(',')[0].trim();
  }

  getRideAvailabilityLabel(ride: Trip): string {
    if (ride.seatsAvailable <= 1) {
      return 'Almost full';
    }

    if (ride.bookingMode === 'instant') {
      return 'Instant booking';
    }

    return `${ride.seatsAvailable} seats left`;
  }

  onLocationInput(field: 'departure' | 'destination'): void {
    const value = `${this.searchForm.value[field] || ''}`.trim();
    this.locationError = '';

    if (this.searchTimeout) {
      clearTimeout(this.searchTimeout);
    }

    if (!value) {
      this.loadingLocations = false;
      if (field === 'departure') {
        this.departureSuggestions = [];
        this.showDepartureSuggestions = false;
      } else {
        this.destinationSuggestions = [];
        this.showDestinationSuggestions = false;
      }
      return;
    }

    if (field === 'departure') {
      this.showDepartureSuggestions = true;
    } else {
      this.showDestinationSuggestions = true;
    }

    this.searchTimeout = setTimeout(() => {
      this.searchTunisiaCities(field, value);
    }, 250);
  }

  openSuggestions(field: 'departure' | 'destination'): void {
    if (`${this.searchForm.value[field] || ''}`.trim()) {
      this.onLocationInput(field);
    }
  }

  closeSuggestions(field: 'departure' | 'destination'): void {
    setTimeout(() => {
      if (field === 'departure') {
        this.showDepartureSuggestions = false;
      } else {
        this.showDestinationSuggestions = false;
      }
    }, 150);
  }

  selectSuggestion(
    field: 'departure' | 'destination',
    suggestion: { label: string; value: string; type: 'city' },
  ): void {
    this.searchForm.patchValue({
      [field]: suggestion.value,
    });

    if (field === 'departure') {
      this.departureSuggestions = [];
      this.showDepartureSuggestions = false;
    } else {
      this.destinationSuggestions = [];
      this.showDestinationSuggestions = false;
    }
  }

  shouldShowNoResults(field: 'departure' | 'destination'): boolean {
    const value = `${this.searchForm.value[field] || ''}`.trim();
    const suggestions =
      field === 'departure'
        ? this.departureSuggestions
        : this.destinationSuggestions;

    return (
      !this.loadingLocations &&
      !this.locationError &&
      value.length > 0 &&
      suggestions.length === 0
    );
  }

  private applyClientFilters(): void {
    let filteredRides = [...this.rides];

    if (this.selectedTimeSlots.length > 0) {
      filteredRides = filteredRides.filter((ride) =>
        this.selectedTimeSlots.some((slot) => this.matchesTimeSlot(ride, slot)),
      );
    }

    filteredRides.sort((left, right) => this.compareRides(left, right));
    this.displayedRides = filteredRides;
  }

  private compareRides(left: Trip, right: Trip): number {
    if (this.activeSort === 'lowest-price') {
      return left.pricePerSeat - right.pricePerSeat;
    }

    if (this.activeSort === 'shortest-trip') {
      return this.getDurationMinutes(left) - this.getDurationMinutes(right);
    }

    if (this.activeSort === 'closest-arrival') {
      return (
        new Date(left.departureDateTime).getTime() +
        this.getDurationMinutes(left) * 60000 -
        (new Date(right.departureDateTime).getTime() +
          this.getDurationMinutes(right) * 60000)
      );
    }

    if (this.activeSort === 'closest-departure') {
      const departureSearch = `${this.searchForm.value.departure || ''}`
        .trim()
        .toLowerCase();

      const leftMatch = left.departure.toLowerCase().startsWith(departureSearch)
        ? 0
        : 1;
      const rightMatch = right.departure
        .toLowerCase()
        .startsWith(departureSearch)
        ? 0
        : 1;

      if (leftMatch !== rightMatch) {
        return leftMatch - rightMatch;
      }
    }

    return (
      new Date(left.departureDateTime).getTime() -
      new Date(right.departureDateTime).getTime()
    );
  }

  private matchesTimeSlot(ride: Trip, key: TimeSlotKey): boolean {
    const hour = new Date(ride.departureDateTime).getHours();

    if (key === 'morning') {
      return hour < 12;
    }

    if (key === 'afternoon') {
      return hour >= 12 && hour <= 18;
    }

    return hour > 18;
  }

  private getDurationMinutes(ride: Trip): number {
    if (ride.durationMinutes && ride.durationMinutes > 0) {
      return Math.round(ride.durationMinutes);
    }

    return 60;
  }

  private searchTunisiaCities(
    field: 'departure' | 'destination',
    value: string,
  ): void {
    this.loadingLocations = true;
    this.dataService.searchTunisiaCities(value).subscribe({
      next: (cities: string[]) => {
        const suggestions = cities.map((city) => ({
          label: city,
          value: city,
          type: 'city' as const,
        }));

        if (field === 'departure') {
          this.departureSuggestions = suggestions;
        } else {
          this.destinationSuggestions = suggestions;
        }

        this.loadingLocations = false;
      },
      error: () => {
        this.loadingLocations = false;
        this.locationError = 'Location list unavailable.';
        if (field === 'departure') {
          this.departureSuggestions = [];
        } else {
          this.destinationSuggestions = [];
        }
      },
    });
  }
}
