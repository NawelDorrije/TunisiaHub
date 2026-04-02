import { Injectable } from '@angular/core';
import { BehaviorSubject, map, Observable, of, tap } from 'rxjs';
import { CartItem } from '../../models/souvenirs-shops/cart-item.model';
import { Order } from '../../models/souvenirs-shops/order.model';
import { OrderService } from './order.service';
import { Shop } from '../../models/souvenirs-shops/shop.model';
import { Product } from '../../models/souvenirs-shops/product.model';

export interface CartGroup {
  shop: Partial<Shop>;
  items: CartItem[];
  totalAmount: number;
}

@Injectable({ providedIn: 'root' })
export class CartService {
  private cartItemsSubject = new BehaviorSubject<CartItem[]>([]);
  items$ = this.cartItemsSubject.asObservable();

  constructor(private orderService: OrderService) {}

  private get currentItems(): CartItem[] {
    return this.cartItemsSubject.value;
  }

  addProduct(product: Product, quantity = 1): void {
    if (!product?.id || !product?.shop?.id || quantity <= 0) {
      return;
    }

    const existingItems = [...this.currentItems];
    const existingItem = existingItems.find((item) => item.product.id === product.id);

    if (existingItem) {
      existingItem.quantity += quantity;
    } else {
      existingItems.push({
        product,
        shop: product.shop,
        quantity,
      });
    }

    this.cartItemsSubject.next(existingItems);
  }

  updateQuantity(productId: number, quantity: number): void {
    const items = [...this.currentItems];
    const item = items.find((cartItem) => cartItem.product.id === productId);
    if (!item) {
      return;
    }

    if (quantity <= 0) {
      this.removeProduct(productId);
      return;
    }

    item.quantity = quantity;
    this.cartItemsSubject.next(items);
  }

  removeProduct(productId: number): void {
    const items = this.currentItems.filter((item) => item.product.id !== productId);
    this.cartItemsSubject.next(items);
  }

  clearCart(): void {
    this.cartItemsSubject.next([]);
  }

  getTotal(): number {
    return this.currentItems.reduce(
      (sum, item) => sum + item.quantity * (item.product.price || 0),
      0
    );
  }

  getOrderGroups(): CartGroup[] {
    const groups = new Map<number, CartGroup>();

    this.currentItems.forEach((item) => {
      const shopId = item.shop?.id;
      if (!shopId) {
        return;
      }

      if (!groups.has(shopId)) {
        groups.set(shopId, {
          shop: item.shop,
          items: [],
          totalAmount: 0,
        });
      }

      const group = groups.get(shopId)!;
      group.items.push(item);
      group.totalAmount = group.items.reduce(
        (sum, current) => sum + current.quantity * (current.product.price || 0),
        0
      );
    });

    return Array.from(groups.values());
  }

  checkout(): Observable<Order[]> {
    const validItems = this.currentItems
      .filter(
        (item): item is CartItem & { product: Required<Pick<Product, 'id'>> } =>
          item.product?.id != null && item.quantity > 0
      )
      .map((item) => ({
        productId: item.product.id,
        quantity: item.quantity,
      }));

    if (validItems.length === 0) {
      return of([]);
    }

    return this.orderService.createOrders({ items: validItems }).pipe(
      tap(() => this.clearCart())
    );
  }
}
