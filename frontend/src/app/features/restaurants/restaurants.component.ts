import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { Router } from '@angular/router';
import { AuthService } from '../auth/services/auth.service';

@Component({
  selector: 'app-restaurants',
  templateUrl: './restaurants.component.html',
  styleUrls: ['./restaurants.component.css']
})
export class RestaurantsComponent implements OnInit {
  restaurants: any[] = [];
  selectedRestaurant: any = null;
  showDetailsModal: boolean = false;
  showAddForm: boolean = false;
  searchAddress: string = '';
  newRestaurant = {
    name: '',
    address: '',
    email: '',
    phoneNum: ''
  };
  isSubmitting: boolean = false;
  showEditForm: boolean = false;
  editRestaurant: {
    id: number | null;
    name: string;
    address: string;
    email: string;
    phoneNum: string;
  } = {
    id: null,
    name: '',
    address: '',
    email: '',
    phoneNum: '',
  };
  isSubmittingEdit: boolean = false;

  constructor(
    private api: ApiService,
    private router: Router,
    private auth: AuthService
  ) {}

  get isAdmin(): boolean {
    return this.auth.isAdmin();
  }

  get isClient(): boolean {
    return this.auth.isClient();
  }

  showReservationForm = false;
  reservationRestaurant: any = null;
  reservationDateTime = '';
  reservationPartySize: number | null = 2;
  reservationNotes = '';
  isSubmittingReservation = false;

  ngOnInit(): void {
    this.api.getRestaurants().subscribe({
      next: (data) => {
        this.restaurants = Array.isArray(data) ? data : [];
      },
      error: (err) => console.error(err),
    });
  }

  viewDetails(restaurant: any): void {
    this.selectedRestaurant = restaurant;
    this.showDetailsModal = true;
  }

  viewMenus(restaurant: any): void {
    // Navigate to menus page with restaurantId as query parameter
    this.router.navigate(['/menus'], { queryParams: { restaurantId: restaurant.id } });
  }

  closeDetailsModal(): void {
    this.showDetailsModal = false;
    this.selectedRestaurant = null;
  }

  getFilteredRestaurants(): any[] {
    if (!this.searchAddress.trim()) {
      return this.restaurants;
    }
    const searchTerm = this.searchAddress.toLowerCase();
    return this.restaurants.filter((restaurant) =>
      (restaurant.address ?? '').toLowerCase().includes(searchTerm)
    );
  }

  openAddForm(): void {
    if (!this.isAdmin) return;
    this.showAddForm = true;
    this.newRestaurant = { name: '', address: '', email: '', phoneNum: '' };
  }

  closeAddForm(): void {
    this.showAddForm = false;
    this.newRestaurant = { name: '', address: '', email: '', phoneNum: '' };
  }

  submitAddRestaurant(): void {
    if (!this.isAdmin) return;
    if (!this.newRestaurant.name || !this.newRestaurant.address || !this.newRestaurant.email || !this.newRestaurant.phoneNum) {
      alert('Please fill in all fields');
      return;
    }

    this.isSubmitting = true;
    this.api.addRestaurant(this.newRestaurant).subscribe(
      data => {
        console.log('Restaurant added:', data);
        this.restaurants.push(data);
        this.closeAddForm();
        this.isSubmitting = false;
        alert('Restaurant added successfully!');
      },
      err => {
        console.error('Error adding restaurant:', err);
        this.isSubmitting = false;
        alert('Error adding restaurant. Please try again.');
      }
    );
  }

  openEditForm(restaurant: any): void {
    if (!this.isAdmin) return;
    this.editRestaurant = {
      id: restaurant.id,
      name: restaurant.name ?? '',
      address: restaurant.address ?? '',
      email: restaurant.email ?? '',
      phoneNum: restaurant.phoneNum ?? '',
    };
    this.showEditForm = true;
  }

  closeEditForm(): void {
    this.showEditForm = false;
    this.editRestaurant = {
      id: null,
      name: '',
      address: '',
      email: '',
      phoneNum: '',
    };
    this.isSubmittingEdit = false;
  }

  submitEditRestaurant(): void {
    if (!this.isAdmin || this.editRestaurant.id == null) return;
    if (
      !this.editRestaurant.name ||
      !this.editRestaurant.address ||
      !this.editRestaurant.email ||
      !this.editRestaurant.phoneNum
    ) {
      alert('Please fill in all fields');
      return;
    }

    this.isSubmittingEdit = true;
    const payload = {
      id: this.editRestaurant.id,
      name: this.editRestaurant.name,
      address: this.editRestaurant.address,
      email: this.editRestaurant.email,
      phoneNum: this.editRestaurant.phoneNum,
    };
    this.api.updateRestaurant(payload).subscribe({
      next: (data) => {
        const idx = this.restaurants.findIndex((r) => r.id === data.id);
        if (idx !== -1) this.restaurants[idx] = data;
        if (this.selectedRestaurant?.id === data.id) this.selectedRestaurant = data;
        this.closeEditForm();
        alert('Restaurant updated successfully!');
      },
      error: (err) => {
        console.error('Error updating restaurant:', err);
        this.isSubmittingEdit = false;
        alert('Error updating restaurant. Please try again.');
      },
    });
  }

  confirmDeleteRestaurant(restaurant: any): void {
    if (!this.isAdmin) return;
    if (!confirm(`Delete restaurant "${restaurant.name}"? This cannot be undone.`)) return;
    this.api.deleteRestaurant(restaurant.id).subscribe({
      next: () => {
        this.restaurants = this.restaurants.filter((r) => r.id !== restaurant.id);
        if (this.selectedRestaurant?.id === restaurant.id) this.closeDetailsModal();
        alert('Restaurant deleted.');
      },
      error: (err) => {
        console.error('Error deleting restaurant:', err);
        alert('Error deleting restaurant. Please try again.');
      },
    });
  }

  openReservationForm(restaurant: any): void {
    if (!this.isClient) return;
    this.reservationRestaurant = restaurant;
    this.reservationDateTime = '';
    this.reservationPartySize = 2;
    this.reservationNotes = '';
    this.showReservationForm = true;
  }

  closeReservationForm(): void {
    this.showReservationForm = false;
    this.reservationRestaurant = null;
    this.isSubmittingReservation = false;
  }

  submitReservation(): void {
    if (!this.isClient || !this.reservationRestaurant?.id) return;
    if (!this.reservationDateTime?.trim()) {
      alert('Please choose date and time for your reservation.');
      return;
    }
    const party = Number(this.reservationPartySize);
    if (!Number.isFinite(party) || party < 1) {
      alert('Please enter a valid party size (at least 1).');
      return;
    }
    let dateTime = this.reservationDateTime.trim();
    if (dateTime.length === 16) {
      dateTime = `${dateTime}:00`;
    }
    const payload: Record<string, unknown> = {
      type: 'RestaurantReservation',
      restaurant: { id: this.reservationRestaurant.id },
      dateTime,
      partySize: party,
    };
    const notes = this.reservationNotes?.trim();
    if (notes) payload['notes'] = notes;

    this.isSubmittingReservation = true;
    this.api.createReservation(payload).subscribe({
      next: () => {
        this.closeReservationForm();
        alert('Reservation request sent. Status: pending — the restaurant will confirm.');
      },
      error: (err) => {
        console.error('Reservation error:', err);
        this.isSubmittingReservation = false;
        alert('Could not create reservation. Sign in as a client and try again.');
      },
    });
  }
}
