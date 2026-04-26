import { Component, OnInit, Inject, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { ApiService } from '../../services/api.service';
import { Router } from '@angular/router';
import { AuthService } from '../auth/services/auth.service';
import { AiSearchService } from '../../services/ai-search.service';
import { AiEnhancedRestaurant } from '../../models/ai-search/ai-enhanced-restaurant.model';

@Component({
  selector: 'app-restaurants',
  templateUrl: './restaurants.component.html',
  styleUrls: ['./restaurants.component.css']
})
export class RestaurantsComponent implements OnInit {
  private aiSearchService = inject(AiSearchService);

  restaurants: any[] = [];
  aiResults: AiEnhancedRestaurant[] = []; // Store AI-enhanced versions
  isAiMode: boolean = false; // Flag to check if we are showing AI results
  
  selectedRestaurant: any = null;
  showDetailsModal: boolean = false;
  showAddForm: boolean = false;
  searchAddress: string = '';
  selectedCuisine: string = '';
  cuisineOptions: string[] = [];
  isCuisinesLoading: boolean = false;
  mapSearchQuery: string = '';
  newRestaurant = {
    name: '',
    address: '',
    latitude: null as number | null,
    longitude: null as number | null,
    email: '',
    phoneNum: '',
    cuisine: '',
    picture: ''
  };
  addMap: any = null;
  addMarker: any = null;

  isSubmitting: boolean = false;
  showEditForm: boolean = false;
  editRestaurant: {
    id: number | null;
    name: string;
    address: string;
    email: string;
    phoneNum: string;
    cuisine: string;
    picture: string;
  } = {
    id: null,
    name: '',
    address: '',
    email: '',
    phoneNum: '',
    cuisine: '',
    picture: '',
  };
  isSubmittingEdit: boolean = false;

  constructor(
    private api: ApiService,
    private router: Router,
    private auth: AuthService,
    @Inject(PLATFORM_ID) private platformId: Object
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
  selectedTableIds: number[] = [];

  // AI Suggestion State
  aiSuggestion: any = null;
  isAiSuggestionLoading = false;
  aiSuggestionError = '';
  lastCheckedDate = '';

  ngOnInit(): void {
    this.api.getRestaurants().subscribe({
      next: (data) => {
        this.restaurants = Array.isArray(data) ? data : [];
      },
      error: (err) => console.error(err),
    });

    this.loadRestaurantCuisines();
  }

  private loadRestaurantCuisines(): void {
    this.isCuisinesLoading = true;
    this.api.getRestaurantCuisines().subscribe({
      next: (data: any[]) => {
        this.cuisineOptions = Array.isArray(data) ? data.map((c) => String(c)) : [];
        if (!this.selectedCuisine) this.selectedCuisine = '';
        this.isCuisinesLoading = false;
      },
      error: (err) => {
        console.error('Error fetching restaurant cuisines:', err);
        this.cuisineOptions = [];
        this.isCuisinesLoading = false;
      },
    });
  }

  private toTitleCase(value: string): string {
    return value
      .toLowerCase()
      .replace(/_/g, ' ')
      .split(/[\s]+/g)
      .filter(Boolean)
      .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  }

  getCuisineLabel(cuisine: unknown): string {
    const value = cuisine == null ? '' : String(cuisine);
    if (!value) return '—';
    return this.toTitleCase(value);
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
    if (this.isAiMode) {
      return this.aiResults;
    }

    let filtered = this.restaurants;

    if (this.searchAddress.trim()) {
      const searchTerm = this.searchAddress.toLowerCase();
      filtered = filtered.filter((restaurant) =>
        (restaurant.address ?? '').toLowerCase().includes(searchTerm)
      );
    }

    if (this.selectedCuisine) {
      const normalizedSelected = this.selectedCuisine.trim().toUpperCase();
      filtered = filtered.filter(
        (restaurant) => String(restaurant.cuisine ?? '').toUpperCase() === normalizedSelected
      );
    }

    return filtered;
  }

  onAiSearch(query: string): void {
    if (!query || !query.trim()) {
      this.resetSearch();
      return;
    }

    this.aiSearchService.search(query).subscribe({
      next: (response) => {
        if (response && response.results) {
          this.isAiMode = true;
          // Merge results with local restaurant data
          this.aiResults = response.results.map((item: any) => {
            const original = this.restaurants.find((r) => r.id === item.id);
            if (original) {
              return {
                ...original,
                matchScore: item.matchScore,
                matchReason: item.matchReason,
                suggestedTime: item.suggestedTime
              };
            }
            return null;
          }).filter(Boolean) as AiEnhancedRestaurant[];
        }
      },
      error: (err) => {
        console.error('Error during AI search in Restaurants component:', err);
      }
    });
  }

  resetSearch(): void {
    this.isAiMode = false;
    this.aiResults = [];
    this.searchAddress = '';
    this.selectedCuisine = '';
  }

  onFileSelected(event: any, target: 'new' | 'edit'): void {
    const file = event.target.files[0];
    if (file) {
      if (target === 'new') this.isSubmitting = true;
      else this.isSubmittingEdit = true;

      this.api.uploadRestaurantPicture(file).subscribe({
        next: (resp: any) => {
          if (target === 'new') {
            this.newRestaurant.picture = resp.picture;
            this.isSubmitting = false;
          } else {
            this.editRestaurant.picture = resp.picture;
            this.isSubmittingEdit = false;
          }
        },
        error: (err) => {
          console.error('Upload error', err);
          alert('Error uploading picture');
          if (target === 'new') this.isSubmitting = false;
          else this.isSubmittingEdit = false;
        }
      });
    }
  }

  getImageUrl(path: string): string {
    return this.api.getImageUrl(path);
  }

  openAddForm(): void {
    if (!this.isAdmin) return;
    this.showAddForm = true;
    this.newRestaurant = {
      name: '',
      address: '',
      latitude: null,
      longitude: null,
      email: '',
      phoneNum: '',
      cuisine: this.cuisineOptions[0] ?? '',
      picture: '',
    };
    
    // Initialize map after modal is rendered
    setTimeout(() => {
      this.initAddMap();
    }, 200);
  }

  private initAddMap(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    if (this.addMap) {
      this.addMap.invalidateSize();
      return;
    }

    import('leaflet').then((L) => {
      // Fix marker icons
      const iconDefault = L.icon({
        iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
        iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
        shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
        iconSize: [25, 41],
        iconAnchor: [12, 41],
        popupAnchor: [1, -34],
        tooltipAnchor: [16, -28],
        shadowSize: [41, 41]
      });
      L.Marker.prototype.options.icon = iconDefault;

      this.addMap = L.map('add-restaurant-map').setView([33.8869, 9.5375], 6); // default view to Tunisia
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; OpenStreetMap contributors'
      }).addTo(this.addMap!);

      this.addMap.on('click', (e: any) => {
        const lat = e.latlng.lat;
        const lng = e.latlng.lng;
        
        this.newRestaurant.latitude = lat;
        this.newRestaurant.longitude = lng;

        if (!this.addMarker) {
          this.addMarker = L.marker([lat, lng]).addTo(this.addMap!);
        } else {
          this.addMarker.setLatLng([lat, lng]);
        }

        this.fetchAddressFromCoordinates(lat, lng);
      });
    });
  }

  private fetchAddressFromCoordinates(lat: number, lng: number): void {
    const url = `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}`;
    fetch(url)
      .then(response => response.json())
      .then(data => {
        if (data && data.display_name) {
          this.newRestaurant.address = data.display_name;
        }
      })
      .catch(error => {
        console.error('Error fetching address:', error);
      });
  }

  searchMapLocation(): void {
    if (!this.mapSearchQuery.trim()) return;
    
    const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(this.mapSearchQuery)}&limit=1`;
    fetch(url)
      .then(response => response.json())
      .then(data => {
        if (data && data.length > 0) {
          const result = data[0];
          const lat = parseFloat(result.lat);
          const lng = parseFloat(result.lon);
          
          this.newRestaurant.latitude = lat;
          this.newRestaurant.longitude = lng;
          this.newRestaurant.address = result.display_name;

          if (this.addMap) {
            import('leaflet').then((L) => {
              this.addMap.setView([lat, lng], 14);
              if (!this.addMarker) {
                this.addMarker = L.marker([lat, lng]).addTo(this.addMap);
              } else {
                this.addMarker.setLatLng([lat, lng]);
              }
            });
          }
        } else {
          alert('Location not found. Please try a different search.');
        }
      })
      .catch(error => {
        console.error('Error searching location:', error);
      });
  }

  closeAddForm(): void {
    this.showAddForm = false;
    this.newRestaurant = { name: '', address: '', latitude: null, longitude: null, email: '', phoneNum: '', cuisine: '', picture: '' };
    if (this.addMap) {
      this.addMap.remove();
      this.addMap = null;
      this.addMarker = null;
    }
  }

  submitAddRestaurant(): void {
    if (!this.isAdmin) return;
    if (
      !this.newRestaurant.name ||
      !this.newRestaurant.address ||
      !this.newRestaurant.email ||
      !this.newRestaurant.phoneNum ||
      !this.newRestaurant.cuisine
    ) {
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
      cuisine: restaurant.cuisine ?? '',
      picture: restaurant.picture ?? '',
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
      cuisine: '',
      picture: '',
    };
    this.isSubmittingEdit = false;
  }

  submitEditRestaurant(): void {
    if (!this.isAdmin || this.editRestaurant.id == null) return;
    if (
      !this.editRestaurant.name ||
      !this.editRestaurant.address ||
      !this.editRestaurant.email ||
      !this.editRestaurant.phoneNum ||
      !this.editRestaurant.cuisine
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
      cuisine: this.editRestaurant.cuisine,
      picture: this.editRestaurant.picture,
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
    this.selectedTableIds = [];
    this.showReservationForm = true;
  }

  onTableSelectionChange(ids: number[]): void {
    this.selectedTableIds = ids;
  }

  closeReservationForm(): void {
    this.showReservationForm = false;
    this.reservationRestaurant = null;
    this.isSubmittingReservation = false;
    // Reset AI suggestions
    this.aiSuggestion = null;
    this.isAiSuggestionLoading = false;
    this.aiSuggestionError = '';
    this.lastCheckedDate = '';
  }

  onReservationDateChange(): void {
    if (!this.reservationDateTime) return;
    
    // Extract YYYY-MM-DD from datetime string (e.g. "2026-04-26T19:00")
    const dateStr = this.reservationDateTime.split('T')[0];
    
    if (dateStr && dateStr !== this.lastCheckedDate && this.reservationRestaurant?.id) {
      this.lastCheckedDate = dateStr;
      this.isAiSuggestionLoading = true;
      this.aiSuggestionError = '';
      this.aiSuggestion = null;

      this.api.getAiReservationSuggestion(this.reservationRestaurant.id, dateStr).subscribe({
        next: (res) => {
          this.isAiSuggestionLoading = false;
          this.aiSuggestion = res; // e.g. { bestTime: "19:00", message: "..." }
        },
        error: (err) => {
          console.error('Error fetching AI suggestion:', err);
          this.isAiSuggestionLoading = false;
          this.aiSuggestionError = err?.error?.message || 'Could not fetch AI recommendation at this time.';
        }
      });
    }
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
      tables: this.selectedTableIds.map(id => ({ id })),
      status: 'PENDING'
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
        alert('Could not create reservation. Please check your selection and try again.');
      },
    });
  }
}
