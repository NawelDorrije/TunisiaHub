import { Component, OnInit } from '@angular/core';
import { AdminReviewService, OwnerReviewInsights } from '../../services/admin-review.service';
import { AdminShopService, Shop } from '../../services/admin-shop.service';

interface Product {
  id: number;
  name: string;
  shopId?: number;
}

@Component({
  selector: 'app-admin-insights',
  templateUrl: './insights.component.html',
  styleUrls: ['./insights.component.css']
})
export class InsightsComponent implements OnInit {
  reviewInsights: OwnerReviewInsights | null = null;
  shops: Shop[] = [];
  products: Product[] = [];
  selectedShopId: number | null = null;
  selectedProductIds: number[] = [];
  isLoadingShops = false;
  isLoading = false;
  error: string | null = null;

  constructor(
    private reviewService: AdminReviewService,
    private shopService: AdminShopService
  ) {}

  ngOnInit(): void {
    this.loadShops();
    this.loadInsights();
  }

  private loadShops(): void {
    this.isLoadingShops = true;
    this.shopService.getAllShops().subscribe({
      next: (shops: Shop[]) => {
        this.shops = shops;
        this.isLoadingShops = false;
      },
      error: (err: any) => {
        console.error('Error loading shops:', err);
        this.isLoadingShops = false;
      }
    });
  }

  onShopSelected(shopId: number | null): void {
    this.selectedShopId = shopId;
    this.selectedProductIds = [];
    this.products = [];
    
    if (shopId) {
      this.shopService.getShopById(shopId).subscribe({
        next: (shop: any) => {
          this.products = shop.products || [];
          // Auto-reload insights when shop changes
          this.loadInsights();
        },
        error: (err: any) => {
          console.error('Error loading shop products:', err);
        }
      });
    } else {
      // If shop is deselected, reload unfiltered insights
      this.loadInsights();
    }
  }

  onProductSelected(productId: number, checked: boolean): void {
    if (checked) {
      this.selectedProductIds.push(productId);
    } else {
      this.selectedProductIds = this.selectedProductIds.filter(id => id !== productId);
    }
  }

  applyFilters(): void {
    this.loadInsights();
  }

  private loadInsights(): void {
    this.isLoading = true;
    this.error = null;

    // Pass filter parameters if they're set
    const shopId = this.selectedShopId || undefined;
    const productIds = this.selectedProductIds.length > 0 ? this.selectedProductIds : undefined;

    this.reviewService.getOwnerReviewInsights(shopId, productIds).subscribe({
      next: (insights: OwnerReviewInsights) => {
        this.reviewInsights = insights;
        this.isLoading = false;
      },
      error: (err: any) => {
        console.error('Error loading review insights:', err);
        this.error = 'Unable to load review insights at the moment.';
        this.isLoading = false;
      }
    });
  }
}
