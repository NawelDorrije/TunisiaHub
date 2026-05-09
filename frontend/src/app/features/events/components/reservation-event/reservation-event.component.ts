import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { EventService } from '../../services/event.service';
import { AuthService } from '../../../../services/auth.service';

@Component({
  selector: 'app-reservation-event',
  templateUrl: './reservation-event.component.html',
  styleUrls: ['./reservation-event.component.css']
})
export class ReservationEventComponent implements OnInit {

  event: any;
  eventId!: number;
  reservationId!: number;

  isLoading = false;
  errorMessage = '';
  showStripe = false;

  reservationForm = new FormGroup({
    firstName: new FormControl('', [Validators.required, Validators.minLength(3)]),
    lastName: new FormControl('', [Validators.required, Validators.minLength(3)]),
    email: new FormControl('', [Validators.required, Validators.email]),
    paymentMethod: new FormControl('CARD', [Validators.required])
  });

  constructor(
    private route: ActivatedRoute,
    private eventService: EventService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.eventId = Number(this.route.snapshot.paramMap.get('id'));

    this.eventService.getEventById(this.eventId).subscribe({
      next: (data) => this.event = data,
      error: () => this.errorMessage = "Failed to load event"
    });
  }

  get f() {
    return this.reservationForm.controls;
  }

  pay(): void {

  if (this.reservationForm.invalid) {
    this.reservationForm.markAllAsTouched();
    return;
  }

  const userId = this.authService.getUserId();

  if (!userId) {
    this.errorMessage = "User not logged in";
    return;
  }

  this.isLoading = true;

  // ✅ 1. CREATE PENDING RESERVATION
  this.eventService.createPendingReservation(userId, this.eventId)
    .subscribe({
      next: (res: any) => {

        // ✅ IMPORTANT
        this.reservationId = res.id;
        if (!this.reservationId) {
    this.errorMessage = "Reservation ID NULL ❌";
    return;
  }


        console.log("Reservation ID:", this.reservationId);

        // ✅ 2. OPEN STRIPE
        this.showStripe = true;
        this.isLoading = false;
      },

      error: (err: any) => {
  console.log("FULL ERROR:", err);

  this.errorMessage =
    err.error?.message ||
    err.error?.error ||
    err.message ||
    "User already reserved this event";

  this.isLoading = false;
}
    });
}

  closeStripe() {
    this.showStripe = false;
  }
}