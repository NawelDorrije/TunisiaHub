import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import {
  CarpoolingDataService,
  Vehicle,
} from '../services/carpooling-data.service';

@Component({
  selector: 'app-publish-trip',
  templateUrl: './publish-trip.component.html',
  styleUrls: ['./publish-trip.component.css'],
})
export class PublishTripComponent implements OnInit {
  publishForm!: FormGroup;

  vehicleForm!: FormGroup;

  error = '';
  success = '';
  vehicles: Vehicle[] = [];
  selectedVehicleId: number | null = null;
  showAddVehicleForm = false;
  loadingVehicles = false;
  creatingVehicle = false;
  publishingTrip = false;

  constructor(
    private fb: FormBuilder,
    private readonly dataService: CarpoolingDataService,
    private readonly router: Router,
  ) {
    this.publishForm = this.fb.group({
      departure: ['', [Validators.required]],
      destination: ['', [Validators.required]],
      departureDateTime: ['', [Validators.required]],
      pricePerSeat: [0, [Validators.required, Validators.min(0)]],
      seatsTotal: [1, [Validators.required, Validators.min(1)]],
      meetingPoint: [''],
    });

    this.vehicleForm = this.fb.group({
      model: ['', [Validators.required]],
      plateNumber: ['', [Validators.required]],
      color: ['', [Validators.required]],
    });
  }

  ngOnInit(): void {
    this.loadVehicles();
  }

  loadVehicles(): void {
    this.loadingVehicles = true;
    this.dataService.getMyVehicles().subscribe({
      next: (vehicles) => {
        this.vehicles = vehicles;
        if (vehicles.length === 1) {
          this.selectedVehicleId = vehicles[0].id;
        }
        this.loadingVehicles = false;
      },
      error: () => {
        this.loadingVehicles = false;
      },
    });
  }

  toggleAddVehicleForm(): void {
    this.showAddVehicleForm = !this.showAddVehicleForm;
    if (!this.showAddVehicleForm) {
      this.vehicleForm.reset();
    }
  }

  addVehicle(): void {
    this.error = '';
    if (this.vehicleForm.invalid) {
      this.vehicleForm.markAllAsTouched();
      return;
    }

    this.creatingVehicle = true;
    const formValue = this.vehicleForm.getRawValue();
    this.dataService
      .createVehicle({
        model: formValue.model,
        plateNumber: formValue.plateNumber.toUpperCase(),
        color: formValue.color,
      })
      .subscribe({
        next: (vehicle) => {
          this.vehicles.push(vehicle);
          this.selectedVehicleId = vehicle.id;
          this.vehicleForm.reset();
          this.showAddVehicleForm = false;
          this.creatingVehicle = false;
        },
        error: (err) => {
          this.error = this.extractErrorMessage(err, 'Failed to add vehicle');
          this.creatingVehicle = false;
        },
      });
  }

  publish(): void {
    this.error = '';
    this.success = '';
    if (this.publishForm.invalid) {
      this.publishForm.markAllAsTouched();
      return;
    }

    const form = this.publishForm.getRawValue();
    const departureDate = new Date(form.departureDateTime);
    if (
      Number.isNaN(departureDate.getTime()) ||
      departureDate.getTime() <= Date.now()
    ) {
      this.error = 'Departure date must be in the future.';
      return;
    }

    if (this.vehicles.length > 0 && !this.selectedVehicleId) {
      this.error = 'Please select a vehicle or add a new one.';
      return;
    }

    this.publishingTrip = true;
    this.dataService
      .publishTrip({
        departure: form.departure,
        destination: form.destination,
        departureDateTime: departureDate.toISOString(),
        pricePerSeat: form.pricePerSeat,
        seatsTotal: form.seatsTotal,
        vehicleId: this.selectedVehicleId || undefined,
      })
      .subscribe({
        next: () => {
          this.success = 'Trip published successfully! Redirecting...';
          this.publishingTrip = false;
          setTimeout(() => {
            this.router.navigate(['/carpooling/my-trips']);
          }, 1500);
        },
        error: (err: HttpErrorResponse) => {
          this.error = this.extractErrorMessage(
            err,
            'Unable to publish trip. Please ensure backend is running.',
          );
          this.publishingTrip = false;
        },
      });
  }

  private extractErrorMessage(error: unknown, fallback: string): string {
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
