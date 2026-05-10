import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AdminOrderService, Order } from '../../services/admin-order.service';

@Component({
  selector: 'app-admin-orders',
  templateUrl: './orders.component.html',
  styleUrls: ['./orders.component.css']
})
export class OrdersComponent implements OnInit {
  orders: Order[] = [];
  isLoading = false;
  error: string | null = null;

  constructor(private orderService: AdminOrderService, private router: Router) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.isLoading = true;
    this.error = null;

    this.orderService.getAllOrders().subscribe({
      next: (data) => {
        this.orders = data;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading orders:', err);
        this.error = 'Failed to load orders. Please try again.';
        this.isLoading = false;
      }
    });
  }

  calculateTotal(order: Order): number {
    if (order.items && order.items.length > 0) {
      return order.items.reduce((total, item) => total + (item.price * item.quantity), 0);
    }
    return order.totalAmount || 0;
  }

  viewDetails(id: number): void {
    // Navigate to order details page
    this.router.navigate(['/orders', id]);
  }

  updateStatus(id: number, newStatus: string): void {
    this.orderService.updateOrderStatus(id, newStatus).subscribe({
      next: (updatedOrder) => {
        const index = this.orders.findIndex(o => o.id === id);
        if (index !== -1) {
          this.orders[index] = updatedOrder;
        }
      },
      error: (err) => {
        console.error('Error updating order status:', err);
        alert('Failed to update order status: ' + (err.error?.message || 'Unknown error'));
      }
    });
  }
}
