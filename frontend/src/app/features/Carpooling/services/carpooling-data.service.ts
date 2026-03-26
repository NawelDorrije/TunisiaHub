import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, forkJoin, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import {
  AdminStats,
  Booking,
  BookingWithContext,
  CarpoolUser,
  Complaint,
  ComplaintStatus,
  ComplaintWithContext,
  Trip,
  TripSearchFilters,
} from '../../../models/Carpooling/carpooling';

interface TripApi {
  id: number;
  departurePoint: string;
  destination: string;
  departureDateTime: string;
  price: number;
  seatsTotal: number;
  seatsAvailable: number;
  status: string;
  createdBy: string;
  vehicle?: VehicleApi;
}

interface ReservationApi {
  id: number;
  status: string;
  startDate: string;
  endDate: string;
  totalPrice: number;
  type: string;
  trip?: number | { id: number };
}

interface ComplaintApi {
  id: number;
  description: string;
  date: string;
  reportedByUserId: string;
  reservation?: number | { id: number };
  status?: string;
}

interface VehicleApi {
  id: number;
  model: string;
  plateNumber: string;
  color: string;
}

export interface Vehicle {
  id: number;
  model: string;
  plateNumber: string;
  color: string;
}

@Injectable({
  providedIn: 'root',
})
export class CarpoolingDataService {
  private readonly baseUrl = 'http://localhost:8089';
  private readonly currentUserId = 2;
  private readonly currentRole = 'USER';

  constructor(private readonly http: HttpClient) {}

  getCurrentUser(): CarpoolUser {
    return {
      id: this.currentUserId,
      fullName: `User ${this.currentUserId}`,
      email: `user${this.currentUserId}@example.com`,
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
    if (filters.date) {
      params = params.set('date', filters.date);
    }
    if (filters.seatsNeeded && filters.seatsNeeded > 0) {
      params = params.set('seatsRequired', filters.seatsNeeded);
    }

    return this.http
      .get<TripApi[]>(`${this.baseUrl}/api/carpooling/trips`, { params })
      .pipe(
        map((response) => (response ?? []).map((trip) => this.mapTrip(trip))),
      );
  }

  publishTrip(
    payload: Omit<Trip, 'id' | 'ownerUserId' | 'seatsAvailable' | 'status'> & {
      vehicleId?: number;
    },
  ): Observable<Trip> {
    const body = {
      departurePoint: payload.departure,
      destination: payload.destination,
      departureDateTime: payload.departureDateTime,
      price: payload.pricePerSeat,
      seatsTotal: payload.seatsTotal,
      vehicle: payload.vehicleId ? { id: payload.vehicleId } : undefined,
    };

    return this.http
      .post<TripApi>(`${this.baseUrl}/api/driver/trips`, body, {
        headers: this.userHeaders(),
      })
      .pipe(map((trip) => this.mapTrip(trip)));
  }

  canEditTrip(trip: Trip): { allowed: boolean; reason?: string } {
    if (trip.ownerUserId !== this.currentUserId) {
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
        | 'pricePerSeat'
        | 'seatsTotal'
        | 'vehicleInfo'
      >
    > & { vehicleId?: number },
  ): Observable<{ ok: boolean; error?: string; trip?: Trip }> {
    return this.getTripById(tripId).pipe(
      switchMap((existing) => {
        if (!existing) {
          return of({ ok: false, error: 'Trip not found.' });
        }

        const body = {
          departurePoint: patch.departure ?? existing.departure,
          destination: patch.destination ?? existing.destination,
          departureDateTime:
            patch.departureDateTime ?? existing.departureDateTime,
          price: patch.pricePerSeat ?? existing.pricePerSeat,
          seatsTotal: patch.seatsTotal ?? existing.seatsTotal,
          vehicle: patch.vehicleId ? { id: patch.vehicleId } : undefined,
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

  bookTrip(
    tripId: number,
    seatsRequested: number,
  ): Observable<{ ok: boolean; error?: string; booking?: Booking }> {
    return this.getTripById(tripId).pipe(
      switchMap((trip) => {
        if (!trip) {
          return of({ ok: false, error: 'Trip not found.' });
        }
        if (trip.ownerUserId === this.currentUserId) {
          return of({ ok: false, error: 'You cannot book your own trip.' });
        }
        if (trip.seatsAvailable < seatsRequested) {
          return of({ ok: false, error: 'Not enough seats available.' });
        }

        const body = {
          status: 'CONFIRMED',
          startDate: trip.departureDateTime,
          endDate: trip.departureDateTime,
          totalPrice: seatsRequested * trip.pricePerSeat,
          type: 'TripReservation',
          trip: { id: trip.id },
        };

        return this.http
          .post<ReservationApi>(`${this.baseUrl}/api/reservations`, body)
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

  getMyBookingsWithContext(): Observable<BookingWithContext[]> {
    return this.http
      .get<ReservationApi[]>(`${this.baseUrl}/api/reservations`)
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
            this.http
              .get<TripApi>(`${this.baseUrl}/api/carpooling/trips/${id}`)
              .pipe(catchError(() => of(undefined))),
          );

          return forkJoin(tripCalls).pipe(
            map((trips) => {
              const tripMap = new Map<number, Trip>();
              trips.forEach((tripApi) => {
                if (tripApi) {
                  tripMap.set(tripApi.id, this.mapTrip(tripApi));
                }
              });

              return bookingReservations.map((reservation) => {
                const booking = this.mapReservationToBooking(reservation);
                const trip = tripMap.get(booking.tripId);
                return {
                  booking,
                  trip,
                  passenger: this.getCurrentUser(),
                  driver: trip ? this.getUserById(trip.ownerUserId) : undefined,
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
            .put<ReservationApi>(`${this.baseUrl}/api/reservations`, body)
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
    return this.getMyBookingsWithContext().pipe(
      map((bookings) =>
        bookings.filter((item) => item.booking.tripId === tripId),
      ),
    );
  }

  submitComplaint(
    payload: Pick<Complaint, 'tripId' | 'description' | 'bookingId'>,
  ): Observable<Complaint> {
    const body = {
      description: payload.description,
      date: new Date().toISOString(),
      reportedByUserId: String(this.currentUserId),
      reservation: payload.bookingId ? { id: payload.bookingId } : undefined,
    };

    return this.http
      .post<ComplaintApi>(`${this.baseUrl}/api/complaints`, body)
      .pipe(map((complaint) => this.mapComplaint(complaint, payload.tripId)));
  }

  getAdminStats(): Observable<AdminStats> {
    return forkJoin({
      trips: this.searchTrips({}).pipe(catchError(() => of([]))),
      bookings: this.getMyBookingsWithContext().pipe(catchError(() => of([]))),
      complaints: this.getComplaintsWithContext().pipe(
        catchError(() => of([])),
      ),
    }).pipe(
      map(({ trips, bookings, complaints }) => ({
        totalTrips: trips.length,
        totalBookings: bookings.length,
        totalComplaints: complaints.length,
        activeUsers: new Set(trips.map((trip) => trip.ownerUserId)).size,
      })),
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
            Number(complaint.reportedByUserId || this.currentUserId),
          ),
        })),
      ),
      catchError(() => of([])),
    );
  }

  // Vehicle Management API Methods
  getMyVehicles(): Observable<Vehicle[]> {
    return this.http
      .get<VehicleApi[]>(`${this.baseUrl}/api/driver/vehicles`, {
        headers: this.userHeaders(),
      })
      .pipe(
        map((response) => (response ?? []).map((v) => this.mapVehicle(v))),
        catchError(() => of([])),
      );
  }

  createVehicle(vehicle: Omit<Vehicle, 'id'>): Observable<Vehicle> {
    return this.http
      .post<VehicleApi>(`${this.baseUrl}/api/driver/vehicles`, vehicle, {
        headers: this.userHeaders(),
      })
      .pipe(map((v) => this.mapVehicle(v)));
  }

  updateVehicle(id: number, vehicle: Omit<Vehicle, 'id'>): Observable<Vehicle> {
    return this.http
      .put<VehicleApi>(`${this.baseUrl}/api/driver/vehicles/${id}`, vehicle, {
        headers: this.userHeaders(),
      })
      .pipe(map((v) => this.mapVehicle(v)));
  }

  deleteVehicle(id: number): Observable<{ ok: boolean; error?: string }> {
    return this.http
      .delete(`${this.baseUrl}/api/driver/vehicles/${id}`, {
        headers: this.userHeaders(),
      })
      .pipe(
        map(() => ({ ok: true })),
        catchError((error) =>
          of({
            ok: false,
            error: this.extractError(error, 'Unable to delete vehicle.'),
          }),
        ),
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
      map(({ trips, bookings, complaints }) => {
        const current = this.getCurrentUser();
        return [
          {
            user: current,
            tripsCreated: trips.filter(
              (trip) => trip.ownerUserId === current.id,
            ).length,
            bookingsMade: bookings.length,
            complaintsSubmitted: complaints.length,
            complaintsReceived: 0,
          },
        ];
      }),
    );
  }

  private userHeaders(): HttpHeaders {
    return new HttpHeaders({
      'X-USER-ID': String(this.currentUserId),
      'X-ROLE': this.currentRole,
    });
  }

  private mapTrip(apiTrip: TripApi): Trip {
    const ownerUserId = Number(apiTrip.createdBy);
    return {
      id: apiTrip.id,
      departure: apiTrip.departurePoint,
      destination: apiTrip.destination,
      departureDateTime: apiTrip.departureDateTime,
      pricePerSeat: Number(apiTrip.price),
      seatsTotal: apiTrip.seatsTotal,
      seatsAvailable: apiTrip.seatsAvailable,
      ownerUserId: Number.isFinite(ownerUserId) ? ownerUserId : 0,
      vehicleInfo: apiTrip.vehicle?.id
        ? `Vehicle #${apiTrip.vehicle.id}`
        : undefined,
      status: apiTrip.status?.toUpperCase() === 'CANCELED' ? 'CANCELED' : 'ACTIVE',
    };
  }

  private mapReservationToBooking(reservation: ReservationApi): Booking {
    return {
      id: reservation.id,
      tripId: this.extractTripId(reservation),
      passengerUserId: this.currentUserId,
      seatsBooked: 1,
      totalPrice: Number(reservation.totalPrice ?? 0),
      bookingDate: reservation.startDate,
      status: reservation.status === 'CANCELED' ? 'CANCELED' : 'CONFIRMED',
    };
  }

  private mapComplaint(complaint: ComplaintApi, tripId: number): Complaint {
    return {
      id: complaint.id,
      description: complaint.description,
      createdAt: complaint.date,
      reporterUserId: Number(complaint.reportedByUserId || this.currentUserId),
      tripId,
      status: (complaint.status as ComplaintStatus) || 'OPEN',
      bookingId: undefined,
    };
  }

  private mapVehicle(apiVehicle: VehicleApi): Vehicle {
    return {
      id: apiVehicle.id,
      model: apiVehicle.model,
      plateNumber: apiVehicle.plateNumber,
      color: apiVehicle.color,
    };
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
