import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Order } from '../../../../../models/souvenirs-shops/order.model';
import { OrderService } from '../../../../../services/souvenirs-shops/order.service';
import { AuthService } from '../../../../auth/services/auth.service';
import { OrderStatus } from '../../../../../models/souvenirs-shops/order-status.enum';

@Component({
  selector: 'app-order-list',
  templateUrl: './order-list.component.html',
  styleUrls: ['./order-list.component.css']
})
export class OrderListComponent implements OnInit {
  orders: Order[] = [];
  isLoading = true;
  errorMessage = '';
  actionMessage = '';

  activeViewTab: 'client' | 'owner' = 'client';
  activeStatusTab = 'ALL';
  searchTerm = '';
  selectedDateRange = 'ALL';
  customStartDate: string | null = null;
  customEndDate: string | null = null;
  selectedStatus = 'ALL';
  selectedShopOrCustomer = '';
  minTotal: number | null = null;
  maxTotal: number | null = null;
  sortBy = 'NEWEST';

  public OrderStatus = OrderStatus;

  constructor(
    private orderService: OrderService,
    public authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (this.authService.isOwner() || this.authService.isAdmin()) {
      this.activeViewTab = 'owner';
    } else {
      this.activeViewTab = 'client';
    }
    this.loadOrders();
  }

  loadOrders(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.actionMessage = '';

    this.orderService.getAllOrders().subscribe({
      next: (orders) => {
        this.orders = orders;
        this.isLoading = false;
      },
      error: (error) => {
        if (error.status === 401 || error.status === 403) {
          this.errorMessage = 'You must be logged in to view your orders.';
        } else {
          this.errorMessage = error.error?.message || 'Unable to load orders. Please try again later.';
        }
        this.isLoading = false;
      }
    });
  }

  get currentUserEmail(): string | null {
    return this.authService.getEmail()?.toLowerCase() ?? null;
  }

  get viewTabs(): Array<{ key: 'client' | 'owner'; label: string }> {
    const tabs: Array<{ key: 'client' | 'owner'; label: string }> = [];
    if (this.authService.isClient()) {
      tabs.push({ key: 'client', label: 'Client View' });
    }
    if (this.authService.isOwner() || this.authService.isAdmin()) {
      tabs.push({ key: 'owner', label: 'Owner/Seller View' });
    }
    return tabs;
  }

  get statusTabs(): Array<{ key: string; label: string }> {
    if (this.activeViewTab === 'owner') {
      return [
        { key: 'ALL', label: 'All orders' },
        { key: 'PENDING', label: 'Pending' },
        { key: 'PROCESSING', label: 'Processing' },
        { key: 'NEEDS_ACTION', label: 'Needs action' },
        { key: 'HISTORY', label: 'History' }
      ];
    }
    return [
      { key: 'ALL', label: 'All' },
      { key: 'PENDING', label: 'Pending' },
      { key: 'PROCESSING', label: 'Processing' },
      { key: 'COMPLETED', label: 'Completed' },
      { key: 'CANCELLED', label: 'Canceled' }
    ];
  }

  get statusOptions(): string[] {
    return ['ALL', ...Object.values(OrderStatus)];
  }

  get availableShopNames(): string[] {
    return Array.from(new Set(this.orders
      .map((order) => order.shop?.name)
      .filter((name): name is string => !!name)
    ));
  }

  get availableCustomerNames(): string[] {
    return Array.from(new Set(this.orders
      .map((order) => this.getCustomerName(order))
      .filter((name) => !!name)
    ));
  }

  get filteredOrders(): Order[] {
    return this.orders
      .filter((order) => this.filterByView(order))
      .filter((order) => this.filterByStatusTab(order))
      .filter((order) => this.filterBySearch(order))
      .filter((order) => this.filterBySelectedStatus(order))
      .filter((order) => this.filterByShopOrCustomer(order))
      .filter((order) => this.filterByDate(order))
      .filter((order) => this.filterByPrice(order))
      .sort((a, b) => this.sortOrders(a, b));
  }

  private filterByView(order: Order): boolean {
    if (this.activeViewTab === 'client') {
      if (!this.currentUserEmail) {
        return true;
      }
      const orderEmail = order.user?.email?.toLowerCase();
      return orderEmail ? orderEmail === this.currentUserEmail : true;
    }
    if (this.activeViewTab === 'owner') {
      // backend already returns only owner/shop orders for owners,
      // and all orders for admins.
      return true;
    }
    return true;
  }

  private filterByStatusTab(order: Order): boolean {
    if (this.activeStatusTab === 'ALL') {
      return true;
    }
    if (this.activeStatusTab === 'NEEDS_ACTION') {
      return [OrderStatus.PENDING, OrderStatus.PAID].includes(order.status);
    }
    if (this.activeStatusTab === 'HISTORY') {
      return [OrderStatus.COMPLETED, OrderStatus.CANCELLED].includes(order.status);
    }
    return order.status === this.activeStatusTab;
  }

  private filterBySearch(order: Order): boolean {
    const term = this.searchTerm.trim().toLowerCase();
    if (!term) {
      return true;
    }
    const shopName = order.shop?.name?.toLowerCase() ?? '';
    const customerName = this.getCustomerName(order).toLowerCase();
    const orderDate = order.createdAt?.toLowerCase() ?? '';
    const productNames = (order.orderItems ?? [])
      .map((item) => item.product?.name?.toLowerCase() ?? '')
      .join(' ');
    return shopName.includes(term)
      || customerName.includes(term)
      || orderDate.includes(term)
      || productNames.includes(term)
      || (order.id?.toString().includes(term) ?? false);
  }

  private filterBySelectedStatus(order: Order): boolean {
    if (this.selectedStatus === 'ALL' || !this.selectedStatus) {
      return true;
    }
    return order.status === this.selectedStatus;
  }

  private filterByShopOrCustomer(order: Order): boolean {
    if (!this.selectedShopOrCustomer) {
      return true;
    }
    const value = this.selectedShopOrCustomer.toLowerCase();
    if (this.activeViewTab === 'client') {
      return order.shop?.name?.toLowerCase().includes(value) ?? false;
    }
    return this.getCustomerName(order).toLowerCase().includes(value);
  }

  private filterByDate(order: Order): boolean {
    if (this.selectedDateRange === 'ALL') {
      return true;
    }
    const orderDate = order.createdAt ? new Date(order.createdAt) : null;
    if (!orderDate) {
      return false;
    }
    const now = new Date();
    if (this.selectedDateRange === 'LAST_30_DAYS') {
      const past = new Date(now);
      past.setDate(now.getDate() - 30);
      return orderDate >= past && orderDate <= now;
    }
    if (this.selectedDateRange === 'THIS_YEAR') {
      return orderDate.getFullYear() === now.getFullYear();
    }
    if (this.selectedDateRange === 'CUSTOM' && this.customStartDate && this.customEndDate) {
      const start = new Date(this.customStartDate);
      const end = new Date(this.customEndDate);
      end.setHours(23, 59, 59, 999);
      return orderDate >= start && orderDate <= end;
    }
    return true;
  }

  private filterByPrice(order: Order): boolean {
    if (this.minTotal !== null && order.totalAmount < this.minTotal) {
      return false;
    }
    if (this.maxTotal !== null && order.totalAmount > this.maxTotal) {
      return false;
    }
    return true;
  }

  private sortOrders(a: Order, b: Order): number {
    switch (this.sortBy) {
      case 'OLDEST':
        return new Date(a.createdAt || '').getTime() - new Date(b.createdAt || '').getTime();
      case 'TOTAL_ASC':
        return a.totalAmount - b.totalAmount;
      case 'TOTAL_DESC':
        return b.totalAmount - a.totalAmount;
      case 'STATUS':
        return a.status.localeCompare(b.status);
      default:
        return new Date(b.createdAt || '').getTime() - new Date(a.createdAt || '').getTime();
    }
  }

  getCustomerName(order: Order): string {
    if (order.user?.prenom || order.user?.nom) {
      return `${order.user?.prenom ?? ''} ${order.user?.nom ?? ''}`.trim();
    }
    return order.user?.email ?? 'Unknown Customer';
  }

  setActiveView(view: 'client' | 'owner'): void {
    this.activeViewTab = view;
    this.activeStatusTab = 'ALL';
    this.selectedShopOrCustomer = '';
    this.selectedStatus = 'ALL';
    this.searchTerm = '';
    this.selectedDateRange = 'ALL';
    this.customStartDate = null;
    this.customEndDate = null;
    this.minTotal = null;
    this.maxTotal = null;
    this.sortBy = 'NEWEST';
  }

  setActiveStatusTab(tab: string): void {
    this.activeStatusTab = tab;
  }

  resetFilters(): void {
    this.searchTerm = '';
    this.selectedStatus = 'ALL';
    this.selectedShopOrCustomer = '';
    this.selectedDateRange = 'ALL';
    this.customStartDate = null;
    this.customEndDate = null;
    this.minTotal = null;
    this.maxTotal = null;
    this.sortBy = 'NEWEST';
  }

  viewOrder(order: Order): void {
    if (!order.id) {
      return;
    }
    this.router.navigate(['/orders', order.id]);
  }

  cancelPendingOrder(order: Order): void {
    if (!order.id) {
      return;
    }

    this.orderService.deleteOrder(order.id).subscribe({
      next: () => {
        this.actionMessage = 'Order has been cancelled.';
        this.loadOrders();
      },
      error: () => {
        this.errorMessage = 'Unable to cancel the order. Please try again.';
      }
    });
  }

  cancelOwnerOrder(order: Order): void {
    if (!order.id) {
      return;
    }

    this.orderService.updateOrderStatus(order.id, OrderStatus.CANCELLED).subscribe({
      next: () => {
        this.actionMessage = 'Order has been cancelled.';
        this.loadOrders();
      },
      error: () => {
        this.errorMessage = 'Unable to cancel the order. Please try again.';
      }
    });
  }

  updateOrderStatus(order: Order, status: OrderStatus, label: string): void {
    if (!order.id) {
      return;
    }

    this.orderService.updateOrderStatus(order.id, status).subscribe({
      next: () => {
        this.actionMessage = `Order status changed to ${label}.`;
        this.loadOrders();
      },
      error: () => {
        this.errorMessage = 'Unable to update the order status. Please try again.';
      }
    });
  }

  canCancelPending(order: Order): boolean {
    return this.authService.isClient() && order.status === OrderStatus.PENDING;
  }

  canProcessOrder(order: Order): boolean {
    return (this.authService.isOwner() || this.authService.isAdmin()) && order.status === OrderStatus.PAID;
  }

  canCompleteOrder(order: Order): boolean {
    return (this.authService.isOwner() || this.authService.isAdmin()) && order.status === OrderStatus.PROCESSING;
  }

  canOwnerCancel(order: Order): boolean {
    return (this.authService.isOwner() || this.authService.isAdmin()) &&
      [OrderStatus.PENDING, OrderStatus.PAID, OrderStatus.PROCESSING].includes(order.status);
  }
  getStatusClass(status: OrderStatus): string {
  const map: Record<string, string> = {
    PENDING:    'status-pending',
    PAID:       'status-paid',
    PROCESSING: 'status-processing',
    COMPLETED:  'status-completed',
    CANCELLED:  'status-cancelled'
  };
  return map[status] || 'status-default';
}

getPaymentClass(status: string | undefined): string {
  const map: Record<string, string> = {
    PENDING:  'payment-pending',
    SUCCESS:  'payment-success',
    FAILED:   'payment-failed',
    REFUNDED: 'payment-refunded'
  };
  return status ? (map[status] || 'payment-none') : 'payment-none';
}
}



