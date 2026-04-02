import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Order } from '../../../../../models/souvenirs-shops/order.model';
import { OrderService } from '../../../../../services/souvenirs-shops/order.service';

@Component({
  selector: 'app-order-success',
  templateUrl: './order-success.component.html',
  styleUrls: ['./order-success.component.css']
})
export class OrderSuccessComponent implements OnInit {
  createdOrders: Order[] = [];
  isLoading = true;
  fallbackMessage = '';

  constructor(private router: Router, private orderService: OrderService) {}

  ngOnInit(): void {
    const navigation = this.router.getCurrentNavigation();
    const state = navigation?.extras?.state as { createdOrders?: Order[] } | undefined;

    if (state?.createdOrders?.length) {
      this.createdOrders = state.createdOrders;
      try {
        sessionStorage.setItem('createdOrders', JSON.stringify(this.createdOrders));
      } catch {
        // ignore storage errors
      }
      this.isLoading = false;
      return;
    }

    const persisted = this.loadPersistedCreatedOrders();
    if (persisted.length > 0) {
      this.createdOrders = persisted;
      this.isLoading = false;
      return;
    }

    this.fallbackMessage = 'No recent order details are available. You can view your orders below.';
    this.orderService.getAllOrders().subscribe({
      next: (orders) => {
        this.createdOrders = orders.slice(-5).reverse();
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  viewMyOrders(): void {
    this.clearPersistedCreatedOrders();
    this.router.navigate(['/orders']);
  }

  continueShopping(): void {
    this.clearPersistedCreatedOrders();
    this.router.navigate(['/products']);
  }

  private loadPersistedCreatedOrders(): Order[] {
    try {
      const raw = sessionStorage.getItem('createdOrders');
      if (!raw) {
        return [];
      }
      const parsed = JSON.parse(raw) as Order[];
      return Array.isArray(parsed) ? parsed : [];
    } catch {
      return [];
    }
  }

  private clearPersistedCreatedOrders(): void {
    try {
      sessionStorage.removeItem('createdOrders');
    } catch {
      // ignore storage errors
    }
  }
}
