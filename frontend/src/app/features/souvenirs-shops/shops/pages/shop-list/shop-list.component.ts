import { Component, OnInit } from '@angular/core';
import { Shop, NearbyShopResponse } from '../../../../../models/souvenirs-shops/shop.model';
import { ShopService } from '../../../../../services/souvenirs-shops/shop.service';
import { AuthService } from '../../../../auth/services/auth.service';

@Component({
  selector: 'app-shop-list',
  templateUrl: './shop-list.component.html',
  styleUrls: ['./shop-list.component.css']
})
export class ShopListComponent implements OnInit {
  shops: Shop[] = [];
  filteredShops: (Shop | NearbyShopResponse)[] = [];
  nearbyShops: NearbyShopResponse[] = [];

  isLoading = true;
  errorMessage = '';
  showNearby = false;
  isGettingLocation = false;
  userLocation: { latitude: number; longitude: number } | null = null;

  // Radius options
  radiusOptions = [1, 2, 3, 5, 10, 15];
  selectedRadius = 5;

  // Category filter
  categories = [
    { value: 'ALL',       label: 'All',         color: '#6B7280' },
    { value: 'SOUVENIRS', label: 'Souvenirs',   color: '#C94B2C' },
    { value: 'CLOTHING',  label: 'Clothing',    color: '#4A90D9' },
    { value: 'JEWELRY',   label: 'Jewelry',     color: '#9B59B6' },
    { value: 'LEATHER',   label: 'Leather',     color: '#8B4513' },
    { value: 'CARPETS',   label: 'Carpets',     color: '#C0392B' },
    { value: 'ART',       label: 'Art',         color: '#27AE60' },
  ];

  activeCategory = 'ALL';

  constructor(
    private shopService: ShopService,
    public authService: AuthService
  ) {}

  get canManageShops(): boolean {
    return this.authService.isOwner();
  }

  ngOnInit(): void {
    this.loadAllShops();
  }

  private loadAllShops(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.shopService.getAllShops().subscribe({
      next: (data: Shop[]) => {
        this.shops = this.getVisibleShops(data);
        this.applyFilters();
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load shops. Please try again.';
        this.isLoading = false;
        console.error(err);
      }
    });
  }

  private loadNearbyShops(): void {
    if (!this.userLocation) return;

    this.isLoading = true;
    this.errorMessage = '';

    this.shopService.getNearbyShops(
      this.userLocation.latitude,
      this.userLocation.longitude,
      this.selectedRadius
    ).subscribe({
      next: (data: NearbyShopResponse[]) => {
        this.nearbyShops = data || [];
        this.applyFilters();
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading nearby shops:', err);
        this.errorMessage = 'Failed to load nearby shops. Please try again.';
        this.isLoading = false;
      }
    });
  }

  toggleNearbyMode(): void {
    this.showNearby = !this.showNearby;

    if (this.showNearby) {
      this.getUserLocation();
    } else {
      this.loadAllShops();
    }
  }

  onRadiusChange(newRadius: number | string): void {
    this.selectedRadius = typeof newRadius === 'string' ? parseInt(newRadius, 10) : newRadius;

    if (this.showNearby && this.userLocation) {
      this.loadNearbyShops();
    } else if (!this.showNearby) {
      this.applyFilters();
    }
  }

  private getUserLocation(): void {
    if (!navigator.geolocation) {
      this.errorMessage = 'Geolocation is not supported by this browser.';
      return;
    }

    this.isGettingLocation = true;
    navigator.geolocation.getCurrentPosition(
      (position) => {
        this.userLocation = {
          latitude: position.coords.latitude,
          longitude: position.coords.longitude
        };
        this.isGettingLocation = false;
        this.loadNearbyShops();
      },
      (error) => {
        this.isGettingLocation = false;
        this.errorMessage = 'Unable to get your location. Please check browser settings.';
        console.error('Geolocation error:', error);
      },
      { enableHighAccuracy: true, timeout: 10000 }
    );
  }

  private getVisibleShops(shops: Shop[]): Shop[] {
    if (!this.authService.isOwner()) {
      return shops;
    }

    const ownerEmail = this.authService.getEmail()?.toLowerCase();
    if (!ownerEmail) return [];

    return shops.filter(shop => shop.owner?.email?.toLowerCase() === ownerEmail);
  }

  filterByCategory(category: string): void {
    this.activeCategory = category;
    this.applyFilters();
  }

  private applyFilters(): void {
    const sourceShops: (Shop | NearbyShopResponse)[] = this.showNearby 
      ? this.nearbyShops 
      : this.shops;

    if (this.activeCategory === 'ALL') {
      this.filteredShops = [...sourceShops];
    } else {
      this.filteredShops = sourceShops.filter(s => s.category === this.activeCategory);
    }
  }

  // ==================== NEW METHOD ADDED ====================
  deleteShop(shop: any): void {
    if (!confirm(`Are you sure you want to delete the shop "${shop.name}"?`)) {
      return;
    }

    this.isLoading = true;

    this.shopService.deleteShop(shop.id).subscribe({
      next: () => {
        // Remove from local lists
        this.shops = this.shops.filter(s => s.id !== shop.id);
        this.nearbyShops = this.nearbyShops.filter(s => s.id !== shop.id);
        this.applyFilters();
        
        alert('Shop deleted successfully.');
      },
      error: (err) => {
        console.error('Error deleting shop:', err);
        this.errorMessage = 'Failed to delete shop. Please try again.';
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }

  // Optional: Image error handler
  handleImageError(event: any, shop: any): void {
    console.warn(`Failed to load image for shop: ${shop.name}`);
    event.target.src = 'assets/images/shop-placeholder.jpg'; // Make sure this file exists
  }
}