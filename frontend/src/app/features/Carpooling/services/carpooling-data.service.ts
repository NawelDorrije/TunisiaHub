import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, forkJoin, of } from 'rxjs';
import { catchError, map, shareReplay, switchMap } from 'rxjs/operators';
import {
  AdminStats,
  Booking,
  BookingWithContext,
  CarpoolUser,
  Complaint,
  ComplaintStatus,
  ComplaintWithContext,
  DriverReview,
  DriverReviewSummary,
  ReservationQuote,
  Trip,
  TripSearchFilters,
} from '../../../models/Carpooling/carpooling';

export interface CountryCityData {
  country: string;
  cities: string[];
}

interface TripApi {
  id: number;
  departure: string;
  destination: string;
  departureDateTime: string;
  durationMinutes?: number;
  price: number;
  seatsTotal: number;
  status: string;
  driver?: { id?: number; nom?: string; prenom?: string };
  bookingMode?: string;
}

interface ReservationApi {
  id: number;
  status: string;
  startDate?: string;
  endDate?: string;
  totalPrice: number;
  numberOfPeople?: number;
  type: string;
  trip?: number | { id: number };
  reservedBy?: { id?: number; nom?: string; prenom?: string; email?: string };
}

interface ComplaintApi {
  id: number;
  description: string;
  date: string;
  reportedByUserId: string;
  reservation?: number | { id: number };
  status?: string;
}

interface ReservationQuoteApi {
  seatsRequested: number;
  driverAmount: number;
  serviceFee: number;
  totalAmount: number;
}

interface DriverReviewApi {
  id: number;
  comment: string;
  rating: number;
  date: string;
  reservationId: number;
  reviewerName?: string;
}

interface DriverReviewSummaryApi {
  averageRating: number;
  reviewsCount: number;
}

@Injectable({
  providedIn: 'root',
})
export class CarpoolingDataService {
  private readonly baseUrl = 'http://localhost:8089';
  private readonly tunisiaCities = [
    'Tunis',
    'Le Bardo',
    'La Marsa',
    'La Goulette',
    'Carthage',
    'Ariana',
    'Raoued',
    'La Soukra',
    'Ben Arous',
    'Hammam Lif',
    'Radès',
    'Mourouj',
    'Manouba',
    'Den Den',
    'Douar Hicher',
    'Nabeul',
    'Hammamet',
    'Kelibia',
    'Korba',
    'Menzel Temime',
    'Bizerte',
    'Menzel Bourguiba',
    'Ras Jebel',
    'Beja',
    'Medjez el Bab',
    'Jendouba',
    'Tabarka',
    'Ain Draham',
    'Kef',
    'Dahmani',
    'Siliana',
    'Makthar',
    'Sousse',
    'Akouda',
    'Kalaa Kebira',
    'Monastir',
    'Ksibet el Mediouni',
    'Moknine',
    'Jemmal',
    'Mahdia',
    'Chebba',
    'El Jem',
    'Kairouan',
    'Sbikha',
    'Sfax',
    'Sakiet Ezzit',
    'Sakiet Eddaier',
    'Mahres',
    'Gabes',
    'Mareth',
    'Metouia',
    'Medenine',
    'Ben Gardane',
    'Zarzis',
    'Djerba',
    'Houmt Souk',
    'Midoun',
    'Ajim',
    'Tataouine',
    'Gafsa',
    'Metlaoui',
    'Redeyef',
    'Tozeur',
    'Nefta',
    'Kebili',
    'Douz',
    'Kasserine',
    'Sbeitla',
    'Sidi Bouzid',
    'Regueb',
    'Zaghouan',
  ];
  private countriesAndCities$?: Observable<CountryCityData[]>;

  constructor(private readonly http: HttpClient) {}

  getCurrentUser(): CarpoolUser {
    const currentUserId = this.getCurrentUserId();
    return {
      id: currentUserId,
      fullName: `User ${currentUserId}`,
      email: `user${currentUserId}@example.com`,
    };
  }

  getUserById(userId: number): CarpoolUser {
    return {
      id: userId,
      fullName: `User ${userId}`,
      email: `user${userId}@example.com`,
    };
  }

  getTripById(tripId: number): Observable<Trip | undefined> {
    return this.http
      .get<TripApi>(`${this.baseUrl}/api/carpooling/trips/${tripId}`)
      .pipe(
        map((trip) => this.mapTrip(trip)),
        switchMap((trip) => this.setSeatsAvailableTrip(trip)),
        switchMap((trip) => this.setDriverReviewSummaryTrip(trip)),
        catchError(() => of(undefined)),
      );
  }

  getMyTrips(): Observable<Trip[]> {
    return this.http
      .get<TripApi[]>(`${this.baseUrl}/api/driver/trips`, {
        headers: this.userHeaders(),
      })
      .pipe(
        map((response) => (response ?? []).map((trip) => this.mapTrip(trip))),
        switchMap((trips) => this.setSeatsAvailableTrips(trips)),
        switchMap((trips) => this.setDriverReviewSummaryTrips(trips)),
      );
  }

  searchTrips(filters: TripSearchFilters): Observable<Trip[]> {
    let params = new HttpParams();
    if (filters.departure) {
      params = params.set('departurePoint', filters.departure);
    }
    if (filters.destination) {
      params = params.set('destination', filters.destination);
    }
    if (filters.dateFrom) {
      params = params.set('dateFrom', filters.dateFrom);
    }
    if (filters.dateTo) {
      params = params.set('dateTo', filters.dateTo);
    }
    if (filters.seatsNeeded && filters.seatsNeeded > 0) {
      params = params.set('seatsRequired', filters.seatsNeeded);
    }
    if (filters.status) {
      params = params.set('status', filters.status);
    }
    if (filters.bookingMode) {
      params = params.set('bookingMode', filters.bookingMode);
    }
    if (filters.minPrice !== undefined && filters.minPrice !== null && filters.minPrice >= 0) {
      params = params.set('minPrice', filters.minPrice);
    }
    if (filters.maxPrice && filters.maxPrice > 0) {
      params = params.set('maxPrice', filters.maxPrice);
    }
    if (filters.durationMax && filters.durationMax > 0) {
      params = params.set('durationMax', filters.durationMax);
    }

    return this.http
      .get<TripApi[]>(`${this.baseUrl}/api/carpooling/trips`, { params })
      .pipe(
        map((response) => (response ?? []).map((trip) => this.mapTrip(trip))),
        switchMap((trips) => this.setSeatsAvailableTrips(trips)),
        switchMap((trips) => this.setDriverReviewSummaryTrips(trips)),
      );
  }

  publishTrip(
    payload: Omit<Trip, 'id' | 'ownerUserId' | 'status'>,
  ): Observable<Trip> {
    const body = {
      departure: payload.departure,
      destination: payload.destination,
      departureDateTime: payload.departureDateTime,
      durationMinutes: payload.durationMinutes,
      price: payload.pricePerSeat,
      seatsTotal: payload.seatsTotal,
      bookingMode: payload.bookingMode,
    };

    return this.http
      .post<TripApi>(`${this.baseUrl}/api/driver/trips`, body, {
        headers: this.userHeaders(),
      })
      .pipe(map((trip) => this.mapTrip(trip)));
  }

  getRouteSuggestions(
    startLat: number,
    startLng: number,
    endLat: number,
    endLng: number,
  ): Observable<any[]> {
    const params = new HttpParams()
      .set('startLat', startLat)
      .set('startLng', startLng)
      .set('endLat', endLat)
      .set('endLng', endLng);

    return this.http.get<any[]>(
      `${this.baseUrl}/api/driver/trips/route-suggestions`,
      {
        headers: this.userHeaders(),
        params,
      },
    );
  }

  getCountriesAndCities(): Observable<CountryCityData[]> {
    if (!this.countriesAndCities$) {
      this.countriesAndCities$ = this.http
        .get<{
          data: CountryCityData[];
        }>('https://countriesnow.space/api/v0.1/countries')
        .pipe(
          map((response) => response.data || []),
          shareReplay(1),
        );
    }

    return this.countriesAndCities$;
  }

  getLocationCoordinates(query: string, limit: number = 1): Observable<any> {
    return this.http.get(
      `https://nominatim.openstreetmap.org/search?format=json&addressdetails=1&namedetails=1&accept-language=en&q=${encodeURIComponent(query)}&limit=${limit}`,
    );
  }

  getTunisiaLocationSuggestions(
    query: string,
    limit: number = 8,
  ): Observable<any> {
    return this.http.get(
      `https://nominatim.openstreetmap.org/search?format=json&addressdetails=1&namedetails=1&accept-language=en&countrycodes=tn&viewbox=7.4,37.6,11.7,30.1&bounded=1&dedupe=1&q=${encodeURIComponent(query)}&limit=${limit}`,
    );
  }

  searchTunisiaCities(query: string, limit: number = 8): Observable<string[]> {
    const value = (query || '').trim();
    if (!value) {
      console.log('[Carpooling] Tunisia city search skipped', { query: value });
      return of([]);
    }

    console.log('[Carpooling] Tunisia city search', {
      query: value,
      limit,
    });

    const cities = this.filterTunisiaCityNames(
      this.tunisiaCities,
      value,
      limit,
    );

    console.log('[Carpooling] Tunisia city search result', {
      query: value,
      result: cities,
    });

    return of(cities);
  }

  private filterTunisiaCityNames(
    names: string[],
    query: string,
    limit: number,
  ): string[] {
    const labels = new Set<string>();
    const normalizedQuery = query.toLowerCase();

    return (names || [])
      .filter((city) => {
        const normalized = city.toLowerCase();
        if (
          !normalized ||
          labels.has(normalized) ||
          !normalized.includes(normalizedQuery)
        ) {
          return false;
        }

        labels.add(normalized);
        return true;
      })
      .sort((a, b) => {
        const aLower = a.toLowerCase();
        const bLower = b.toLowerCase();
        const aStarts = aLower.startsWith(normalizedQuery);
        const bStarts = bLower.startsWith(normalizedQuery);

        if (aStarts && !bStarts) {
          return -1;
        }
        if (!aStarts && bStarts) {
          return 1;
        }

        return a.localeCompare(b);
      })
      .slice(0, limit);
  }

  getLocationName(lat: number, lng: number): Observable<any> {
    return this.http.get(
      `https://nominatim.openstreetmap.org/reverse?format=json&addressdetails=1&namedetails=1&accept-language=en&lat=${lat}&lon=${lng}`,
    );
  }

  canEditTrip(trip: Trip): { allowed: boolean; reason?: string } {
    if (trip.ownerUserId !== this.getCurrentUserId()) {
      return { allowed: false, reason: 'Only trip owner can edit this ride.' };
    }
    if (trip.status === 'CANCELED') {
      return { allowed: false, reason: 'Canceled trips are not editable.' };
    }
    if (new Date(trip.departureDateTime).getTime() <= Date.now()) {
      return { allowed: false, reason: 'Only future trips can be edited.' };
    }
    return { allowed: true };
  }

  updateTrip(
    tripId: number,
    patch: Partial<
      Pick<
        Trip,
        | 'departure'
        | 'destination'
        | 'departureDateTime'
        | 'durationMinutes'
        | 'pricePerSeat'
        | 'seatsTotal'
        | 'bookingMode'
      >
    >,
  ): Observable<{ ok: boolean; error?: string; trip?: Trip }> {
    return this.getTripById(tripId).pipe(
      switchMap((existing) => {
        if (!existing) {
          return of({ ok: false, error: 'Trip not found.' });
        }

        const body = {
          departure: patch.departure ?? existing.departure,
          destination: patch.destination ?? existing.destination,
          departureDateTime:
            patch.departureDateTime ?? existing.departureDateTime,
          durationMinutes: patch.durationMinutes ?? existing.durationMinutes,
          price: patch.pricePerSeat ?? existing.pricePerSeat,
          seatsTotal: patch.seatsTotal ?? existing.seatsTotal,
          bookingMode: patch.bookingMode ?? existing.bookingMode,
        };

        return this.http
          .put<TripApi>(`${this.baseUrl}/api/driver/trips/${tripId}`, body, {
            headers: this.userHeaders(),
          })
          .pipe(
            map((trip) => ({ ok: true, trip: this.mapTrip(trip) })),
            catchError((error) =>
              of({
                ok: false,
                error: this.extractError(error, 'Unable to update trip.'),
              }),
            ),
          );
      }),
    );
  }

  cancelTrip(tripId: number): Observable<{ ok: boolean; error?: string }> {
    return this.http
      .put<TripApi>(
        `${this.baseUrl}/api/driver/trips/${tripId}/cancel`,
        {},
        { headers: this.userHeaders() },
      )
      .pipe(
        map(() => ({ ok: true })),
        catchError((error) =>
          of({
            ok: false,
            error: this.extractError(error, 'Unable to cancel trip.'),
          }),
        ),
      );
  }

  makeTripAvailable(
    tripId: number,
  ): Observable<{ ok: boolean; error?: string }> {
    return this.http
      .put<TripApi>(
        `${this.baseUrl}/api/driver/trips/${tripId}/available`,
        {},
        { headers: this.userHeaders() },
      )
      .pipe(
        map(() => ({ ok: true })),
        catchError((error) =>
          of({
            ok: false,
            error: this.extractError(error, 'Unable to make trip available.'),
          }),
        ),
      );
  }

  bookTrip(
    tripId: number,
    seatsRequested: number,
  ): Observable<{ ok: boolean; error?: string; booking?: Booking }> {
    return this.getTripById(tripId).pipe(
      switchMap((trip) => {
        if (!trip) {
          return of({ ok: false, error: 'Trip not found.' });
        }
        if (trip.ownerUserId === this.getCurrentUserId()) {
          return of({ ok: false, error: 'You cannot book your own trip.' });
        }

        const body = {
          status: trip.bookingMode === 'instant' ? 'CONFIRMED' : 'PENDING',
          numberOfPeople: seatsRequested,
          type: 'TripReservation',
          trip: { id: trip.id },
        };

        return this.http
          .post<ReservationApi>(`${this.baseUrl}/api/reservations`, body, {
            headers: this.userHeaders(),
          })
          .pipe(
            map((reservation) => ({
              ok: true,
              booking: this.mapReservationToBooking(reservation),
            })),
            catchError((error) =>
              of({
                ok: false,
                error: this.extractError(error, 'Booking failed.'),
              }),
            ),
          );
      }),
    );
  }

  getBookingQuote(
    tripId: number,
    seatsRequested: number,
  ): Observable<ReservationQuote | undefined> {
    const params = new HttpParams()
      .set('tripId', tripId)
      .set('seats', seatsRequested);

    return this.http
      .get<ReservationQuoteApi>(`${this.baseUrl}/api/reservations/quote`, {
        params,
      })
      .pipe(
        map((quote) => ({
          seatsRequested: Number(quote.seatsRequested ?? seatsRequested),
          driverAmount: Number(quote.driverAmount ?? 0),
          serviceFee: Number(quote.serviceFee ?? 0),
          totalAmount: Number(quote.totalAmount ?? 0),
        })),
        catchError(() => of(undefined)),
      );
  }

  getMyBookingsWithContext(): Observable<BookingWithContext[]> {
    return this.http
      .get<ReservationApi[]>(
        `${this.baseUrl}/api/reservations/user/${this.getCurrentUserId()}`,
      )
      .pipe(
        switchMap((reservations) => {
          const bookingReservations = (reservations ?? []).filter(
            (r) => r.type === 'TripReservation',
          );
          const tripIds = this.extractTripIds(bookingReservations);

          if (tripIds.length === 0) {
            return of([]);
          }

          const tripCalls = tripIds.map((id) =>
            this.getTripById(id).pipe(catchError(() => of(undefined))),
          );

          return forkJoin(tripCalls).pipe(
            map((trips: Array<Trip | undefined>) => {
              const tripMap = new Map<number, Trip>();
              trips.forEach((trip: Trip | undefined) => {
                if (trip) {
                  tripMap.set(trip.id, trip);
                }
              });

              return bookingReservations.map((reservation) => {
                const booking = this.mapReservationToBooking(reservation);
                const trip = tripMap.get(booking.tripId);
                const defaultDriver = trip
                  ? this.getUserById(trip.ownerUserId)
                  : undefined;
                return {
                  booking,
                  trip,
                  passenger:
                    this.mapReservationPassenger(reservation) ||
                    this.getCurrentUser(),
                  driver:
                    trip && defaultDriver
                      ? {
                          ...defaultDriver,
                          fullName:
                            trip.ownerFullName || defaultDriver.fullName,
                        }
                      : undefined,
                };
              });
            }),
          );
        }),
        catchError(() => of([])),
      );
  }

  cancelBooking(
    bookingId: number,
  ): Observable<{ ok: boolean; error?: string }> {
    return this.http
      .get<ReservationApi>(`${this.baseUrl}/api/reservations/${bookingId}`)
      .pipe(
        switchMap((reservation) => {
          const body = { ...reservation, status: 'CANCELED' };
          return this.http
            .put<ReservationApi>(`${this.baseUrl}/api/reservations`, body, {
              headers: this.userHeaders(),
            })
            .pipe(
              map(() => ({ ok: true })),
              catchError((error) =>
                of({
                  ok: false,
                  error: this.extractError(error, 'Unable to cancel booking.'),
                }),
              ),
            );
        }),
        catchError((error) =>
          of({
            ok: false,
            error: this.extractError(error, 'Booking not found.'),
          }),
        ),
      );
  }

  getPassengersForTrip(tripId: number): Observable<BookingWithContext[]> {
    return this.http
      .get<ReservationApi[]>(`${this.baseUrl}/api/reservations/trip/${tripId}`, {
        headers: this.userHeaders(),
      })
      .pipe(
        switchMap((reservations) => {
          const bookingReservations = (reservations ?? []).filter(
            (reservation) => reservation.type === 'TripReservation',
          );

          if (bookingReservations.length === 0) {
            return of([]);
          }

          return this.getTripById(tripId).pipe(
            map((trip) => {
              const defaultDriver = trip
                ? this.getUserById(trip.ownerUserId)
                : undefined;

              return bookingReservations.map((reservation) => {
                const booking = this.mapReservationToBooking(reservation);

                return {
                  booking,
                  trip,
                  passenger: this.mapReservationPassenger(reservation),
                  driver:
                    trip && defaultDriver
                      ? {
                          ...defaultDriver,
                          fullName:
                            trip.ownerFullName || defaultDriver.fullName,
                        }
                      : undefined,
                };
              });
            }),
            catchError(() => of([])),
          );
        }),
        catchError(() => of([])),
      );
  }

  approveBooking(
    bookingId: number,
  ): Observable<{ ok: boolean; error?: string; booking?: Booking }> {
    return this.http
      .put<ReservationApi>(
        `${this.baseUrl}/api/reservations/${bookingId}/approve`,
        {},
        { headers: this.userHeaders() },
      )
      .pipe(
        map((reservation) => ({
          ok: true,
          booking: this.mapReservationToBooking(reservation),
        })),
        catchError((error) =>
          of({
            ok: false,
            error: this.extractError(error, 'Unable to approve booking.'),
          }),
        ),
      );
  }

  rejectBooking(
    bookingId: number,
  ): Observable<{ ok: boolean; error?: string; booking?: Booking }> {
    return this.http
      .put<ReservationApi>(
        `${this.baseUrl}/api/reservations/${bookingId}/reject`,
        {},
        { headers: this.userHeaders() },
      )
      .pipe(
        map((reservation) => ({
          ok: true,
          booking: this.mapReservationToBooking(reservation),
        })),
        catchError((error) =>
          of({
            ok: false,
            error: this.extractError(error, 'Unable to reject booking.'),
          }),
        ),
      );
  }

  submitComplaint(
    payload: Pick<Complaint, 'tripId' | 'description' | 'bookingId'>,
  ): Observable<Complaint> {
    const body = {
      description: payload.description,
      date: new Date().toISOString(),
      reportedByUserId: String(this.getCurrentUserId()),
      reservation: payload.bookingId ? { id: payload.bookingId } : undefined,
    };

    return this.http
      .post<ComplaintApi>(`${this.baseUrl}/api/complaints`, body)
      .pipe(map((complaint) => this.mapComplaint(complaint, payload.tripId)));
  }

  getReservationReview(
    reservationId: number,
  ): Observable<DriverReview | undefined> {
    return this.http
      .get<DriverReviewApi>(
        `${this.baseUrl}/api/carpooling-reviews/reservation/${reservationId}`,
      )
      .pipe(
        map((review) => this.mapDriverReview(review)),
        catchError(() => of(undefined)),
      );
  }

  addReservationReview(
    reservationId: number,
    payload: { rating: number; comment: string },
  ): Observable<DriverReview> {
    return this.http
      .post<DriverReviewApi>(
        `${this.baseUrl}/api/carpooling-reviews/add/${reservationId}`,
        payload,
        { headers: this.userHeaders() },
      )
      .pipe(map((review) => this.mapDriverReview(review)));
  }

  updateReservationReview(
    reviewId: number,
    payload: { rating: number; comment: string },
  ): Observable<DriverReview> {
    return this.http
      .put<DriverReviewApi>(
        `${this.baseUrl}/api/carpooling-reviews/update/${reviewId}`,
        payload,
        { headers: this.userHeaders() },
      )
      .pipe(map((review) => this.mapDriverReview(review)));
  }

  getDriverReviewSummary(driverId: number): Observable<DriverReviewSummary> {
    return this.http
      .get<DriverReviewSummaryApi>(
        `${this.baseUrl}/api/carpooling-reviews/driver-summary/${driverId}`,
      )
      .pipe(
        map((summary) => ({
          averageRating: Number(summary.averageRating ?? 0),
          reviewsCount: Number(summary.reviewsCount ?? 0),
        })),
        catchError(() => of({ averageRating: 0, reviewsCount: 0 })),
      );
  }

  getAdminStats(): Observable<AdminStats> {
    return forkJoin({
      trips: this.searchTrips({}).pipe(catchError(() => of([]))),
      bookings: this.getMyBookingsWithContext().pipe(catchError(() => of([]))),
      complaints: this.getComplaintsWithContext().pipe(
        catchError(() => of([])),
      ),
    }).pipe(
      map(
        ({
          trips,
          bookings,
          complaints,
        }: {
          trips: Trip[];
          bookings: BookingWithContext[];
          complaints: ComplaintWithContext[];
        }) => ({
          totalTrips: trips.length,
          totalBookings: bookings.length,
          totalComplaints: complaints.length,
          activeUsers: new Set(trips.map((trip: Trip) => trip.ownerUserId))
            .size,
        }),
      ),
    );
  }

  getAllBookingsWithContext(): Observable<BookingWithContext[]> {
    return this.getMyBookingsWithContext();
  }

  getComplaintsWithContext(): Observable<ComplaintWithContext[]> {
    return this.http.get<ComplaintApi[]>(`${this.baseUrl}/api/complaints`).pipe(
      map((complaints) =>
        (complaints ?? []).map((complaint) => ({
          complaint: this.mapComplaint(complaint, 0),
          reporter: this.getUserById(
            Number(complaint.reportedByUserId || this.getCurrentUserId()),
          ),
        })),
      ),
      catchError(() => of([])),
    );
  }

  updateComplaintStatus(
    complaintId: number,
    status: ComplaintStatus,
  ): Observable<{ ok: boolean; error?: string }> {
    return this.http
      .patch(`${this.baseUrl}/api/complaints/${complaintId}/status`, { status })
      .pipe(
        map(() => ({ ok: true })),
        catchError((error) =>
          of({
            ok: false,
            error: this.extractError(
              error,
              'Unable to update complaint status.',
            ),
          }),
        ),
      );
  }

  getUsersSummary(): Observable<
    Array<{
      user: CarpoolUser;
      tripsCreated: number;
      bookingsMade: number;
      complaintsSubmitted: number;
      complaintsReceived: number;
    }>
  > {
    return forkJoin({
      trips: this.searchTrips({}).pipe(catchError(() => of([]))),
      bookings: this.getMyBookingsWithContext().pipe(catchError(() => of([]))),
      complaints: this.getComplaintsWithContext().pipe(
        catchError(() => of([])),
      ),
    }).pipe(
      map(
        ({
          trips,
          bookings,
          complaints,
        }: {
          trips: Trip[];
          bookings: BookingWithContext[];
          complaints: ComplaintWithContext[];
        }) => {
          const current = this.getCurrentUser();
          return [
            {
              user: current,
              tripsCreated: trips.filter(
                (trip: Trip) => trip.ownerUserId === current.id,
              ).length,
              bookingsMade: bookings.length,
              complaintsSubmitted: complaints.length,
              complaintsReceived: 0,
            },
          ];
        },
      ),
    );
  }

  private userHeaders(): HttpHeaders {
    let headers = new HttpHeaders();
    const userId = this.getStoredUserId();
    const role = this.getStoredRole();

    if (userId) {
      headers = headers.set('X-USER-ID', userId);
    }
    if (role) {
      headers = headers.set('X-ROLE', role);
    }

    return headers;
  }

  private mapTrip(apiTrip: TripApi): Trip {
    const ownerUserId = Number(apiTrip.driver?.id);
    return {
      id: apiTrip.id,
      departure: apiTrip.departure,
      destination: apiTrip.destination,
      departureDateTime: apiTrip.departureDateTime,
      durationMinutes: apiTrip.durationMinutes,
      pricePerSeat: Number(apiTrip.price),
      seatsTotal: apiTrip.seatsTotal,
      seatsAvailable: undefined,
      ownerUserId: Number.isFinite(ownerUserId) ? ownerUserId : 0,
      ownerFullName: this.buildFullName(
        apiTrip.driver?.nom,
        apiTrip.driver?.prenom,
      ),
      bookingMode: apiTrip.bookingMode,
      status:
        apiTrip.status?.toUpperCase() === 'CANCELED' ? 'CANCELED' : 'ACTIVE',
    };
  }

  private setSeatsAvailableTrip(trip: Trip): Observable<Trip> {
    return this.http
      .get<number>(
        `${this.baseUrl}/api/carpooling/trips/retrieve-seats-available/${trip.id}`,
      )
      .pipe(
        map((seatsAvailable) =>
          this.setSeatsAvailable(trip, Number(seatsAvailable)),
        ),
        catchError(() => of(this.setSeatsAvailable(trip, trip.seatsTotal))),
      );
  }

  private setSeatsAvailableTrips(trips: Trip[]): Observable<Trip[]> {
    if (!trips || trips.length === 0) {
      return of([]);
    }

    const requests = trips.map((trip) =>
      this.http
        .get<number>(
          `${this.baseUrl}/api/carpooling/trips/retrieve-seats-available/${trip.id}`,
        )
        .pipe(
          map((seatsAvailable) =>
            this.setSeatsAvailable(trip, Number(seatsAvailable)),
          ),
          catchError(() => of(this.setSeatsAvailable(trip, trip.seatsTotal))),
        ),
    );

    return forkJoin(requests).pipe(
      catchError(() =>
        of(
          trips.map((trip) =>
            this.setSeatsAvailable(trip, trip.seatsTotal),
          ),
        ),
      ),
    );
  }

  private setSeatsAvailable(trip: Trip, seatsAvailable: number): Trip {
    const safeSeatsAvailable =
      Number.isFinite(seatsAvailable) && seatsAvailable >= 0
        ? seatsAvailable
        : trip.seatsTotal;

    return {
      ...trip,
      seatsAvailable: safeSeatsAvailable,
    };
  }

  private setDriverReviewSummaryTrip(trip: Trip): Observable<Trip> {
    return this.getDriverReviewSummary(trip.ownerUserId).pipe(
      map((summary) =>
        this.setDriverReviewSummary(
          trip,
          summary.averageRating,
          summary.reviewsCount,
        ),
      ),
      catchError(() =>
        of(this.setDriverReviewSummary(trip, 0, 0)),
      ),
    );
  }

  private setDriverReviewSummaryTrips(trips: Trip[]): Observable<Trip[]> {
    if (!trips || trips.length === 0) {
      return of([]);
    }

    const requests = trips.map((trip) =>
      this.getDriverReviewSummary(trip.ownerUserId).pipe(
        map((summary) =>
          this.setDriverReviewSummary(
            trip,
            summary.averageRating,
            summary.reviewsCount,
          ),
        ),
        catchError(() => of(this.setDriverReviewSummary(trip, 0, 0))),
      ),
    );

    return forkJoin(requests).pipe(
      catchError(() =>
        of(trips.map((trip) => this.setDriverReviewSummary(trip, 0, 0))),
      ),
    );
  }

  private setDriverReviewSummary(
    trip: Trip,
    averageRating: number,
    reviewsCount: number,
  ): Trip {
    const safeAverageRating =
      Number.isFinite(averageRating) && averageRating > 0 ? averageRating : 0;
    const safeReviewsCount =
      Number.isFinite(reviewsCount) && reviewsCount > 0 ? reviewsCount : 0;

    return {
      ...trip,
      driverRatingAverage: safeAverageRating,
      driverReviewsCount: safeReviewsCount,
    };
  }

  private buildFullName(nom?: string, prenom?: string): string | undefined {
    const fullName = [nom, prenom]
      .filter((value) => !!value && value.trim().length > 0)
      .join(' ')
      .trim();

    return fullName || undefined;
  }

  private mapReservationToBooking(reservation: ReservationApi): Booking {
    const passengerUserId = Number(reservation.reservedBy?.id);
    const reservationStatus = (reservation.status || '').toUpperCase();

    return {
      id: reservation.id,
      tripId: this.extractTripId(reservation),
      passengerUserId: Number.isFinite(passengerUserId)
        ? passengerUserId
        : this.getCurrentUserId(),
      seatsBooked: Number(reservation.numberOfPeople ?? 1),
      totalPrice: Number(reservation.totalPrice ?? 0),
      bookingDate:
        reservation.startDate ||
        reservation.endDate ||
        new Date().toISOString(),
      status:
        reservationStatus === 'CANCELED'
          ? 'CANCELED'
          : reservationStatus === 'PENDING'
            ? 'PENDING'
            : 'CONFIRMED',
    };
  }

  private mapDriverReview(review: DriverReviewApi): DriverReview {
    return {
      id: review.id,
      reservationId: review.reservationId,
      rating: Number(review.rating ?? 0),
      comment: review.comment,
      date: review.date,
      reviewerName: review.reviewerName,
    };
  }

  private mapReservationPassenger(
    reservation: ReservationApi,
  ): CarpoolUser | undefined {
    const userId = Number(reservation.reservedBy?.id);
    if (!Number.isFinite(userId)) {
      return undefined;
    }

    return {
      id: userId,
      fullName:
        this.buildFullName(
          reservation.reservedBy?.nom,
          reservation.reservedBy?.prenom,
        ) || `User ${userId}`,
      email: reservation.reservedBy?.email || `user${userId}@example.com`,
    };
  }

  private mapComplaint(complaint: ComplaintApi, tripId: number): Complaint {
    return {
      id: complaint.id,
      description: complaint.description,
      createdAt: complaint.date,
      reporterUserId: Number(
        complaint.reportedByUserId || this.getCurrentUserId(),
      ),
      tripId,
      status: (complaint.status as ComplaintStatus) || 'OPEN',
      bookingId: undefined,
    };
  }

  private getCurrentUserId(): number {
    const value = this.getStoredUserId();
    if (!value) {
      return 0;
    }

    const userId = Number(value);
    return Number.isFinite(userId) ? userId : 0;
  }

  private getStoredUserId(): string | null {
    if (typeof localStorage === 'undefined') {
      return null;
    }

    const value = localStorage.getItem('userId');
    if (value == null || value.trim() === '') {
      return null;
    }

    return value;
  }

  private getStoredRole(): string | null {
    if (typeof localStorage === 'undefined') {
      return null;
    }

    const value = localStorage.getItem('role');
    if (value == null || value.trim() === '') {
      return null;
    }

    return value;
  }

  private extractTripIds(reservations: ReservationApi[]): number[] {
    return [
      ...new Set(
        reservations
          .map((reservation) => this.extractTripId(reservation))
          .filter((id) => id > 0),
      ),
    ];
  }

  private extractTripId(reservation: ReservationApi): number {
    if (typeof reservation.trip === 'number') {
      return reservation.trip;
    }
    if (reservation.trip && typeof reservation.trip === 'object') {
      return Number(reservation.trip.id ?? 0);
    }
    return 0;
  }

  private extractError(error: unknown, fallback: string): string {
    if (
      typeof error === 'object' &&
      error !== null &&
      'error' in error &&
      typeof (error as { error?: { message?: string } }).error?.message ===
        'string'
    ) {
      return (error as { error: { message: string } }).error.message;
    }
    return fallback;
  }
}
