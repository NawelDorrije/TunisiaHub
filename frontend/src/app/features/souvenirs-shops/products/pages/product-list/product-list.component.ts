import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Product } from '../../../../../models/souvenirs-shops/product.model';
import { ProductService } from '../../../../../services/souvenirs-shops/product.service';
import { CartService } from '../../../../../services/souvenirs-shops/cart.service';

@Component({
  selector: 'app-product-list',
  templateUrl: './product-list.component.html',
  styleUrl: './product-list.component.css'
})
export class ProductListComponent implements OnInit {
  products: Product[] = [];
  shopId: number | null = null;
  isLoading = true;
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    private cartService: CartService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const shopIdParam = params.get('shopId');
      this.shopId = shopIdParam ? Number(shopIdParam) : null;
      this.loadProducts();
    });
  }

  loadProducts(): void {
    this.isLoading = true;
    this.errorMessage = '';

    const request = this.shopId !== null && !isNaN(this.shopId)
      ? this.productService.getProductsByShop(this.shopId)
      : this.productService.getAllProducts();

    request.subscribe({
      next: (data) => {
        this.products = data;
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Unable to load products. Please try again later.';
        this.isLoading = false;
      }
    });
  }

  createProduct(): void {
    if (this.shopId !== null && !isNaN(this.shopId)) {
      this.router.navigate(['/products', 'shop', this.shopId, 'new']);
      return;
    }
    this.router.navigate(['/products', 'new']);
  }

  viewProduct(product: Product): void {
    if (product.id) {
      this.router.navigate(['/products', product.id]);
    }
  }

  addToCart(product: Product): void {
    if (!product.id) {
      return;
    }
    this.cartService.addProduct(product, 1);
  }

  editProduct(product: Product): void {
    if (product.id) {
      this.router.navigate(['/products', product.id, 'edit']);
    }
  }

  deleteProduct(product: Product): void {
    if (!product.id) {
      return;
    }

    this.productService.deleteProduct(product.id).subscribe({
      next: () => this.loadProducts(),
      error: () => {
        this.errorMessage = 'Failed to delete product. Please try again.';
      }
    });
  }
}
