import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../../services/api.service';

@Component({
  selector: 'app-admin-manage-restaurants',
  templateUrl: './admin-manage-restaurants.component.html',
  styleUrls: ['./admin-manage-restaurants.component.css'],
})
export class AdminManageRestaurantsComponent implements OnInit {
  restaurants: any[] = [];
  loading = true;
  loadError: string | null = null;

  showAddTableModal = false;
  selectedRestaurant: any = null;
  submitting = false;
  tableStatuses: string[] = [];

  newTable = {
    tableNumber: null as number | null,
    capacity: null as number | null,
    location: '',
    status: 'AVAILABLE',
  };

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.loadRestaurants();
    this.loadTableStatuses();
  }

  loadRestaurants(): void {
    this.loading = true;
    this.loadError = null;
    this.api.getRestaurants().subscribe({
      next: (data) => {
        this.restaurants = Array.isArray(data) ? data : [];
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.loadError = 'Could not load restaurants.';
        this.loading = false;
      },
    });
  }

  loadTableStatuses(): void {
    this.api.getRestaurantTableStatuses().subscribe({
      next: (statuses) => {
        this.tableStatuses = Array.isArray(statuses) && statuses.length > 0 ? statuses : ['AVAILABLE'];
        if (!this.tableStatuses.includes(this.newTable.status)) {
          this.newTable.status = this.tableStatuses[0];
        }
      },
      error: (err) => {
        console.error(err);
        this.tableStatuses = ['AVAILABLE'];
      },
    });
  }

  cuisineLabel(value: unknown): string {
    if (value == null || value === '') return '—';
    return String(value)
      .toLowerCase()
      .replace(/_/g, ' ')
      .replace(/\b\w/g, (m) => m.toUpperCase());
  }

  openAddTableModal(restaurant: any): void {
    this.selectedRestaurant = restaurant;
    this.newTable = {
      tableNumber: null,
      capacity: null,
      location: '',
      status: this.tableStatuses[0] ?? 'AVAILABLE',
    };
    this.submitting = false;
    this.showAddTableModal = true;
  }

  closeAddTableModal(): void {
    this.showAddTableModal = false;
    this.selectedRestaurant = null;
    this.submitting = false;
  }

  submitAddTable(): void {
    if (!this.selectedRestaurant?.id) return;
    const tableNumber = Number(this.newTable.tableNumber);
    const capacity = Number(this.newTable.capacity);

    if (!Number.isFinite(tableNumber) || tableNumber <= 0) {
      alert('Please enter a valid table number.');
      return;
    }
    if (!Number.isFinite(capacity) || capacity <= 0) {
      alert('Please enter a valid capacity.');
      return;
    }

    const payload = {
      tableNumber,
      capacity,
      location: this.newTable.location?.trim() || null,
      status: this.newTable.status || 'AVAILABLE',
      restaurantId: this.selectedRestaurant.id,
    };

    this.submitting = true;
    this.api.addRestaurantTable(payload).subscribe({
      next: () => {
        this.closeAddTableModal();
        alert('Table added successfully.');
      },
      error: (err) => {
        console.error(err);
        this.submitting = false;
        alert('Could not add table for this restaurant.');
      },
    });
  }
}
