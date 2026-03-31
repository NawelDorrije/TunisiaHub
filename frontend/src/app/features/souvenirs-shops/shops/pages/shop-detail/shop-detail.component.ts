import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Shop } from '../../../../../models/souvenirs-shops/shop.model';
import { ShopService } from '../../../../../services/souvenirs-shops/shop.service';

@Component({
  selector: 'app-shop-detail',
  templateUrl: './shop-detail.component.html',
  styleUrl: './shop-detail.component.css'
})
export class ShopDetailComponent implements OnInit {
  shop: Shop | null = null;
  productCount = 0;
  isLoading = true;
  isDeleting = false;
  errorMessage = '';
  selectedImageUrl: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private shopService: ShopService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id || isNaN(id)) {
      this.errorMessage = 'Invalid shop id.';
      this.isLoading = false;
      return;
    }

    this.shopService.getShopById(id).subscribe({
      next: (shop) => {
        this.shop = shop;
        this.loadProductCount(id);
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Unable to load shop details. Please try again later.';
        this.isLoading = false;
      }
    });
  }

  private loadProductCount(shopId: number): void {
    this.shopService.getProductsByShop(shopId).subscribe({
      next: (products) => {
        this.productCount = products.length;
      },
      error: () => {
        this.productCount = 0;
      }
    });
  }

  editShop(): void {
    if (this.shop?.id) {
      this.router.navigate(['/shops', this.shop.id, 'edit']);
    }
  }

  deleteShop(): void {
    if (!this.shop?.id || this.isDeleting) {
      return;
    }

    const confirmed = window.confirm('Delete this shop and all its data? This action cannot be undone.');
    if (!confirmed) {
      return;
    }

    this.isDeleting = true;
    this.shopService.deleteShop(this.shop.id).subscribe({
      next: () => {
        this.router.navigate(['/shops']);
      },
      error: () => {
        this.errorMessage = 'Unable to delete shop. Please try again.';
        this.isDeleting = false;
      }
    });
  }

  viewProducts(): void {
    if (this.shop?.id) {
      this.router.navigate(['/products', 'shop', this.shop.id]);
    }
  }

  goBack(): void {
    this.router.navigate(['/shops']);
  }

  openImageViewer(url: string): void {
    this.selectedImageUrl = url;
  }

  closeImageViewer(): void {
    this.selectedImageUrl = null;
  }
}
