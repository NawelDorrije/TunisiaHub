import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import {
  CarpoolingDataService,
  Vehicle,
} from '../services/carpooling-data.service';

@Component({
  selector: 'app-vehicle-management',
  templateUrl: './vehicle-management.component.html',
  styleUrls: ['./vehicle-management.component.css'],
})
export class VehicleManagementComponent implements OnInit {
  vehicles: Vehicle[] = [];
  loading = false;
  error = '';
  successMessage = '';

  editingVehicleId: number | null = null;
  showAddForm = false;

  addVehicleForm: FormGroup;
  editVehicleForm: FormGroup;

  constructor(
    private readonly dataService: CarpoolingDataService,
    private readonly fb: FormBuilder,
  ) {
    this.addVehicleForm = this.fb.nonNullable.group({
      model: ['', [Validators.required]],
      plateNumber: ['', [Validators.required]],
      color: ['', [Validators.required]],
    });

    this.editVehicleForm = this.fb.nonNullable.group({
      model: ['', [Validators.required]],
      plateNumber: ['', [Validators.required]],
      color: ['', [Validators.required]],
    });
  }

  ngOnInit(): void {
    this.loadVehicles();
  }

  loadVehicles(): void {
    this.loading = true;
    this.error = '';
    this.successMessage = '';

    this.dataService.getMyVehicles().subscribe({
      next: (vehicles: Vehicle[]) => {
        this.vehicles = vehicles;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load vehicles';
        this.loading = false;
      },
    });
  }

  toggleAddForm(): void {
    console.debug('[VehicleManagement] Add vehicle button clicked', {
      wasOpen: this.showAddForm,
      vehiclesCount: this.vehicles.length,
    });
    this.showAddForm = !this.showAddForm;
    if (!this.showAddForm) {
      console.debug(
        '[VehicleManagement] Add vehicle form closed, resetting form',
      );
      this.addVehicleForm.reset();
      this.error = '';
    } else {
      console.debug('[VehicleManagement] Add vehicle form opened');
    }
  }

  addVehicle(): void {
    console.debug('[VehicleManagement] addVehicle triggered', {
      formValid: this.addVehicleForm.valid,
      formValue: this.addVehicleForm.getRawValue(),
    });
    this.error = '';
    if (this.addVehicleForm.invalid) {
      console.warn(
        '[VehicleManagement] addVehicle blocked due to invalid form',
      );
      this.addVehicleForm.markAllAsTouched();
      return;
    }

    const formValue = this.addVehicleForm.getRawValue();
    this.dataService
      .createVehicle({
        model: formValue.model,
        plateNumber: formValue.plateNumber.toUpperCase(),
        color: formValue.color,
      })
      .subscribe({
        next: (vehicle: Vehicle) => {
          console.info(
            '[VehicleManagement] Vehicle created successfully',
            vehicle,
          );
          this.vehicles.push(vehicle);
          this.addVehicleForm.reset();
          this.showAddForm = false;
          this.successMessage = 'Vehicle added successfully!';
          setTimeout(() => (this.successMessage = ''), 3000);
        },
        error: (err: unknown) => {
          console.error('[VehicleManagement] Vehicle creation failed', err);
          this.error = this.extractErrorMessage(err, 'Failed to add vehicle');
        },
      });
  }

  startEdit(vehicle: Vehicle): void {
    this.editingVehicleId = vehicle.id;
    this.editVehicleForm.patchValue({
      model: vehicle.model,
      plateNumber: vehicle.plateNumber,
      color: vehicle.color,
    });
  }

  cancelEdit(): void {
    this.editingVehicleId = null;
    this.editVehicleForm.reset();
    this.error = '';
  }

  saveEdit(vehicleId: number): void {
    this.error = '';
    if (this.editVehicleForm.invalid) {
      this.editVehicleForm.markAllAsTouched();
      return;
    }

    const formValue = this.editVehicleForm.getRawValue();
    this.dataService
      .updateVehicle(vehicleId, {
        model: formValue.model,
        plateNumber: formValue.plateNumber.toUpperCase(),
        color: formValue.color,
      })
      .subscribe({
        next: (updatedVehicle: Vehicle) => {
          const index = this.vehicles.findIndex((v) => v.id === vehicleId);
          if (index !== -1) {
            this.vehicles[index] = updatedVehicle;
          }
          this.editingVehicleId = null;
          this.editVehicleForm.reset();
          this.successMessage = 'Vehicle updated successfully!';
          setTimeout(() => (this.successMessage = ''), 3000);
        },
        error: (err: unknown) => {
          this.error = this.extractErrorMessage(
            err,
            'Failed to update vehicle',
          );
        },
      });
  }

  deleteVehicle(vehicleId: number): void {
    if (!confirm('Are you sure you want to delete this vehicle?')) {
      return;
    }

    this.dataService.deleteVehicle(vehicleId).subscribe({
      next: () => {
        this.vehicles = this.vehicles.filter((v) => v.id !== vehicleId);
        this.successMessage = 'Vehicle deleted successfully!';
        setTimeout(() => (this.successMessage = ''), 3000);
      },
      error: (err: unknown) => {
        this.error = this.extractErrorMessage(err, 'Failed to delete vehicle');
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
