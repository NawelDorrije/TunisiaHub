import { Component, OnInit } from '@angular/core';
import { AdminUserService } from '../../services/admin-user.service';
import { AdminShopService } from '../../services/admin-shop.service';
import { AdminOrderService } from '../../services/admin-order.service';
import { AdminReviewService } from '../../services/admin-review.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-admin-overview',
  templateUrl: './overview.component.html',
  styleUrls: ['./overview.component.css']
})
export class OverviewComponent implements OnInit {
  stats = {
    totalUsers: 0,
    totalShops: 0,
    totalOrders: 0,
    totalReviews: 0
  };

  isLoading = true;
  error: string | null = null;

  constructor(
    private userService: AdminUserService,
    private shopService: AdminShopService,
    private orderService: AdminOrderService,
    private reviewService: AdminReviewService
  ) {}

  ngOnInit(): void {
    this.loadStats();
  }

  loadStats(): void {
    this.isLoading = true;
    this.error = null;

    forkJoin({
      users: this.userService.getAllUsers(),
      shops: this.shopService.getAllShops(),
      orders: this.orderService.getAllOrders(),
      reviews: this.reviewService.getAllReviews()
    }).subscribe({
      next: (data) => {
        this.stats.totalUsers = data.users.length;
        this.stats.totalShops = data.shops.length;
        this.stats.totalOrders = data.orders.length;
        this.stats.totalReviews = data.reviews.length;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading stats:', err);
        this.error = 'Failed to load dashboard statistics. Please try again.';
        this.isLoading = false;
      }
    });
  }
}
