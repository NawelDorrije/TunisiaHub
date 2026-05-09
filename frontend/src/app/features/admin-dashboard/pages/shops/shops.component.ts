import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AdminShopService, Shop } from '../../services/admin-shop.service';

@Component({
  selector: 'app-admin-shops',
  templateUrl: './shops.component.html',
  styleUrls: ['./shops.component.css']
})
export class ShopsComponent implements OnInit {
  shops: Shop[] = [];
  isLoading = false;
  error: string | null = null;

  constructor(private shopService: AdminShopService, private router: Router) {}

  ngOnInit(): void {
    this.loadShops();
  }

  loadShops(): void {
    this.isLoading = true;
    this.error = null;

    this.shopService.getAllShops().subscribe({
      next: (data) => {
        this.shops = data;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading shops:', err);
        this.error = 'Failed to load shops. Please try again.';
        this.isLoading = false;
      }
    });
  }

  deleteShop(id: number): void {
    if (confirm('Are you sure you want to delete this shop?')) {
      this.shopService.deleteShop(id).subscribe({
        next: () => {
          this.shops = this.shops.filter(s => s.id !== id);
        },
        error: (err) => {
          console.error('Error deleting shop:', err);
          alert('Failed to delete shop: ' + (err.error?.message || 'Unknown error'));
        }
      });
    }
  }

  viewDetails(id: number): void {
    // Navigate to shop details page
    this.router.navigate(['/shops', id]);
  }
}
