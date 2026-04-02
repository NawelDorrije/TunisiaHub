import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { CartGroup, CartService } from '../../../services/souvenirs-shops/cart.service';
import { CartItem } from '../../../models/souvenirs-shops/cart-item.model';
import { AuthService } from '../../auth/services/auth.service';

@Component({
  selector: 'app-cart',
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.css']
})
export class CartComponent implements OnInit, OnDestroy {
  groups: CartGroup[] = [];
  totalAmount = 0;
  isProcessing = false;
  canCheckout = false;
  message = '';
  private subscription = new Subscription();

  constructor(
    private cartService: CartService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.subscription.add(
      this.cartService.items$.subscribe(() => this.refreshCart())
    );
    this.refreshCart();
    this.canCheckout = this.authService.isClient();
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  refreshCart(): void {
    this.groups = this.cartService.getOrderGroups();
    this.totalAmount = this.cartService.getTotal();
    this.canCheckout = this.authService.isClient();
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
    if (!this.canCheckout) {
      this.message = 'Only clients can checkout. Please sign in with a client account.';
      return;
    }
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
        try {
          sessionStorage.setItem('createdOrders', JSON.stringify(createdOrders));
        } catch {
          // ignore storage errors
        }
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
  asEvent(value: number): Event {
  const event = { target: { value: String(value) } } as unknown as Event;
  return event;
}
}
