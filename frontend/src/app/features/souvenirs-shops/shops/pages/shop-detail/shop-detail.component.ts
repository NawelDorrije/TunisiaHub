import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Shop } from '../../../../../models/souvenirs-shops/shop.model';
import { Product } from '../../../../../models/souvenirs-shops/product.model';
import { ShopService } from '../../../../../services/souvenirs-shops/shop.service';
import { CartService } from '../../../../../services/souvenirs-shops/cart.service';
import { AuthService } from '../../../../auth/services/auth.service';

@Component({
  selector: 'app-shop-detail',
  templateUrl: './shop-detail.component.html',
  styleUrls: ['./shop-detail.component.css']
})
export class ShopDetailComponent implements OnInit {
  shop: Shop | null = null;
  productCount = 0;
  orderCount = 0;
  isLoading = true;
  isDeleting = false;
  errorMessage = '';
  selectedImageUrl: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private shopService: ShopService,
    private cartService: CartService,
    public authService: AuthService
  ) {}

  get canManageShop(): boolean {
    return this.authService.isAdmin() || this.authService.isOwner();
  }

  get canAddToCart(): boolean {
    return this.authService.isClient();
  }

  addToCart(product: Product): void {
    if (!product?.id || !this.canAddToCart || product.stockQuantity <= 0) {
      return;
    }

    this.cartService.addProduct(product, 1);
  }

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
        this.loadOrderCount(id);
        if (!this.shop.products) {
          this.loadShopProducts(id);
        }
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
        if (this.shop) {
          this.shop.products = products;
        }
      },
      error: () => {
        this.productCount = 0;
      }
    });
  }

  private loadShopProducts(shopId: number): void {
    this.shopService.getProductsByShop(shopId).subscribe({
      next: (products) => {
        if (this.shop) {
          this.shop.products = products;
        }
      },
      error: () => {
        // Keep existing product state if loading fails
      }
    });
  }

  private loadOrderCount(shopId: number): void {
    this.shopService.getOrdersByShop(shopId).subscribe({
      next: (orders) => {
        this.orderCount = orders.length;
      },
      error: () => {
        this.orderCount = this.shop?.orders?.length ?? 0;
      }
    });
  }

  viewOrders(): void {
    this.router.navigate(['/orders']);
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



