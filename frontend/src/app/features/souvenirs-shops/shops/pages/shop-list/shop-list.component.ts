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
    this.shopService.getAllShops().subscribe({
      next: (data: Shop[]) => {
        this.shops = this.getVisibleShops(data);
        this.applyFilters();
        this.isLoading = false;
      },
      error: (err: any) => {
        this.errorMessage = 'Failed to load shops. Please try again.';
        this.isLoading = false;
      }
    });
  }

  private loadNearbyShops(): void {
    if (!this.userLocation) return;

    this.isLoading = true;
    this.shopService.getNearbyShops(this.userLocation.latitude, this.userLocation.longitude, 20).subscribe({
      next: (data: NearbyShopResponse[]) => {
        this.nearbyShops = data;
        this.applyFilters();
        this.isLoading = false;
      },
      error: (err: any) => {
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
        this.errorMessage = 'Unable to get your location. Please check your browser settings.';
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
    if (!ownerEmail) {
      return [];
    }

    return shops.filter((shop) => shop.owner?.email?.toLowerCase() === ownerEmail);
  }
  categories = [
  { value: 'SOUVENIRS', label: 'Souvenirs', color: '#C94B2C' },
  { value: 'CLOTHING',  label: 'Clothing',  color: '#4A90D9' },
  { value: 'JEWELRY',   label: 'Jewelry',   color: '#9B59B6' },
  { value: 'LEATHER',   label: 'Leather',   color: '#8B4513' },
  { value: 'CARPETS',   label: 'Carpets',   color: '#C0392B' },
  { value: 'ART',       label: 'Art',       color: '#27AE60' },
];

activeCategory = 'ALL';


  filterByCategory(category: string): void {
    this.activeCategory = category;
    this.applyFilters();
  }

  private applyFilters(): void {
    const sourceShops: (Shop | NearbyShopResponse)[] = this.showNearby ? this.nearbyShops : this.shops;
    if (this.activeCategory === 'ALL') {
      this.filteredShops = sourceShops;
    } else {
      this.filteredShops = sourceShops.filter(s => s.category === this.activeCategory);
    }
  }
}


