import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Product } from '../../../../../models/souvenirs-shops/product.model';
import { ProductService } from '../../../../../services/souvenirs-shops/product.service';
import { CartService } from '../../../../../services/souvenirs-shops/cart.service';
import { AuthService } from '../../../../auth/services/auth.service';

@Component({
  selector: 'app-product-detail',
  templateUrl: './product-detail.component.html',
  styleUrls: ['./product-detail.component.css']
})
export class ProductDetailComponent implements OnInit {
  product: Product | null = null;
  isLoading = true;
  errorMessage = '';
  selectedImageUrl: string | null = null;

  quantity = 1;
  cartMessage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    private cartService: CartService,
    public authService: AuthService
  ) {}

  get canManageProducts(): boolean {
    return this.authService.isAdmin() || this.authService.isOwner();
  }

  get canAddToCart(): boolean {
    return this.authService.isClient() && !!this.product && this.product.stockQuantity > 0;
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id || isNaN(id)) {
      this.errorMessage = 'Invalid product id.';
      this.isLoading = false;
      return;
    }

    this.productService.getProductById(id).subscribe({
      next: (product) => {
        this.product = product;
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Unable to load product details. Please try again later.';
        this.isLoading = false;
      }
    });
  }

  editProduct(): void {
    if (this.product?.id) {
      this.router.navigate(['/products', this.product.id, 'edit']);
    }
  }

  addToCart(): void {
    if (!this.product || !this.canAddToCart) return;

    this.cartService.addProduct(this.product, this.quantity || 1);
    this.cartMessage = 'Added to cart successfully.';
    setTimeout(() => this.cartMessage = '', 3000);
  }

  backToProductList(event?: Event): void {
    event?.preventDefault();
    if (this.product?.shop?.id) {
      this.router.navigate(['/products', 'shop', this.product.shop.id]);
      return;
    }
    this.router.navigate(['/products']);
  }

  openImageViewer(url?: string): void {
    if (!url) {
      return;
    }
    this.selectedImageUrl = url;
  }

  closeImageViewer(): void {
    this.selectedImageUrl = null;
  }
  increaseQty(): void {
  if (this.quantity < (this.product?.stockQuantity || 1)) {
    this.quantity++;
  }
}

decreaseQty(): void {
  if (this.quantity > 1) {
    this.quantity--;
  }
}
}



