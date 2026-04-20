import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Trip } from '../../../models/Carpooling/carpooling';
import { CarpoolingDataService } from '../services/carpooling-data.service';

type SortKey =
  | 'departure-early'
  | 'lowest-price'
  | 'highest-rated-driver'
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
  filteredRides: Trip[] = [];
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
  currentPage = 1;
  readonly pageSize = 10;
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
      key: 'highest-rated-driver',
      label: 'Best driver rating',
      icon: 'bi bi-star-fill',
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
      seatsNeeded: [
        1,
        [Validators.required, Validators.min(1), Validators.max(8)],
      ],
      minDriverRating: [null],
      status: [''],
      bookingMode: [''],
      minPrice: [null],
      maxPrice: [null],
      durationMax: [null],
    });
  }

  ngOnInit(): void {
    const params = this.route.snapshot.queryParamMap;
    const departure = params.get('departure') ?? '';
    const destination = params.get('destination') ?? '';
    const date = params.get('date') ?? '';
    const returnDate = params.get('returnDate') ?? '';
    const seatsNeeded = Number(params.get('seatsNeeded') ?? 1);
    const minDriverRatingParam = params.get('minDriverRating');
    const status = params.get('status') ?? '';
    const bookingMode = params.get('bookingMode') ?? '';
    const minPriceParam = params.get('minPrice');
    const maxPriceParam = params.get('maxPrice');
    const durationMaxParam = params.get('durationMax');
    const minPrice = minPriceParam !== null ? Number(minPriceParam) : null;
    const maxPrice = maxPriceParam !== null ? Number(maxPriceParam) : null;
    const durationMax =
      durationMaxParam !== null ? Number(durationMaxParam) : null;
    const minDriverRating =
      minDriverRatingParam !== null ? Number(minDriverRatingParam) : null;

    this.searchForm.patchValue({
      departure,
      destination,
      date,
      returnDate,
      seatsNeeded:
        Number.isFinite(seatsNeeded) && seatsNeeded > 0 ? seatsNeeded : 1,
      minDriverRating:
        minDriverRating !== null &&
        Number.isFinite(minDriverRating) &&
        minDriverRating >= 1
          ? minDriverRating
          : null,
      status,
      bookingMode,
      minPrice:
        minPrice !== null && Number.isFinite(minPrice) && minPrice >= 0
          ? minPrice
          : null,
      maxPrice:
        maxPrice !== null && Number.isFinite(maxPrice) && maxPrice > 0
          ? maxPrice
          : null,
      durationMax:
        durationMax !== null && Number.isFinite(durationMax) && durationMax > 0
          ? durationMax
          : null,
    });

    this.search();
  }

  search(): void {
    const form = this.searchForm.getRawValue();
    this.dataService
      .searchTrips({
        departure: form.departure,
        destination: form.destination,
        dateFrom: form.date,
        dateTo: form.returnDate,
        seatsNeeded: form.seatsNeeded,
        minDriverRating: form.minDriverRating,
        status: form.status,
        bookingMode: form.bookingMode,
        minPrice: form.minPrice,
        maxPrice: form.maxPrice,
        durationMax: form.durationMax,
      })
      .subscribe({
        next: (rides) => {
          this.rides = rides ?? [];
          this.currentPage = 1;
          this.applyClientFilters();
        },
        error: () => {
          this.rides = [];
          this.filteredRides = [];
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
        minDriverRating:
          form.minDriverRating && Number(form.minDriverRating) >= 1
            ? form.minDriverRating
            : undefined,
        status: form.status || undefined,
        bookingMode: form.bookingMode || undefined,
        minPrice:
          form.minPrice !== null &&
          form.minPrice !== undefined &&
          Number(form.minPrice) >= 0
            ? form.minPrice
            : undefined,
        maxPrice:
          form.maxPrice && Number(form.maxPrice) > 0
            ? form.maxPrice
            : undefined,
        durationMax:
          form.durationMax && Number(form.durationMax) > 0
            ? form.durationMax
            : undefined,
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
    this.currentPage = 1;
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

    this.currentPage = 1;
    this.applyClientFilters();
  }

  isTimeSlotSelected(key: TimeSlotKey): boolean {
    return this.selectedTimeSlots.includes(key);
  }

  clearFilters(): void {
    this.activeSort = 'departure-early';
    this.selectedTimeSlots = [];
    this.searchForm.patchValue({
      minDriverRating: null,
      status: '',
      bookingMode: '',
      minPrice: null,
      maxPrice: null,
      durationMax: null,
    });
    this.search();
  }

  getTimeSlotCount(key: TimeSlotKey): number {
    return this.rides.filter((ride) => this.matchesTimeSlot(ride, key)).length;
  }

  getSearchDateLabel(): string {
    const dateFrom = this.searchForm.value.date;
    const dateTo = this.searchForm.value.returnDate;

    if (!dateFrom && !dateTo) {
      return 'Today';
    }

    if (dateFrom && dateTo) {
      if (dateFrom === dateTo) {
        return this.formatSearchDate(dateFrom);
      }

      return `${this.formatSearchDate(dateFrom)} - ${this.formatSearchDate(dateTo)}`;
    }

    if (dateFrom) {
      return `From ${this.formatSearchDate(dateFrom)}`;
    }

    return `To ${this.formatSearchDate(dateTo)}`;
  }

  getPassengersLabel(): string {
    const seats = Number(this.searchForm.value.seatsNeeded || 1);
    return seats > 1 ? `${seats} passengers` : '1 passenger';
  }

  getResultsCountLabel(): string {
    const count = this.filteredRides.length;
    return `${count} ${count === 1 ? 'trip' : 'trips'} available`;
  }

  getPaginationLabel(): string {
    if (this.filteredRides.length === 0) {
      return '0 of 0';
    }

    const start = (this.currentPage - 1) * this.pageSize + 1;
    const end = Math.min(
      this.currentPage * this.pageSize,
      this.filteredRides.length,
    );
    return `${start}-${end} of ${this.filteredRides.length}`;
  }

  hasPreviousPage(): boolean {
    return this.currentPage > 1;
  }

  hasNextPage(): boolean {
    return this.currentPage < this.getTotalPages();
  }

  previousPage(): void {
    if (!this.hasPreviousPage()) {
      return;
    }

    this.currentPage -= 1;
    this.updateDisplayedRides();
  }

  nextPage(): void {
    if (!this.hasNextPage()) {
      return;
    }

    this.currentPage += 1;
    this.updateDisplayedRides();
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

  formatRideDate(value: string): string {
    const tripDate = new Date(value);
    const today = new Date();
    const tomorrow = new Date();
    tomorrow.setDate(today.getDate() + 1);

    if (
      tripDate.getFullYear() === today.getFullYear() &&
      tripDate.getMonth() === today.getMonth() &&
      tripDate.getDate() === today.getDate()
    ) {
      return 'Today';
    }

    if (
      tripDate.getFullYear() === tomorrow.getFullYear() &&
      tripDate.getMonth() === tomorrow.getMonth() &&
      tripDate.getDate() === tomorrow.getDate()
    ) {
      return 'Tomorrow';
    }

    const dayLabels = ['Sun.', 'Mon.', 'Tue.', 'Wed.', 'Thu.', 'Fri.', 'Sat.'];
    const monthLabels = [
      'Jan',
      'Feb',
      'Mar',
      'Apr',
      'May',
      'Jun',
      'Jul',
      'Aug',
      'Sep',
      'Oct',
      'Nov',
      'Dec',
    ];

    return `${dayLabels[tripDate.getDay()]} ${tripDate.getDate()} ${monthLabels[tripDate.getMonth()]}`;
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

  isArrivalNextDay(ride: Trip): boolean {
    const departure = new Date(ride.departureDateTime);
    const arrival = new Date(
      departure.getTime() + this.getDurationMinutes(ride) * 60000,
    );

    return this.isNextDay(departure, arrival);
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

  private formatSearchDate(value: string): string {
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

  private isNextDay(departure: Date, arrival: Date): boolean {
    return (
      departure.getFullYear() !== arrival.getFullYear() ||
      departure.getMonth() !== arrival.getMonth() ||
      departure.getDate() !== arrival.getDate()
    );
  }

  getRideAvailabilityLabel(ride: Trip): string {
    const seatsAvailable = this.getSeatsAvailable(ride);
    if (seatsAvailable <= 1) {
      return 'Almost full';
    }

    return `${seatsAvailable} seats left`;
  }

  getRideStatusLabel(ride: Trip): string {
    if (ride.status === 'CANCELED') {
      return 'Canceled';
    }

    if (ride.status === 'COMPLETED') {
      return 'Completed';
    }

    return 'Scheduled';
  }

  getDriverReviewLabel(ride: Trip): string {
    if (!ride.driverReviewsCount || !ride.driverRatingAverage) {
      return 'No driver review yet';
    }

    return `${ride.driverRatingAverage.toFixed(1)}/5 · ${ride.driverReviewsCount} review${ride.driverReviewsCount > 1 ? 's' : ''}`;
  }

  hasDriverReviews(ride: Trip): boolean {
    return !!ride.driverReviewsCount && !!ride.driverRatingAverage;
  }

  private getSeatsAvailable(ride: Trip): number {
    if (
      ride.seatsAvailable !== undefined &&
      ride.seatsAvailable !== null &&
      ride.seatsAvailable >= 0
    ) {
      return ride.seatsAvailable;
    }

    return ride.seatsTotal;
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

    filteredRides = filteredRides.filter((ride) =>
      this.matchesMinDriverRating(ride),
    );

    if (this.selectedTimeSlots.length > 0) {
      filteredRides = filteredRides.filter((ride) =>
        this.selectedTimeSlots.some((slot) => this.matchesTimeSlot(ride, slot)),
      );
    }

    filteredRides.sort((left, right) => this.compareRides(left, right));
    this.filteredRides = filteredRides;

    const totalPages = this.getTotalPages();
    if (totalPages === 0) {
      this.currentPage = 1;
    } else if (this.currentPage > totalPages) {
      this.currentPage = totalPages;
    }

    this.updateDisplayedRides();
  }

  private updateDisplayedRides(): void {
    const startIndex = (this.currentPage - 1) * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.displayedRides = this.filteredRides.slice(startIndex, endIndex);
  }

  private getTotalPages(): number {
    return Math.ceil(this.filteredRides.length / this.pageSize);
  }

  private compareRides(left: Trip, right: Trip): number {
    if (this.activeSort === 'lowest-price') {
      return left.pricePerSeat - right.pricePerSeat;
    }

    if (this.activeSort === 'highest-rated-driver') {
      const rightRating = right.driverRatingAverage ?? 0;
      const leftRating = left.driverRatingAverage ?? 0;
      if (rightRating !== leftRating) {
        return rightRating - leftRating;
      }

      const rightCount = right.driverReviewsCount ?? 0;
      const leftCount = left.driverReviewsCount ?? 0;
      if (rightCount !== leftCount) {
        return rightCount - leftCount;
      }
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

  private matchesMinDriverRating(ride: Trip): boolean {
    const minDriverRating = Number(this.searchForm.value.minDriverRating || 0);
    if (!Number.isFinite(minDriverRating) || minDriverRating < 1) {
      return true;
    }

    if (!ride.driverReviewsCount || !ride.driverRatingAverage) {
      return false;
    }

    return ride.driverRatingAverage >= minDriverRating;
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
