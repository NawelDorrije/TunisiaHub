import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Order } from '../../../../../models/souvenirs-shops/order.model';
import { OrderItem } from '../../../../../models/souvenirs-shops/order-item.model';
import { Payment } from '../../../../../models/souvenirs-shops/payment.model';
import { PaymentMethod } from '../../../../../models/souvenirs-shops/payment-method.enum';
import { PaymentStatus } from '../../../../../models/souvenirs-shops/payment-status.enum';
import { OrderService } from '../../../../../services/souvenirs-shops/order.service';
import { PaymentService } from '../../../../../services/souvenirs-shops/payment.service';
import { AuthService } from '../../../../auth/services/auth.service';
import { OrderStatus } from '../../../../../models/souvenirs-shops/order-status.enum';

@Component({
  selector: 'app-order-detail',
  templateUrl: './order-detail.component.html',
  styleUrls: ['./order-detail.component.css']
})
export class OrderDetailComponent implements OnInit {
  order: Order | null = null;
  items: OrderItem[] = [];
  paymentHistory: Payment[] = [];
  public OrderStatus = OrderStatus;
  selectedMethod = PaymentMethod.CARD;
  paymentMethods = PaymentMethod;
  isLoading = true;
  errorMessage = '';
  actionMessage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private orderService: OrderService,
    private paymentService: PaymentService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    const orderId = Number(this.route.snapshot.paramMap.get('id'));
    if (!orderId || isNaN(orderId)) {
      this.errorMessage = 'Invalid order id.';
      this.isLoading = false;
      return;
    }
    this.loadOrder(orderId);
  }

  loadOrder(orderId: number): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.actionMessage = '';

    this.orderService.getOrderById(orderId).subscribe({
      next: (order) => {
        this.order = order;
        this.loadOrderDetails(orderId);
      },
      error: () => {
        this.errorMessage = 'Unable to load the order details. Please try again later.';
        this.isLoading = false;
      }
    });
  }

  private loadOrderDetails(orderId: number): void {
    forkJoin({
      items: this.orderService.getOrderItems(orderId).pipe(catchError(() => of([]))),
      payments: this.orderService.getOrderPayments(orderId).pipe(catchError(() => of([])))
    }).subscribe({
      next: ({ items, payments }) => {
        this.items = items;
        this.paymentHistory = payments.sort((a, b) => {
          if (!a.createdAt || !b.createdAt) {
            return 0;
          }
          return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
        });
        this.isLoading = false;
      },
      error: () => {
        this.items = [];
        this.paymentHistory = [];
        this.isLoading = false;
      }
    });
  }

  get latestPayment(): Payment | null {
    return this.paymentHistory.length > 0 ? this.paymentHistory[0] : null;
  }

  backToOrders(): void {
    this.router.navigate(['/orders']);
  }

  updateOrderStatus(status: OrderStatus, label: string): void {
    if (!this.order?.id) {
      return;
    }

    // For owners/admins, ask if they want to generate AI message
    const generateAiMessage = (this.authService.isOwner() || this.authService.isAdmin()) &&
      confirm('Generate AI status message for customer?');

    this.orderService.updateOrderStatus(this.order.id, {
      status: status,
      generateAiMessage: generateAiMessage
    }).subscribe({
      next: () => {
        this.actionMessage = `Order status updated to ${label}.`;
        this.loadOrder(this.order!.id!);
      },
      error: () => {
        this.errorMessage = 'Unable to update order status. Please try again.';
      }
    });
  }

  cancelOrder(): void {
    if (!this.order?.id) {
      return;
    }

    // Clients can cancel before completion
    if (this.authService.isClient() && this.order.status !== OrderStatus.COMPLETED && this.order.status !== OrderStatus.CANCELLED) {
      if (confirm('Are you sure you want to cancel this order?')) {
        this.orderService.deleteOrder(this.order.id).subscribe({
          next: () => this.router.navigate(['/orders']),
          error: () => {
            this.errorMessage = 'Unable to cancel the order. Please try again.';
          }
        });
      }
      return;
    }

    if ((this.authService.isOwner() || this.authService.isAdmin()) &&
      [OrderStatus.PENDING, OrderStatus.PAID, OrderStatus.PROCESSING].includes(this.order.status)) {
      this.updateOrderStatus(OrderStatus.CANCELLED, 'CANCELLED');
      return;
    }

    this.errorMessage = 'Cancellation is not permitted for this order.';
  }

  get canOwnerCancel(): boolean {
    return (this.authService.isOwner() || this.authService.isAdmin()) &&
      this.order !== null && [OrderStatus.PENDING, OrderStatus.PAID, OrderStatus.PROCESSING].includes(this.order.status);
  }

  get ownerCancelLabel(): string {
    return this.order?.status === OrderStatus.PENDING ? 'Cancel order' : 'Cancel & Refund';
  }

  get canClientCancel(): boolean {
    return this.authService.isClient() &&
      this.order !== null &&
      this.order.status !== OrderStatus.COMPLETED &&
      this.order.status !== OrderStatus.CANCELLED;
  }

  getAiMessage(): string {
    if (!this.order) return '';

    const productName = this.items[0]?.product?.name || 'your order';
    const shopName = this.order.shop?.name || 'our shop';

    switch (this.order.status) {
      case OrderStatus.PROCESSING:
        return `Great news! Your ${productName} from ${shopName} is being carefully prepared by our artisans. Expected delivery in 2-3 days.`;
      case OrderStatus.COMPLETED:
        return `🎉 Your order is complete! Your ${productName} from ${shopName} is ready for delivery. Expected delivery in 2-3 days.`;
      default:
        return '';
    }
  }

  get showAiMessage(): boolean {
    return this.authService.isClient() &&
      this.order !== null &&
      [OrderStatus.PROCESSING, OrderStatus.COMPLETED].includes(this.order.status);
  }

  get isOrderRefunded(): boolean {
    return !!this.paymentHistory.find((payment) => payment.status === PaymentStatus.REFUNDED);
  }

  payOrder(): void {
    if (!this.order?.id || !this.canPayOrder) {
      return;
    }

    const request = {
      orderId: this.order.id,
      method: this.selectedMethod,
      transactionReference: `TX-${Date.now()}`,
      simulateFailure: this.selectedMethod === PaymentMethod.CARD ? false : undefined
    };

    this.paymentService.addPayment(request).subscribe({
      next: () => {
        this.actionMessage = 'Payment attempt submitted.';
        this.loadOrder(this.order!.id!);
      },
      error: () => {
        this.errorMessage = 'Unable to complete payment. Please try again.';
      }
    });
  }

  retryPayment(): void {
    if (!this.order?.id || !this.latestPayment) {
      return;
    }

    const request = {
      orderId: this.order.id,
      method: this.latestPayment.method,
      transactionReference: `retry-${Date.now()}`,
      simulateFailure: this.latestPayment.method === PaymentMethod.CARD ? false : undefined
    };

    this.paymentService.addPayment(request).subscribe({
      next: () => {
        this.actionMessage = 'Payment retry submitted.';
        this.loadOrder(this.order!.id!);
      },
      error: () => {
        this.errorMessage = 'Unable to retry payment. Please try again.';
      }
    });
  }

  get canPayOrder(): boolean {
    return this.authService.isClient() && !!this.order && this.order.status === OrderStatus.PENDING;
  }

  get canRetryPayment(): boolean {
    return this.authService.isClient() && !!this.latestPayment && this.latestPayment.status === PaymentStatus.FAILED && this.order?.status === OrderStatus.PENDING;
  }

  get canProcessOrder(): boolean {
    return (this.authService.isOwner() || this.authService.isAdmin()) && this.order?.status === OrderStatus.PAID;
  }

  get canCompleteOrder(): boolean {
    return (this.authService.isOwner() || this.authService.isAdmin()) && this.order?.status === OrderStatus.PROCESSING;
  }
  getStatusClass(status: OrderStatus): string {
  const map: Record<string, string> = {
    PENDING:    'status-pending',
    PAID:       'status-paid',
    PROCESSING: 'status-processing',
    COMPLETED:  'status-completed',
    CANCELLED:  'status-cancelled'
  };
  return map[status] || '';
}

getPaymentClass(status: string | undefined): string {
  const map: Record<string, string> = {
    PENDING:  'payment-pending',
    SUCCESS:  'payment-success',
    FAILED:   'payment-failed',
    REFUNDED: 'payment-refunded'
  };
  return status ? (map[status] || '') : '';
}
}



