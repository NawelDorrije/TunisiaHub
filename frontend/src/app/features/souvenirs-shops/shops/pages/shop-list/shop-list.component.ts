import { Component, OnInit } from '@angular/core';
import { Shop } from '../../../../../models/souvenirs-shops/shop.model';
import { ShopService } from '../../../../../services/souvenirs-shops/shop.service';
import { AuthService } from '../../../../auth/services/auth.service';

@Component({
  selector: 'app-shop-list',
  templateUrl: './shop-list.component.html',
  styleUrls: ['./shop-list.component.css']
})
export class ShopListComponent implements OnInit {
  shops: Shop[] = [];
  isLoading = true;
  errorMessage = '';

  constructor(
    private shopService: ShopService,
    public authService: AuthService
  ) {}

  get canManageShops(): boolean {
    return this.authService.isAdmin() || this.authService.isOwner();
  }

  ngOnInit(): void {
  this.shopService.getAllShops().subscribe({
    next: (data: Shop[]) => {
      this.shops = this.getVisibleShops(data);
      this.filteredShops = this.shops;  // ← add this
      this.isLoading = false;
    },
    error: (err: any) => {
      this.errorMessage = 'Failed to load shops. Please try again.';
      this.isLoading = false;
    }
  });
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
filteredShops: Shop[] = [];

filterByCategory(category: string): void {
  this.activeCategory = category;
  if (category === 'ALL') {
    this.filteredShops = this.shops;
  } else {
    this.filteredShops = this.shops.filter(s => s.category === category);
  }
}
}


