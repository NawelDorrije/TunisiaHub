import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { CartGroup, CartService } from '../../../services/souvenirs-shops/cart.service';
import { CartItem } from '../../../models/souvenirs-shops/cart-item.model';

@Component({
  selector: 'app-cart',
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.css']
})
export class CartComponent implements OnInit, OnDestroy {
  groups: CartGroup[] = [];
  totalAmount = 0;
  isProcessing = false;
  message = '';
  private subscription = new Subscription();

  constructor(private cartService: CartService, private router: Router) {}

  ngOnInit(): void {
    this.subscription.add(
      this.cartService.items$.subscribe(() => this.refreshCart())
    );
    this.refreshCart();
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  refreshCart(): void {
    this.groups = this.cartService.getOrderGroups();
    this.totalAmount = this.cartService.getTotal();
  }

  updateQuantity(productId: number | undefined, event: Event): void {
    if (productId === undefined) {
      return;
    }

    const value = Number((event.target as HTMLInputElement).value);
    if (!Number.isFinite(value)) {
      return;
    }
    this.cartService.updateQuantity(productId, value);
  }

  removeItem(item: CartItem): void {
    if (item.product.id) {
      this.cartService.removeProduct(item.product.id);
    }
  }

  clearCart(): void {
    this.cartService.clearCart();
    this.message = 'Your cart has been cleared.';
  }

  checkout(): void {
    if (this.groups.length === 0) {
      this.message = 'Your cart is empty.';
      return;
    }

    this.isProcessing = true;
    this.message = '';

    this.cartService.checkout().subscribe({
      next: (createdOrders) => {
        this.isProcessing = false;
        this.refreshCart();
        this.router.navigate(['/orders/success'], { state: { createdOrders } });
      },
      error: () => {
        this.isProcessing = false;
        this.message = 'Checkout failed. Please try again later.';
      }
    });
  }

  browseProducts(): void {
    this.router.navigate(['/products']);
  }
}
