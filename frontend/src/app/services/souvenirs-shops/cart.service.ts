import { Injectable } from '@angular/core';
import { BehaviorSubject, forkJoin, map, of, Observable, switchMap, tap } from 'rxjs';
import { CartItem } from '../../models/souvenirs-shops/cart-item.model';
import { Order } from '../../models/souvenirs-shops/order.model';
import { OrderItem } from '../../models/souvenirs-shops/order-item.model';
import { OrderStatus } from '../../models/souvenirs-shops/order-status.enum';
import { OrderService } from './order.service';
import { OrderItemService } from './order-item.service';
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

  constructor(
    private orderService: OrderService,
    private orderItemService: OrderItemService
  ) {}

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

  checkout(userId = 1): Observable<{ shop: Partial<Shop>; order: Order; items: OrderItem[] }[]> {
    const groups = this.getOrderGroups();
    if (groups.length === 0) {
      return of([]);
    }

    const checkoutRequests = groups.map((group) => {
      const order: Order = {
        user: { id: userId } as any,
        shop: { id: group.shop.id } as any,
        status: OrderStatus.PENDING,
        totalAmount: group.totalAmount,
      };

      return this.orderService.addOrder(order).pipe(
        switchMap((createdOrder) => {
          const orderItemRequests = group.items.map((item) => {
            const orderItem: OrderItem = {
              order: { id: createdOrder.id } as any,
              product: { id: item.product.id } as any,
              quantity: item.quantity,
              unitPrice: item.product.price,
            };
            return this.orderItemService.addOrderItem(orderItem);
          });
          return forkJoin(orderItemRequests).pipe(
            map((createdItems) => ({ shop: group.shop, order: createdOrder, items: createdItems }))
          );
        })
      );
    });

    return forkJoin(checkoutRequests).pipe(tap(() => this.clearCart()));
  }
}
