import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { CarpoolingDataService } from '../services/carpooling-data.service';
import { ActivatedRoute, Router } from '@angular/router';
import { DemandAlert, Trip } from '../../../models/Carpooling/carpooling';
import { TripPriceSuggestion } from '../../../models/Carpooling/trip-price-suggestion';

@Component({
  selector: 'app-publish-trip',
  templateUrl: './publish-trip.component.html',
  styleUrls: ['./publish-trip.component.css'],
})
export class PublishTripComponent implements OnInit, OnDestroy {
  @ViewChild('pickupMap') pickupMapElement: any;
  @ViewChild('dropoffMap') dropoffMapElement: any;
  @ViewChild('routeMap') routeMapElement: any;

  wizardSteps = [
    {
      key: 'departure',
      label: 'Departure',
      helper: 'Choose the city you are leaving from.',
    },
    {
      key: 'pickup',
      label: 'Pickup',
      helper: 'Add the pickup point.',
    },
    {
      key: 'destination',
      label: 'Destination',
      helper: 'Choose where the trip is going.',
    },
    {
      key: 'dropoff',
      label: 'Dropoff',
      helper: 'Add the dropoff point.',
    },
    {
      key: 'routeSelection',
      label: 'Route',
      helper: 'Confirm the route details.',
    },
    {
      key: 'date',
      label: 'Date',
      helper: 'Pick the day and hour for the trip.',
    },
    {
      key: 'price',
      label: 'Price',
      helper: 'Choose the price per seat.',
    },
    {
      key: 'seats',
      label: 'Seats',
      helper: 'Decide how many seats are available.',
    },
    {
      key: 'message',
      label: 'Message',
      helper: 'Add a short note for passengers.',
    },
    {
      key: 'instantBooking',
      label: 'Booking',
      helper: 'Choose how bookings are handled.',
    },
    {
      key: 'phoneVerification',
      label: 'Verification',
      helper: 'Finalize trust and contact checks.',
    },
  ];

  currentStepIndex = 0;
  departureSearch = '';
  departureSuggestions: any[] = [];
  destinationSearch = '';
  destinationSuggestions: any[] = [];
  pickupSearch = '';
  pickupSuggestions: any[] = [];
  pickupSearchError = '';
  showPickupSuggestions = false;
  pickupPoint = '';
  pickupCoordinates: any = null;
  pickupConfirmed = false;
  dropoffSearch = '';
  dropoffSuggestions: any[] = [];
  dropoffSearchError = '';
  showDropoffSuggestions = false;
  dropoffPoint = '';
  dropoffCoordinates: any = null;
  dropoffConfirmed = false;
  routeSuggestions: any[] = [];
  selectedRouteIndex = 0;
  routeLoading = false;
  routeError = '';
  bookingMode = 'instant';
  departureDate = '';
  departureTime = '';
  pricePerSeat = 1;
  seatsTotal = 3;
  messageText = '';
  monthLabels = [
    'Janvier',
    'Fevrier',
    'Mars',
    'Avril',
    'Mai',
    'Juin',
    'Juillet',
    'Aout',
    'Septembre',
    'Octobre',
    'Novembre',
    'Decembre',
  ];
  dayLabels = ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'];
  timeOptions = [
    '06:00',
    '07:00',
    '08:00',
    '09:00',
    '10:00',
    '11:00',
    '12:00',
    '13:00',
    '14:00',
    '15:00',
    '16:00',
    '17:00',
    '18:00',
    '19:00',
    '20:00',
    '21:00',
    '22:00',
    '23:00',
  ];
  calendarMonths: any[] = [];
  calendarStartMonth: Date = new Date();
  loadingLocations = false;
  locationError = '';
  showDepartureSuggestions = false;
  showDestinationSuggestions = false;
  departureValue = '';
  destinationValue = '';
  editMode = false;
  editTripId: number | null = null;
  loadingTrip = false;
  loadError = '';
  existingDurationMinutes: number | undefined = undefined;
  todayDate = '';
  publishError = '';
  publishingTrip = false;
  driverDemandAlert?: DemandAlert;
  driverDemandLoading = false;
  driverDemandError = '';
  priceSuggestion?: TripPriceSuggestion;
  priceSuggestionLoading = false;
  priceSuggestionError = '';
  priceTouched = false;
  searchTimeout: any = null;
  pickupMap: any = null;
  pickupMarker: any = null;
  dropoffMap: any = null;
  dropoffMarker: any = null;
  routeMap: any = null;
  routeLines: any[] = [];
  routeStartMarker: any = null;
  routeEndMarker: any = null;

  constructor(
    private dataService: CarpoolingDataService,
    private route: ActivatedRoute,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.todayDate = this.getTodayDate();
    this.departureTime = '08:00';
    this.buildCalendarMonths();
    const tripId = Number(this.route.snapshot.paramMap.get('id'));

    if (Number.isFinite(tripId) && tripId > 0) {
      this.editMode = true;
      this.editTripId = tripId;
      this.loadTripForEdit(tripId);
      return;
    }

    this.logWizardStep('Step opened', this.currentStep().key, {
      index: this.currentStepIndex,
    });
  }

  ngOnDestroy(): void {
    if (this.searchTimeout) {
      clearTimeout(this.searchTimeout);
    }

    if (this.pickupMap) {
      this.pickupMap.remove();
    }

    if (this.dropoffMap) {
      this.dropoffMap.remove();
    }

    if (this.routeMap) {
      this.routeMap.remove();
    }
  }

  currentStep() {
    return this.wizardSteps[this.currentStepIndex];
  }

  progressPercent(): number {
    return ((this.currentStepIndex + 1) / this.wizardSteps.length) * 100;
  }

  canGoBack(): boolean {
    return this.currentStepIndex > 0;
  }

  isDepartureStep(): boolean {
    return this.currentStep().key === 'departure';
  }

  isDestinationStep(): boolean {
    return this.currentStep().key === 'destination';
  }

  isPickupStep(): boolean {
    return this.currentStep().key === 'pickup';
  }

  isDropoffStep(): boolean {
    return this.currentStep().key === 'dropoff';
  }

  isDateStep(): boolean {
    return this.currentStep().key === 'date';
  }

  isRouteStep(): boolean {
    return this.currentStep().key === 'routeSelection';
  }

  isPriceStep(): boolean {
    return this.currentStep().key === 'price';
  }

  isSeatsStep(): boolean {
    return this.currentStep().key === 'seats';
  }

  isMessageStep(): boolean {
    return this.currentStep().key === 'message';
  }

  isBookingStep(): boolean {
    return this.currentStep().key === 'instantBooking';
  }

  isVerificationStep(): boolean {
    return this.currentStep().key === 'phoneVerification';
  }

  openDepartureSuggestions(): void {
    if (!this.isDepartureStep()) {
      return;
    }

    this.showDepartureSuggestions = this.departureSearch.trim().length > 0;
  }

  openDestinationSuggestions(): void {
    if (!this.isDestinationStep()) {
      return;
    }

    this.showDestinationSuggestions = this.destinationSearch.trim().length > 0;
  }

  onDepartureInput(): void {
    const value = this.departureSearch.trim();

    this.locationError = '';
    this.showDepartureSuggestions = value.length > 0;

    if (value !== this.departureValue) {
      this.departureValue = '';
    }

    if (this.searchTimeout) {
      clearTimeout(this.searchTimeout);
    }

    if (!value) {
      this.loadingLocations = false;
      this.departureSuggestions = [];
      return;
    }

    this.searchTimeout = setTimeout(() => {
      this.searchDepartureLocations(value);
    }, 300);
  }

  onDestinationInput(): void {
    const value = this.destinationSearch.trim();

    this.locationError = '';
    this.showDestinationSuggestions = value.length > 0;

    if (value !== this.destinationValue) {
      this.destinationValue = '';
    }

    if (this.searchTimeout) {
      clearTimeout(this.searchTimeout);
    }

    if (!value) {
      this.loadingLocations = false;
      this.destinationSuggestions = [];
      return;
    }

    this.searchTimeout = setTimeout(() => {
      this.searchDestinationLocations(value);
    }, 300);
  }

  closeDepartureSuggestions(): void {
    setTimeout(() => {
      this.showDepartureSuggestions = false;
    }, 150);
  }

  closeDestinationSuggestions(): void {
    setTimeout(() => {
      this.showDestinationSuggestions = false;
    }, 150);
  }

  selectDepartureSuggestion(suggestion: any): void {
    this.logWizardStep('Departure selected', 'departure', {
      value: suggestion.value,
    });
    this.departureSearch = suggestion.value;
    this.departureValue = suggestion.value;
    this.pickupPoint = '';
    this.pickupCoordinates = null;
    this.pickupConfirmed = false;
    this.pickupSearch = '';
    this.departureSuggestions = [];
    this.showDepartureSuggestions = false;
    this.refreshDriverDemandAlert();
    this.refreshPriceSuggestion();
    this.goToNextStep();
  }

  selectDestinationSuggestion(suggestion: any): void {
    this.logWizardStep('Destination selected', 'destination', {
      value: suggestion.value,
    });
    this.destinationSearch = suggestion.value;
    this.destinationValue = suggestion.value;
    this.dropoffPoint = '';
    this.dropoffCoordinates = null;
    this.dropoffConfirmed = false;
    this.dropoffSearch = '';
    this.destinationSuggestions = [];
    this.showDestinationSuggestions = false;
    this.refreshDriverDemandAlert();
    this.refreshPriceSuggestion();
    this.goToNextStep();
  }

  goToPreviousStep(): void {
    if (!this.canGoBack()) {
      return;
    }

    this.logWizardStep('Going back', this.currentStep().key, {
      index: this.currentStepIndex,
    });
    this.currentStepIndex -= 1;
    this.refreshStepUi();
  }

  shouldShowNoResults(): boolean {
    return (
      !this.loadingLocations &&
      !this.locationError &&
      this.departureSearch.trim().length > 0 &&
      this.departureSuggestions.length === 0
    );
  }

  shouldShowNoDestinationResults(): boolean {
    return (
      !this.loadingLocations &&
      !this.locationError &&
      this.destinationSearch.trim().length > 0 &&
      this.destinationSuggestions.length === 0
    );
  }

  isStepCompleted(step: any): boolean {
    for (let i = 0; i < this.wizardSteps.length; i++) {
      if (this.wizardSteps[i].key === step.key) {
        return i < this.currentStepIndex;
      }
    }

    return false;
  }

  canOpenStep(index: number): boolean {
    if (!this.editMode) {
      return false;
    }

    return index >= 0 && index < this.wizardSteps.length;
  }

  openStep(index: number): void {
    if (!this.canOpenStep(index) || index === this.currentStepIndex) {
      return;
    }

    this.logWizardStep(
      'Step opened from sidebar',
      this.wizardSteps[index].key,
      {
        from: this.currentStepIndex,
        to: index,
      },
    );
    this.currentStepIndex = index;
    this.refreshStepUi();
  }

  pageModeLabel(): string {
    return this.editMode ? 'Edit trip' : 'Publish a trip';
  }

  goToNextStep(): void {
    if (this.currentStepIndex >= this.wizardSteps.length - 1) {
      return;
    }

    this.logWizardStep('Going next', this.currentStep().key, {
      index: this.currentStepIndex,
    });
    this.currentStepIndex += 1;
    this.refreshStepUi();
  }

  selectDate(day: any): void {
    if (!day || day.empty || day.disabled) {
      return;
    }

    this.departureDate = day.dateValue;
    this.logWizardStep('Date selected', 'date', {
      date: this.departureDate,
      time: this.departureTime,
    });
    this.refreshDriverDemandAlert();
    this.refreshPriceSuggestion();
  }

  isSelectedDate(day: any): boolean {
    return !!day && this.departureDate === day.dateValue;
  }

  decreasePrice(): void {
    if (this.pricePerSeat > 1) {
      this.pricePerSeat -= 1;
      this.priceTouched = true;
      this.logWizardStep('Price updated', 'price', {
        price: this.pricePerSeat,
      });
    }
  }

  increasePrice(): void {
    this.pricePerSeat += 1;
    this.priceTouched = true;
    this.logWizardStep('Price updated', 'price', {
      price: this.pricePerSeat,
    });
  }

  decreaseSeats(): void {
    if (this.seatsTotal > 1) {
      this.seatsTotal -= 1;
      this.logWizardStep('Seats updated', 'seats', {
        seats: this.seatsTotal,
      });
    }
  }

  increaseSeats(): void {
    if (this.seatsTotal < 8) {
      this.seatsTotal += 1;
      this.logWizardStep('Seats updated', 'seats', {
        seats: this.seatsTotal,
      });
    }
  }

  clearPickupPoint(): void {
    this.logWizardStep('Pickup cleared', 'pickup');
    this.pickupPoint = '';
    this.pickupCoordinates = null;
    this.pickupConfirmed = false;
    this.pickupSearch = '';
    this.pickupSuggestions = [];
    this.pickupSearchError = '';
    this.showPickupSuggestions = false;
  }

  clearDropoffPoint(): void {
    this.logWizardStep('Dropoff cleared', 'dropoff');
    this.dropoffPoint = '';
    this.dropoffCoordinates = null;
    this.dropoffConfirmed = false;
    this.dropoffSearch = '';
    this.dropoffSuggestions = [];
    this.dropoffSearchError = '';
    this.showDropoffSuggestions = false;
  }

  onPickupSearchInput(): void {
    const value = this.pickupSearch.trim();

    this.pickupSearchError = '';
    this.showPickupSuggestions = value.length > 0;

    if (this.searchTimeout) {
      clearTimeout(this.searchTimeout);
    }

    if (!value) {
      this.pickupSuggestions = [];
      return;
    }

    this.searchTimeout = setTimeout(() => {
      this.searchPickupLocation(false);
    }, 300);
  }

  openPickupSuggestions(): void {
    if (!this.isPickupStep()) {
      return;
    }

    this.showPickupSuggestions = this.pickupSearch.trim().length > 0;
  }

  closePickupSuggestions(): void {
    setTimeout(() => {
      this.showPickupSuggestions = false;
    }, 150);
  }

  onDropoffSearchInput(): void {
    const value = this.dropoffSearch.trim();

    this.dropoffSearchError = '';
    this.showDropoffSuggestions = value.length > 0;

    if (this.searchTimeout) {
      clearTimeout(this.searchTimeout);
    }

    if (!value) {
      this.dropoffSuggestions = [];
      return;
    }

    this.searchTimeout = setTimeout(() => {
      this.searchDropoffLocation(false);
    }, 300);
  }

  openDropoffSuggestions(): void {
    if (!this.isDropoffStep()) {
      return;
    }

    this.showDropoffSuggestions = this.dropoffSearch.trim().length > 0;
  }

  closeDropoffSuggestions(): void {
    setTimeout(() => {
      this.showDropoffSuggestions = false;
    }, 150);
  }

  searchPickupLocation(autoSelectFirst: boolean = true): void {
    const value = this.pickupSearch.trim();

    if (!value) {
      this.pickupSearchError = 'Type a place to search.';
      this.pickupSuggestions = [];
      return;
    }

    this.pickupSearchError = '';
    this.showPickupSuggestions = true;
    this.logWizardStep('Pickup search', 'pickup', {
      query: value,
    });

    this.dataService
      .getTunisiaLocationSuggestions(this.buildPickupSearchQuery(value), 8)
      .subscribe({
        next: (response: any) => {
          if (!response || response.length === 0) {
            this.pickupSearchError = 'Place not found.';
            this.pickupSuggestions = [];
            return;
          }

          this.pickupSuggestions = this.buildPickupSuggestions(
            value,
            response,
            this.departureValue,
          );

          if (autoSelectFirst && this.pickupSuggestions.length > 0) {
            this.selectPickupSuggestion(this.pickupSuggestions[0]);
          }
        },
        error: () => {
          this.pickupSearchError = 'Search unavailable.';
          this.pickupSuggestions = [];
        },
      });
  }

  selectPickupSuggestion(suggestion: any): void {
    this.logWizardStep('Pickup selected', 'pickup', {
      value: suggestion.label,
    });
    const result = suggestion.result;
    const center: [number, number] = [Number(result.lat), Number(result.lon)];
    let bounds: any = null;

    if (result.boundingbox && result.boundingbox.length === 4) {
      bounds = [
        [Number(result.boundingbox[0]), Number(result.boundingbox[2])],
        [Number(result.boundingbox[1]), Number(result.boundingbox[3])],
      ];
    }

    if (this.pickupMap) {
      if (bounds) {
        this.pickupMap.fitBounds(bounds, {
          padding: [40, 40],
        });
      } else {
        this.pickupMap.setView(center, 15);
      }
    }

    if (this.pickupMarker) {
      this.pickupMarker.setLatLng(center);
    }

    this.pickupCoordinates = {
      lat: center[0],
      lng: center[1],
    };
    this.pickupConfirmed = true;
    this.pickupPoint = suggestion.label;
    this.pickupSearch = suggestion.label;
    this.showPickupSuggestions = false;
  }

  searchDropoffLocation(autoSelectFirst: boolean = true): void {
    const value = this.dropoffSearch.trim();

    if (!value) {
      this.dropoffSearchError = 'Type a place to search.';
      this.dropoffSuggestions = [];
      return;
    }

    this.dropoffSearchError = '';
    this.showDropoffSuggestions = true;
    this.logWizardStep('Dropoff search', 'dropoff', {
      query: value,
    });

    this.dataService
      .getTunisiaLocationSuggestions(this.buildPickupSearchQuery(value), 8)
      .subscribe({
        next: (response: any) => {
          if (!response || response.length === 0) {
            this.dropoffSearchError = 'Place not found.';
            this.dropoffSuggestions = [];
            return;
          }

          this.dropoffSuggestions = this.buildPickupSuggestions(
            value,
            response,
            this.destinationValue,
          );

          if (autoSelectFirst && this.dropoffSuggestions.length > 0) {
            this.selectDropoffSuggestion(this.dropoffSuggestions[0]);
          }
        },
        error: () => {
          this.dropoffSearchError = 'Search unavailable.';
          this.dropoffSuggestions = [];
        },
      });
  }

  selectDropoffSuggestion(suggestion: any): void {
    this.logWizardStep('Dropoff selected', 'dropoff', {
      value: suggestion.label,
    });
    const result = suggestion.result;
    const center: [number, number] = [Number(result.lat), Number(result.lon)];
    let bounds: any = null;

    if (result.boundingbox && result.boundingbox.length === 4) {
      bounds = [
        [Number(result.boundingbox[0]), Number(result.boundingbox[2])],
        [Number(result.boundingbox[1]), Number(result.boundingbox[3])],
      ];
    }

    if (this.dropoffMap) {
      if (bounds) {
        this.dropoffMap.fitBounds(bounds, {
          padding: [40, 40],
        });
      } else {
        this.dropoffMap.setView(center, 15);
      }
    }

    if (this.dropoffMarker) {
      this.dropoffMarker.setLatLng(center);
    }

    this.dropoffCoordinates = {
      lat: center[0],
      lng: center[1],
    };
    this.dropoffConfirmed = true;
    this.dropoffPoint = suggestion.label;
    this.dropoffSearch = suggestion.label;
    this.showDropoffSuggestions = false;
  }

  publishTrip(): void {
    const departureDateTime = `${this.departureDate}T${this.departureTime}:00`;
    const savedDeparture = this.buildSavedLocation(
      this.departureValue,
      this.pickupPoint,
    );
    const savedDestination = this.buildSavedLocation(
      this.destinationValue,
      this.dropoffPoint,
    );

    this.publishError = '';
    this.publishingTrip = true;
    this.logWizardStep('Publishing trip', 'message', {
      departure: savedDeparture,
      destination: savedDestination,
      dateTime: departureDateTime,
      price: this.pricePerSeat,
      seats: this.seatsTotal,
      bookingMode: this.bookingMode,
    });
    const durationMinutes =
      this.routeSuggestions && this.routeSuggestions[this.selectedRouteIndex]
        ? Number(this.routeSuggestions[this.selectedRouteIndex].durationMinutes)
        : this.existingDurationMinutes;
    const payload = {
      departure: savedDeparture,
      destination: savedDestination,
      departureDateTime: departureDateTime,
      durationMinutes: durationMinutes,
      pricePerSeat: this.pricePerSeat,
      seatsTotal: this.seatsTotal,
      bookingMode: this.bookingMode,
    };

    if (this.editMode && this.editTripId) {
      this.dataService.updateTrip(this.editTripId, payload).subscribe({
        next: (result) => {
          this.publishingTrip = false;
          if (!result.ok) {
            this.publishError = result.error ?? 'Unable to update trip.';
            this.logWizardStep('Update failed', 'phoneVerification', {
              error: this.publishError,
            });
            return;
          }
          this.logWizardStep('Trip updated', 'phoneVerification', {
            tripId: this.editTripId,
          });
          this.router.navigate(['/carpooling/trip', this.editTripId]);
        },
        error: (error: any) => {
          this.publishingTrip = false;
          this.publishError = this.extractPublishError(error);
          this.logWizardStep('Update failed', 'phoneVerification', {
            error: this.publishError,
          });
        },
      });
      return;
    }

    this.dataService.publishTrip(payload).subscribe({
      next: () => {
        this.publishingTrip = false;
        this.logWizardStep('Trip published', 'message');
        this.router.navigate(['/carpooling/my-trips']);
      },
      error: (error: any) => {
        this.publishingTrip = false;
        this.publishError = this.extractPublishError(error);
        this.logWizardStep('Publish failed', 'message', {
          error: this.publishError,
        });
      },
    });
  }

  selectBookingMode(mode: string): void {
    this.bookingMode = mode;
    this.logWizardStep('Booking mode selected', 'instantBooking', {
      mode: mode,
    });
    this.goToNextStep();
  }

  private searchDepartureLocations(value: string): void {
    const query = value.toLowerCase();
    this.loadingLocations = true;

    this.dataService.getCountriesAndCities().subscribe({
      next: (data: any) => {
        const locations = this.buildTunisiaLocationList(data);
        this.departureSuggestions = locations
          .filter((location) => location.label.toLowerCase().includes(query))
          .slice(0, 6);
        this.loadingLocations = false;
      },
      error: () => {
        this.loadingLocations = false;
        this.locationError = 'Location list unavailable.';
        this.departureSuggestions = [];
      },
    });
  }

  private loadTripForEdit(tripId: number): void {
    this.loadingTrip = true;
    this.loadError = '';

    this.dataService.getTripById(tripId).subscribe({
      next: (trip: Trip | undefined) => {
        this.loadingTrip = false;

        if (!trip) {
          this.loadError = 'Trip not found.';
          return;
        }

        const editable = this.dataService.canEditTrip(trip);
        if (!editable.allowed) {
          this.loadError = editable.reason ?? 'Trip is not editable.';
          return;
        }

        this.applyTripToWizard(trip);
        this.logWizardStep('Step opened', this.currentStep().key, {
          index: this.currentStepIndex,
          mode: 'edit',
          tripId: tripId,
        });
      },
      error: () => {
        this.loadingTrip = false;
        this.loadError = 'Unable to load trip.';
      },
    });
  }

  private searchDestinationLocations(value: string): void {
    const query = value.toLowerCase();
    this.loadingLocations = true;

    this.dataService.getCountriesAndCities().subscribe({
      next: (data: any) => {
        const locations = this.buildTunisiaLocationList(data);
        this.destinationSuggestions = locations
          .filter((location) => location.label.toLowerCase().includes(query))
          .slice(0, 6);
        this.loadingLocations = false;
      },
      error: () => {
        this.loadingLocations = false;
        this.locationError = 'Location list unavailable.';
        this.destinationSuggestions = [];
      },
    });
  }

  private buildLocationList(data: any): any[] {
    const suggestions: any[] = [];
    const labels = new Set<string>();

    data.forEach((item: any) => {
      const country = (item.country || '').trim();

      if (country && !labels.has(country.toLowerCase())) {
        suggestions.push({
          label: country,
          value: country,
          type: 'country',
        });
        labels.add(country.toLowerCase());
      }

      (item.cities || []).forEach((city: any) => {
        const cityName = (city || '').trim();
        const label = country ? `${cityName}, ${country}` : cityName;

        if (!cityName || labels.has(label.toLowerCase())) {
          return;
        }

        suggestions.push({
          label: label,
          value: cityName,
          type: 'city',
        });
        labels.add(label.toLowerCase());
      });
    });

    return suggestions;
  }

  private buildTunisiaLocationList(data: any): any[] {
    const suggestions: any[] = [];
    const labels = new Set<string>();

    data.forEach((item: any) => {
      const country = (item.country || '').trim().toLowerCase();

      if (country !== 'tunisia') {
        return;
      }

      (item.cities || []).forEach((city: any) => {
        const cityName = (city || '').trim();

        if (!cityName || labels.has(cityName.toLowerCase())) {
          return;
        }

        suggestions.push({
          label: cityName,
          value: cityName,
          type: 'city',
        });
        labels.add(cityName.toLowerCase());
      });
    });

    return suggestions;
  }

  private buildCalendarMonths(): void {
    const firstMonth = new Date(
      this.calendarStartMonth.getFullYear(),
      this.calendarStartMonth.getMonth(),
      1,
    );

    const secondMonth = new Date(
      firstMonth.getFullYear(),
      firstMonth.getMonth() + 1,
      1,
    );

    this.calendarMonths = [
      this.createCalendarMonth(firstMonth),
      this.createCalendarMonth(secondMonth),
    ];
  }

  previousCalendarMonth(): void {
    if (!this.canGoToPreviousCalendarMonth()) {
      return;
    }

    this.calendarStartMonth = new Date(
      this.calendarStartMonth.getFullYear(),
      this.calendarStartMonth.getMonth() - 1,
      1,
    );
    this.buildCalendarMonths();
  }

  nextCalendarMonth(): void {
    this.calendarStartMonth = new Date(
      this.calendarStartMonth.getFullYear(),
      this.calendarStartMonth.getMonth() + 1,
      1,
    );
    this.buildCalendarMonths();
  }

  canGoToPreviousCalendarMonth(): boolean {
    const currentMonth = new Date();
    currentMonth.setDate(1);
    currentMonth.setHours(0, 0, 0, 0);

    const visibleMonth = new Date(
      this.calendarStartMonth.getFullYear(),
      this.calendarStartMonth.getMonth(),
      1,
    );
    visibleMonth.setHours(0, 0, 0, 0);

    return visibleMonth > currentMonth;
  }

  private createCalendarMonth(date: any): any {
    const firstDay = new Date(date.getFullYear(), date.getMonth(), 1);
    const lastDay = new Date(date.getFullYear(), date.getMonth() + 1, 0);
    const startOffset = (firstDay.getDay() + 6) % 7;
    const days: any[] = [];

    for (let i = 0; i < startOffset; i++) {
      days.push({ empty: true });
    }

    for (let day = 1; day <= lastDay.getDate(); day++) {
      const currentDate = new Date(date.getFullYear(), date.getMonth(), day);
      const dateValue = this.formatDateValue(currentDate);

      days.push({
        dayNumber: day,
        dateValue: dateValue,
        disabled: dateValue < this.todayDate,
      });
    }

    while (days.length % 7 !== 0) {
      days.push({ empty: true });
    }

    return {
      monthLabel: this.monthLabels[date.getMonth()],
      year: date.getFullYear(),
      days: days,
    };
  }

  private getTodayDate(): string {
    const today = new Date();
    return this.formatDateValue(today);
  }

  private refreshStepUi(): void {
    this.logWizardStep('Step opened', this.currentStep().key, {
      index: this.currentStepIndex,
    });

    if (this.isPickupStep()) {
      this.pickupSearch = this.pickupPoint || this.departureValue;
      this.pickupSearchError = '';
      this.pickupSuggestions = [];
      this.showPickupSuggestions = false;
      if (this.pickupMap) {
        this.pickupMap.remove();
        this.pickupMap = null;
        this.pickupMarker = null;
      }
      console.log('Pickup step opened');
      setTimeout(() => {
        this.setupPickupMap();
      }, 200);
    }

    if (this.isDropoffStep()) {
      this.dropoffSearch = this.dropoffPoint || this.destinationValue;
      this.dropoffSearchError = '';
      this.dropoffSuggestions = [];
      this.showDropoffSuggestions = false;
      if (this.dropoffMap) {
        this.dropoffMap.remove();
        this.dropoffMap = null;
        this.dropoffMarker = null;
      }
      console.log('Dropoff step opened');
      setTimeout(() => {
        this.setupDropoffMap();
      }, 200);
    }

    if (this.isRouteStep()) {
      this.routeError = '';
      this.routeLoading = true;
      this.routeSuggestions = [];
      this.selectedRouteIndex = 0;
      this.logWizardStep('Route calculation started', 'routeSelection');
      if (this.routeMap) {
        this.routeMap.remove();
        this.routeMap = null;
        this.routeLines = [];
        this.routeStartMarker = null;
        this.routeEndMarker = null;
      }
      setTimeout(() => {
        this.prepareRouteSuggestions();
      }, 200);
    }
  }

  private setupPickupMap(): void {
    if (typeof window === 'undefined' || !this.pickupMapElement) {
      console.log('Pickup map skipped', {
        hasWindow: typeof window !== 'undefined',
        hasElement: !!this.pickupMapElement,
      });
      return;
    }

    import('leaflet')
      .then((L) => {
        const defaultCenter: [number, number] = [36.8065, 10.1815];
        const locationQuery = this.buildRoutePointQuery('pickup') || 'Tunis';
        console.log('Leaflet loaded', locationQuery);

        this.dataService.getLocationCoordinates(locationQuery).subscribe({
          next: (response: any) => {
            let center: [number, number] = defaultCenter;
            let bounds: any = null;

            if (response && response.length > 0) {
              center = [Number(response[0].lat), Number(response[0].lon)];
              this.pickupCoordinates = {
                lat: center[0],
                lng: center[1],
              };
              if (
                response[0].boundingbox &&
                response[0].boundingbox.length === 4
              ) {
                bounds = [
                  [
                    Number(response[0].boundingbox[0]),
                    Number(response[0].boundingbox[2]),
                  ],
                  [
                    Number(response[0].boundingbox[1]),
                    Number(response[0].boundingbox[3]),
                  ],
                ];
              }
            }

            console.log('Pickup map center', center);
            console.log('Pickup map bounds', bounds);

            if (!this.pickupMap) {
              console.log('Creating pickup map');
              this.pickupMap = L.map(
                this.pickupMapElement.nativeElement,
              ).setView(center, 13);

              L.tileLayer(
                'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
                {
                  attribution: '&copy; OpenStreetMap contributors',
                },
              ).addTo(this.pickupMap);

              this.pickupMarker = L.marker(center, {
                icon: L.divIcon({
                  className: 'pickup-map-pin-wrapper',
                  html: '<div class="pickup-map-pin"><span class="pickup-map-pin-hole"></span></div><div class="pickup-map-pin-shadow"></div>',
                  iconSize: [46, 62],
                  iconAnchor: [23, 56],
                }),
              }).addTo(this.pickupMap);

              this.pickupMap.on('click', (event: any) => {
                const lat = event.latlng.lat.toFixed(4);
                const lng = event.latlng.lng.toFixed(4);
                console.log('Pickup map click', lat, lng);

                if (this.pickupMarker) {
                  this.pickupMarker.setLatLng(event.latlng);
                }

                this.pickupCoordinates = {
                  lat: event.latlng.lat,
                  lng: event.latlng.lng,
                };

                this.dataService
                  .getLocationName(event.latlng.lat, event.latlng.lng)
                  .subscribe({
                    next: (locationResponse: any) => {
                      const locationName = this.formatPickupLocationName(
                        locationResponse,
                        this.departureValue,
                      );

                      console.log('Pickup location name', locationName);
                      this.pickupConfirmed = true;
                      this.pickupPoint = locationName;
                      this.pickupSearch = locationName;
                    },
                    error: (error: any) => {
                      console.log('Pickup reverse geocoding failed', error);
                      this.pickupConfirmed = true;
                      this.pickupPoint =
                        this.departureValue || 'Selected pickup point';
                      this.pickupSearch = this.pickupPoint;
                    },
                  });
              });
            } else {
              console.log('Pickup map already exists, recentering');
              this.pickupMap.invalidateSize();
              if (this.pickupMarker) {
                this.pickupMarker.setLatLng(center);
              }
            }

            if (bounds) {
              this.pickupMap.fitBounds(bounds, {
                padding: [40, 40],
              });
            } else {
              this.pickupMap.setView(center, 13);
            }

            setTimeout(() => {
              if (this.pickupMap) {
                console.log('Pickup map invalidateSize after render');
                this.pickupMap.invalidateSize();
              }
            }, 300);
          },
          error: (error: any) => {
            console.log('Pickup geocoding failed', error);
          },
        });
      })
      .catch((error) => {
        console.log('Leaflet load failed', error);
      });
  }

  private setupDropoffMap(): void {
    if (typeof window === 'undefined' || !this.dropoffMapElement) {
      console.log('Dropoff map skipped', {
        hasWindow: typeof window !== 'undefined',
        hasElement: !!this.dropoffMapElement,
      });
      return;
    }

    import('leaflet')
      .then((L) => {
        const defaultCenter: [number, number] = [36.8065, 10.1815];
        const locationQuery = this.buildRoutePointQuery('dropoff') || 'Tunis';
        console.log('Dropoff Leaflet loaded', locationQuery);

        this.dataService.getLocationCoordinates(locationQuery).subscribe({
          next: (response: any) => {
            let center: [number, number] = defaultCenter;
            let bounds: any = null;

            if (response && response.length > 0) {
              center = [Number(response[0].lat), Number(response[0].lon)];
              this.dropoffCoordinates = {
                lat: center[0],
                lng: center[1],
              };
              if (
                response[0].boundingbox &&
                response[0].boundingbox.length === 4
              ) {
                bounds = [
                  [
                    Number(response[0].boundingbox[0]),
                    Number(response[0].boundingbox[2]),
                  ],
                  [
                    Number(response[0].boundingbox[1]),
                    Number(response[0].boundingbox[3]),
                  ],
                ];
              }
            }

            console.log('Dropoff map center', center);
            console.log('Dropoff map bounds', bounds);

            if (!this.dropoffMap) {
              console.log('Creating dropoff map');
              this.dropoffMap = L.map(
                this.dropoffMapElement.nativeElement,
              ).setView(center, 13);

              L.tileLayer(
                'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
                {
                  attribution: '&copy; OpenStreetMap contributors',
                },
              ).addTo(this.dropoffMap);

              this.dropoffMarker = L.marker(center, {
                icon: L.divIcon({
                  className: 'pickup-map-pin-wrapper',
                  html: '<div class="pickup-map-pin"><span class="pickup-map-pin-hole"></span></div><div class="pickup-map-pin-shadow"></div>',
                  iconSize: [46, 62],
                  iconAnchor: [23, 56],
                }),
              }).addTo(this.dropoffMap);

              this.dropoffMap.on('click', (event: any) => {
                const lat = event.latlng.lat.toFixed(4);
                const lng = event.latlng.lng.toFixed(4);
                console.log('Dropoff map click', lat, lng);

                if (this.dropoffMarker) {
                  this.dropoffMarker.setLatLng(event.latlng);
                }

                this.dropoffCoordinates = {
                  lat: event.latlng.lat,
                  lng: event.latlng.lng,
                };

                this.dataService
                  .getLocationName(event.latlng.lat, event.latlng.lng)
                  .subscribe({
                    next: (locationResponse: any) => {
                      const locationName = this.formatPickupLocationName(
                        locationResponse,
                        this.destinationValue,
                      );

                      console.log('Dropoff location name', locationName);
                      this.dropoffConfirmed = true;
                      this.dropoffPoint = locationName;
                      this.dropoffSearch = locationName;
                    },
                    error: (error: any) => {
                      console.log('Dropoff reverse geocoding failed', error);
                      this.dropoffConfirmed = true;
                      this.dropoffPoint =
                        this.destinationValue || 'Selected dropoff point';
                      this.dropoffSearch = this.dropoffPoint;
                    },
                  });
              });
            } else {
              console.log('Dropoff map already exists, recentering');
              this.dropoffMap.invalidateSize();
              if (this.dropoffMarker) {
                this.dropoffMarker.setLatLng(center);
              }
            }

            if (bounds) {
              this.dropoffMap.fitBounds(bounds, {
                padding: [40, 40],
              });
            } else {
              this.dropoffMap.setView(center, 13);
            }

            setTimeout(() => {
              if (this.dropoffMap) {
                console.log('Dropoff map invalidateSize after render');
                this.dropoffMap.invalidateSize();
              }
            }, 300);
          },
          error: (error: any) => {
            console.log('Dropoff geocoding failed', error);
          },
        });
      })
      .catch((error) => {
        console.log('Dropoff Leaflet load failed', error);
      });
  }

  selectRouteSuggestion(index: number): void {
    this.selectedRouteIndex = index;
    this.logWizardStep('Route selected', 'routeSelection', {
      index: index,
      label: this.routeSuggestions[index]
        ? this.routeSuggestions[index].label
        : '',
    });
    this.updateRouteMapStyles();
    this.refreshDriverDemandAlert();
    this.refreshPriceSuggestion();
  }

  private prepareRouteSuggestions(): void {
    this.ensureRoutePoint('pickup', () => {
      this.ensureRoutePoint('dropoff', () => {
        if (!this.pickupCoordinates || !this.dropoffCoordinates) {
          this.routeLoading = false;
          this.routeError = 'Unable to calculate routes.';
          this.logWizardStep('Route calculation failed', 'routeSelection');
          return;
        }

        this.dataService
          .getRouteSuggestions(
            this.pickupCoordinates.lat,
            this.pickupCoordinates.lng,
            this.dropoffCoordinates.lat,
            this.dropoffCoordinates.lng,
          )
          .subscribe({
            next: (routes: any[]) => {
              this.routeSuggestions = routes || [];
              this.routeLoading = false;
              this.logWizardStep(
                'Route calculation completed',
                'routeSelection',
                {
                  count: this.routeSuggestions.length,
                },
              );

              if (this.routeSuggestions.length === 0) {
                this.routeError = 'No route available.';
                this.logWizardStep('Route calculation empty', 'routeSelection');
                this.refreshPriceSuggestion();
                return;
              }

              this.selectedRouteIndex = 0;
              this.setupRouteMap();
              this.refreshPriceSuggestion();
            },
            error: (error: any) => {
              this.routeLoading = false;
              this.routeError = this.extractPublishError(error);
              this.logWizardStep('Route calculation failed', 'routeSelection', {
                error: this.routeError,
              });
            },
          });
      });
    });
  }

  private ensureRoutePoint(type: string, done: () => void): void {
    if (type === 'pickup' && this.pickupCoordinates) {
      done();
      return;
    }

    if (type === 'dropoff' && this.dropoffCoordinates) {
      done();
      return;
    }

    const query = this.buildRoutePointQuery(type);

    if (!query) {
      done();
      return;
    }

    this.logWizardStep('Route point lookup', type, {
      query: query,
    });

    this.dataService.getLocationCoordinates(query).subscribe({
      next: (response: any) => {
        if (response && response.length > 0) {
          const coordinates = {
            lat: Number(response[0].lat),
            lng: Number(response[0].lon),
          };

          if (type === 'pickup') {
            this.pickupCoordinates = coordinates;
          } else {
            this.dropoffCoordinates = coordinates;
          }
        }

        done();
      },
      error: () => {
        done();
      },
    });
  }

  private setupRouteMap(): void {
    if (
      typeof window === 'undefined' ||
      !this.routeMapElement ||
      this.routeSuggestions.length === 0
    ) {
      return;
    }

    import('leaflet').then((L) => {
      const boundsPoints = [
        [this.pickupCoordinates.lat, this.pickupCoordinates.lng],
        [this.dropoffCoordinates.lat, this.dropoffCoordinates.lng],
      ];

      this.routeMap = L.map(this.routeMapElement.nativeElement);

      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; OpenStreetMap contributors',
      }).addTo(this.routeMap);

      this.routeLines = [];

      for (let i = 0; i < this.routeSuggestions.length; i++) {
        const route = this.routeSuggestions[i];
        console.log('ORS geometry:', route.coordinates.length);
        const routePath = route.coordinates.map((coord: any) => [
          coord[1],
          coord[0],
        ]);
        const line = L.polyline(routePath, {
          color: this.getRouteLineColor(i),
          weight: i === this.selectedRouteIndex ? 7 : 5,
          opacity: i === this.selectedRouteIndex ? 1 : 0.9,
          dashArray:
            i === this.selectedRouteIndex
              ? undefined
              : this.getRouteLineDash(i),
        }).addTo(this.routeMap);

        this.routeLines.push(line);
      }

      this.routeStartMarker = L.circleMarker(
        [this.pickupCoordinates.lat, this.pickupCoordinates.lng],
        {
          radius: 10,
          color: '#05204a',
          fillColor: '#ffffff',
          fillOpacity: 1,
          weight: 4,
        },
      ).addTo(this.routeMap);

      this.routeEndMarker = L.circleMarker(
        [this.dropoffCoordinates.lat, this.dropoffCoordinates.lng],
        {
          radius: 10,
          color: '#0b6ff2',
          fillColor: '#ffffff',
          fillOpacity: 1,
          weight: 4,
        },
      ).addTo(this.routeMap);

      this.routeMap.fitBounds(boundsPoints, {
        padding: [60, 60],
      });

      setTimeout(() => {
        if (this.routeMap) {
          this.routeMap.invalidateSize();
        }
      }, 300);
    });
  }

  private updateRouteMapStyles(): void {
    if (!this.routeLines || this.routeLines.length === 0) {
      return;
    }

    for (let i = 0; i < this.routeLines.length; i++) {
      this.routeLines[i].setStyle({
        color: this.getRouteLineColor(i),
        weight: i === this.selectedRouteIndex ? 7 : 5,
        opacity: i === this.selectedRouteIndex ? 1 : 0.9,
        dashArray:
          i === this.selectedRouteIndex ? undefined : this.getRouteLineDash(i),
      });

      if (i === this.selectedRouteIndex) {
        this.routeLines[i].bringToFront();
      }
    }
  }

  private getRouteLineColor(index: number): string {
    if (index === this.selectedRouteIndex) {
      return '#0b6ff2';
    }

    const colors = ['#7a8598', '#c95b17', '#2b8a57'];
    return colors[index] || '#7a8598';
  }

  private getRouteLineDash(index: number): string {
    const dashStyles = ['12 10', '6 10', '18 12'];
    return dashStyles[index] || '10 10';
  }

  private extractPublishError(error: any): string {
    if (error && error.error && typeof error.error.message === 'string') {
      return this.cleanBackendErrorMessage(error.error.message);
    }

    return 'Unable to publish trip.';
  }

  formatVerificationDate(): string {
    if (!this.departureDate) {
      return 'Trip date';
    }

    const date = new Date(`${this.departureDate}T00:00:00`);
    const days = ['Sun.', 'Mon.', 'Tue.', 'Wed.', 'Thu.', 'Fri.', 'Sat.'];
    const months = [
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

    return `${days[date.getDay()]} ${date.getDate()} ${months[date.getMonth()]}`;
  }

  formatArrivalTime(): string {
    if (!this.departureTime) {
      return '--:--';
    }

    const selectedRoute =
      this.routeSuggestions && this.routeSuggestions[this.selectedRouteIndex]
        ? this.routeSuggestions[this.selectedRouteIndex]
        : null;
    const tripMinutes =
      selectedRoute && selectedRoute.durationMinutes
        ? Number(selectedRoute.durationMinutes)
        : 20;
    const timeParts = this.departureTime.split(':');
    const hours = Number(timeParts[0]);
    const minutes = Number(timeParts[1]);
    const totalMinutes = hours * 60 + minutes + Math.round(tripMinutes);
    const nextHours = `${Math.floor((totalMinutes % 1440) / 60)}`.padStart(
      2,
      '0',
    );
    const nextMinutes = `${totalMinutes % 60}`.padStart(2, '0');
    return `${nextHours}:${nextMinutes}`;
  }

  isArrivalNextDay(): boolean {
    if (!this.departureTime) {
      return false;
    }

    const selectedRoute =
      this.routeSuggestions && this.routeSuggestions[this.selectedRouteIndex]
        ? this.routeSuggestions[this.selectedRouteIndex]
        : null;
    const tripMinutes =
      selectedRoute && selectedRoute.durationMinutes
        ? Number(selectedRoute.durationMinutes)
        : 20;
    const timeParts = this.departureTime.split(':');
    const hours = Number(timeParts[0]);
    const minutes = Number(timeParts[1]);
    const totalMinutes = hours * 60 + minutes + Math.round(tripMinutes);

    return totalMinutes >= 1440;
  }

  formatMainPlaceName(value: string): string {
    if (!value) {
      return 'Location';
    }

    const cleanedValue = value.split('/')[0];
    const parts = cleanedValue
      .split(',')
      .map((part) => part.trim())
      .filter((part) => !!part);

    if (parts.length === 0) {
      return value;
    }

    return parts[0];
  }

  getDriverDemandLevelLabel(): string {
    if (!this.driverDemandAlert) {
      return '';
    }

    if (this.driverDemandAlert.holidayCriticalWarning) {
      return 'Holiday alert';
    }
    if (this.driverDemandAlert.demandLevel === 'high') {
      return 'High demand';
    }
    if (this.driverDemandAlert.demandLevel === 'medium') {
      return 'Medium demand';
    }
    return 'Low demand';
  }

  getDriverDemandOccupancyPercent(): number {
    if (!this.driverDemandAlert) {
      return 0;
    }

    return Math.round((this.driverDemandAlert.predictedOccupancyRate || 0) * 100);
  }

  shouldShowDriverDemandAlert(): boolean {
    return !!this.driverDemandAlert
      && (this.driverDemandAlert.demandLevel === 'high'
        || this.driverDemandAlert.holidayCriticalWarning
        || !!this.driverDemandAlert.suggestedDateFrom);
  }

  private cleanBackendErrorMessage(message: string): string {
    if (message.indexOf('ORS route request failed: ') === 0) {
      const jsonPart = message.replace('ORS route request failed: ', '').trim();

      try {
        const parsed = JSON.parse(jsonPart);
        if (
          parsed &&
          parsed.error &&
          typeof parsed.error.message === 'string'
        ) {
          return parsed.error.message;
        }
      } catch {
        return 'Route request failed.';
      }
    }

    return message;
  }

  private logWizardStep(message: string, stepKey: string, data?: any): void {
    if (data) {
      console.log('[PublishTrip]', message, stepKey, data);
      return;
    }

    console.log('[PublishTrip]', message, stepKey);
  }

  private formatDateValue(date: any): string {
    if (!date) {
      return '';
    }

    const dateValue = date instanceof Date ? date : new Date(date);
    if (Number.isNaN(dateValue.getTime())) {
      return '';
    }

    const year = dateValue.getFullYear();
    const month = `${dateValue.getMonth() + 1}`.padStart(2, '0');
    const day = `${dateValue.getDate()}`.padStart(2, '0');

    return `${year}-${month}-${day}`;
  }

  private formatPickupLocationName(
    locationResponse: any,
    fallbackValue: string = '',
  ): string {
    const address =
      locationResponse && locationResponse.address
        ? locationResponse.address
        : {};

    const streetPart = address.road || '';
    const areaPart =
      address.neighbourhood ||
      address.suburb ||
      address.city_district ||
      address.village ||
      address.town ||
      '';
    const cityPart = address.city || address.town || address.county || '';
    const statePart =
      address.state_district || address.state || address.country || '';

    const firstPart = [streetPart, areaPart].filter((part: string) => !!part);
    const secondPart = [cityPart, statePart].filter(
      (part: string, index: number, parts: string[]) =>
        !!part && parts.indexOf(part) === index,
    );

    if (firstPart.length > 0 && secondPart.length > 0) {
      return `${firstPart.join(' ')}, ${secondPart.join('/')}`;
    }

    if (firstPart.length > 0) {
      return firstPart.join(' ');
    }

    if (secondPart.length > 0) {
      return secondPart.join('/');
    }

    if (locationResponse && locationResponse.display_name) {
      const displayParts = locationResponse.display_name.split(',');
      return displayParts.slice(0, 2).join(',').trim();
    }

    return fallbackValue || 'Selected location';
  }

  private buildPickupSuggestions(
    query: string,
    results: any[],
    referenceValue: string,
  ): any[] {
    const normalizedQuery = query.toLowerCase();
    const normalizedReference = referenceValue.toLowerCase();
    const suggestions: any[] = [];
    const usedLabels: string[] = [];

    for (let i = 0; i < results.length; i++) {
      const result = results[i];
      const label = this.formatPickupSuggestionLabel(result);
      const helper = this.formatPickupSuggestionHelper(result);
      const searchText =
        `${label} ${helper} ${result.display_name || ''}`.toLowerCase();
      let score = 0;
      const firstDisplayPart = this.getFirstDisplayPart(result).toLowerCase();

      if (label.toLowerCase() === normalizedQuery) {
        score += 120;
      }

      if (firstDisplayPart === normalizedQuery) {
        score += 100;
      }

      if (label.toLowerCase().startsWith(normalizedQuery)) {
        score += 80;
      }

      if (firstDisplayPart.startsWith(normalizedQuery)) {
        score += 70;
      }

      if (searchText.includes(normalizedQuery)) {
        score += 40;
      }

      if (normalizedReference && searchText.includes(normalizedReference)) {
        score += 35;
      }

      if (
        result.class === 'amenity' ||
        result.class === 'building' ||
        result.class === 'office'
      ) {
        score += 18;
      }

      if (
        result.type === 'university' ||
        result.type === 'school' ||
        result.type === 'college'
      ) {
        score += 30;
      }

      if (result.address && result.address.road) {
        score += 10;
      }

      if (usedLabels.indexOf(label.toLowerCase()) === -1) {
        usedLabels.push(label.toLowerCase());
        suggestions.push({
          label: label,
          helper: helper,
          result: result,
          score: score,
        });
      }
    }

    suggestions.sort((a: any, b: any) => b.score - a.score);

    return suggestions.slice(0, 5);
  }

  private formatPickupSuggestionHelper(locationResponse: any): string {
    const address =
      locationResponse && locationResponse.address
        ? locationResponse.address
        : {};
    const helperParts = [
      address.city || address.town || address.county || '',
      address.state || address.country || '',
    ].filter((part: string, index: number, parts: string[]) => {
      return !!part && parts.indexOf(part) === index;
    });

    if (helperParts.length > 0) {
      return helperParts.join(', ');
    }

    if (locationResponse && locationResponse.display_name) {
      const displayParts = locationResponse.display_name.split(',');
      return displayParts.slice(1, 3).join(',').trim();
    }

    return '';
  }

  private formatPickupSuggestionLabel(locationResponse: any): string {
    const firstDisplayPart = this.getFirstDisplayPart(locationResponse);
    const secondDisplayPart = this.getSecondDisplayPart(locationResponse);

    if (firstDisplayPart && secondDisplayPart) {
      return `${firstDisplayPart}, ${secondDisplayPart}`;
    }

    if (firstDisplayPart) {
      return firstDisplayPart;
    }

    return this.formatPickupLocationName(locationResponse);
  }

  private getFirstDisplayPart(locationResponse: any): string {
    if (
      locationResponse &&
      locationResponse.namedetails &&
      locationResponse.namedetails.name
    ) {
      return locationResponse.namedetails.name;
    }

    if (locationResponse && locationResponse.name) {
      return locationResponse.name;
    }

    if (locationResponse && locationResponse.display_name) {
      const displayParts = locationResponse.display_name.split(',');
      return displayParts[0] ? displayParts[0].trim() : '';
    }

    return '';
  }

  private getSecondDisplayPart(locationResponse: any): string {
    if (locationResponse && locationResponse.display_name) {
      const displayParts = locationResponse.display_name.split(',');
      return displayParts[1] ? displayParts[1].trim() : '';
    }

    return '';
  }

  private buildPickupSearchQuery(value: string): string {
    const normalizedValue = value.toLowerCase();

    if (
      normalizedValue.includes('tunisia') ||
      normalizedValue.includes('tunis') ||
      normalizedValue.includes('ariana')
    ) {
      return value;
    }

    return `${value} Tunisia`;
  }

  private buildSavedLocation(baseValue: string, exactValue: string): string {
    const firstPart = (baseValue || '').trim();
    const secondPart = (exactValue || '').trim();

    if (firstPart && secondPart) {
      return `${firstPart}, ${secondPart}`;
    }

    if (firstPart) {
      return firstPart;
    }

    return secondPart;
  }

  private buildRoutePointQuery(type: string): string {
    const mainValue =
      type === 'pickup'
        ? (this.departureValue || '').trim()
        : (this.destinationValue || '').trim();
    const exactValue =
      type === 'pickup'
        ? (this.pickupPoint || '').trim()
        : (this.dropoffPoint || '').trim();

    if (mainValue && exactValue) {
      const normalizedMain = mainValue.toLowerCase();
      const normalizedExact = exactValue.toLowerCase();

      if (normalizedExact.indexOf(normalizedMain) !== -1) {
        return exactValue;
      }

      return `${mainValue}, ${exactValue}`;
    }

    if (exactValue) {
      return exactValue;
    }

    return mainValue;
  }

  private applyTripToWizard(trip: Trip): void {
    const departureParts = this.splitStoredLocation(trip.departure);
    const destinationParts = this.splitStoredLocation(trip.destination);
    const departureDate = new Date(trip.departureDateTime);

    this.departureValue = departureParts.main;
    this.departureSearch = departureParts.main;
    this.pickupPoint = departureParts.detail;
    this.pickupSearch = departureParts.detail || departureParts.main;
    this.pickupConfirmed = !!departureParts.detail;
    this.pickupCoordinates = null;

    this.destinationValue = destinationParts.main;
    this.destinationSearch = destinationParts.main;
    this.dropoffPoint = destinationParts.detail;
    this.dropoffSearch = destinationParts.detail || destinationParts.main;
    this.dropoffConfirmed = !!destinationParts.detail;
    this.dropoffCoordinates = null;

    this.departureDate = this.formatDateValue(departureDate);
    this.departureTime = this.formatTimeFromDateValue(departureDate);
    this.pricePerSeat = trip.pricePerSeat;
    this.priceTouched = true;
    this.seatsTotal = trip.seatsTotal;
    this.bookingMode = trip.bookingMode || 'manual';
    this.existingDurationMinutes = trip.durationMinutes;
    this.messageText = '';
    this.routeSuggestions = [];
    this.selectedRouteIndex = 0;
    this.routeError = '';
    this.publishError = '';
    this.refreshDriverDemandAlert();
    this.refreshPriceSuggestion();
  }

  private splitStoredLocation(value: string): { main: string; detail: string } {
    const parts = (value || '')
      .split(',')
      .map((part) => part.trim())
      .filter((part) => !!part);

    if (parts.length <= 1) {
      return {
        main: value || '',
        detail: '',
      };
    }

    return {
      main: parts[0],
      detail: parts.slice(1).join(', '),
    };
  }

  private formatTimeFromDateValue(date: Date): string {
    const hours = `${date.getHours()}`.padStart(2, '0');
    const minutes = `${date.getMinutes()}`.padStart(2, '0');

    return `${hours}:${minutes}`;
  }

  private refreshDriverDemandAlert(): void {
    const departure = (this.departureValue || '').trim();
    const destination = (this.destinationValue || '').trim();
    const dateFrom = (this.departureDate || '').trim() || undefined;

    if (!departure || !destination) {
      this.driverDemandAlert = undefined;
      this.driverDemandLoading = false;
      this.driverDemandError = '';
      return;
    }

    this.driverDemandLoading = true;
    this.driverDemandError = '';
    this.dataService.getDemandAlert(departure, destination, dateFrom).subscribe({
      next: (alert) => {
        this.driverDemandLoading = false;
        this.driverDemandAlert = alert;
      },
      error: () => {
        this.driverDemandLoading = false;
        this.driverDemandAlert = undefined;
        this.driverDemandError = 'Demand insight unavailable.';
      },
    });
  }

  private refreshPriceSuggestion(): void {
    const departure = (this.departureValue || '').trim();
    const destination = (this.destinationValue || '').trim();
    const departureDate = (this.departureDate || '').trim();
    const durationMinutes = this.getSelectedTripDurationMinutes();

    if (!departure || !destination || !departureDate || !durationMinutes) {
      this.priceSuggestion = undefined;
      this.priceSuggestionLoading = false;
      this.priceSuggestionError = '';
      return;
    }

    const previousSuggestedPrice = this.priceSuggestion?.suggestedPrice;
    this.priceSuggestionLoading = true;
    this.priceSuggestionError = '';
    this.dataService
      .getTripPriceSuggestion(
        departure,
        destination,
        departureDate,
        durationMinutes,
      )
      .subscribe({
        next: (suggestion) => {
          this.priceSuggestionLoading = false;
          this.priceSuggestion = suggestion;
          if (
            suggestion &&
            !this.priceTouched &&
            (!this.pricePerSeat ||
              this.pricePerSeat <= 1 ||
              this.pricePerSeat === previousSuggestedPrice)
          ) {
            this.pricePerSeat = suggestion.suggestedPrice;
          }
        },
        error: () => {
          this.priceSuggestionLoading = false;
          this.priceSuggestion = undefined;
          this.priceSuggestionError = 'Price suggestion unavailable.';
        },
      });
  }

  private getSelectedTripDurationMinutes(): number | undefined {
    const selectedRoute =
      this.routeSuggestions && this.routeSuggestions[this.selectedRouteIndex]
        ? this.routeSuggestions[this.selectedRouteIndex]
        : null;

    if (selectedRoute && selectedRoute.durationMinutes) {
      return Number(selectedRoute.durationMinutes);
    }

    if (this.existingDurationMinutes && this.existingDurationMinutes > 0) {
      return Number(this.existingDurationMinutes);
    }

    return undefined;
  }
}
