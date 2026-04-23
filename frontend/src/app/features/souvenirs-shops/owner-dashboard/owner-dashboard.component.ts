import { Component, OnInit } from '@angular/core';
import { ShopService } from '../../../services/souvenirs-shops/shop.service';
import { ProductService } from '../../../services/souvenirs-shops/product.service';
import { OrderService } from '../../../services/souvenirs-shops/order.service';
import { PaymentService } from '../../../services/souvenirs-shops/payment.service';
import { AdminReviewService, OwnerReviewInsights } from '../../admin-dashboard/services/admin-review.service';
import { AuthService } from '../../auth/services/auth.service';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-owner-dashboard',
  templateUrl: './owner-dashboard.component.html',
  styleUrls: ['./owner-dashboard.component.css']
})
export class OwnerDashboardComponent implements OnInit {
  isLoading = false;
  errorMessage = '';
  
  ownerName = '';
  shopStats = {
    totalShops: 0,
    totalProducts: 0,
    publishedProducts: 0,
    draftProducts: 0,
    totalRevenue: 0,
    totalOrders: 0
  };

  recentOrders: any[] = [];
  ownerShops: any[] = [];
  reviewInsights: OwnerReviewInsights | null = null;
  insightsLoading = false;
  insightsError = '';
  
  monthlyStats: { name: string, count: number, revenue: number }[] = [];
  seasonalStats: { name: string, count: number, revenue: number }[] = [];
  maxMonthlyCount = 0;
  maxSeasonalCount = 0;

  constructor(
    private shopService: ShopService,
    private productService: ProductService,
    private orderService: OrderService,
    private paymentService: PaymentService,
    private reviewService: AdminReviewService,
    private authService: AuthService,
    private router: Router
  ) {
    this.ownerName = `${authService.getPrenom()} ${authService.getNom()}`;
  }

  ngOnInit(): void {
    this.loadDashboardData();
    this.loadReviewInsights();
  }

  private loadReviewInsights(): void {
    this.insightsLoading = true;
    this.reviewService.getOwnerReviewInsights().subscribe({
      next: (insights: OwnerReviewInsights) => {
        this.reviewInsights = insights;
        this.insightsLoading = false;
      },
      error: (err: any) => {
        console.error('Error loading review insights:', err);
        this.insightsError = 'Unable to load review insights right now.';
        this.insightsLoading = false;
      }
    });
  }

  private loadDashboardData(): void {
    this.isLoading = true;
    
    const ownerId = this.authService.getUserId();
    if (!ownerId) {
      this.router.navigate(['/auth/sign-in']);
      return;
    }

    // Load shops by owner
    this.shopService.getShopsByOwner(ownerId).subscribe({
      next: (shops) => {
        this.ownerShops = shops;
        this.shopStats.totalShops = shops.length;

        // Calculate total products for all owner's shops
        let totalProducts = 0;
        shops.forEach(shop => {
          if (shop.products) {
            totalProducts += shop.products.length;
          }
        });
        this.shopStats.totalProducts = totalProducts;

        // Load orders for all owner's shops
        if (shops.length > 0) {
          this.loadOrdersAndRevenue(shops);
        } else {
          this.isLoading = false;
        }
      },
      error: (err) => {
        console.error('Error loading shops:', err);
        this.errorMessage = 'Failed to load shop data.';
        this.isLoading = false;
      }
    });
  }

  private loadOrdersAndRevenue(shops: any[]): void {
    const orderRequests = shops.map(shop => this.orderService.getOrdersByShop(shop.id));
    
    forkJoin(orderRequests).subscribe({
      next: (allOrders: any[]) => {
        let totalOrders = 0;
        let totalRevenue = 0;
        const allOrdersList: any[] = [];

        allOrders.forEach(ordersForShop => {
          if (Array.isArray(ordersForShop)) {
            totalOrders += ordersForShop.length;
            allOrdersList.push(...ordersForShop);
            
            // Calculate revenue from orders with successful payments
            ordersForShop.forEach((order: any) => {
              if (order.totalAmount) {
                totalRevenue += order.totalAmount;
              }
            });
          }
        });

        this.shopStats.totalOrders = totalOrders;
        this.shopStats.totalRevenue = totalRevenue;
        
        // Get recent orders (last 5)
        this.recentOrders = allOrdersList
          .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
          .slice(0, 5);

        this.computeOrderPatterns(allOrdersList);
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading orders:', err);
        // Don't fail the dashboard if orders fail to load, just continue
        this.isLoading = false;
      }
    });
  }

  navigateToShops(): void {
    this.router.navigate(['/shops']);
  }

  navigateToProducts(): void {
    this.router.navigate(['/products']);
  }

  navigateToCreateShop(): void {
    this.router.navigate(['/shops/new']);
  }

  navigateToCreateProduct(): void {
    this.router.navigate(['/products/new']);
  }

  navigateToEditShop(shopId: number): void {
    this.router.navigate(['/shops', shopId, 'edit']);
  }

  private computeOrderPatterns(orders: any[]): void {
    const monthNames = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
    const monthsData: { [key: string]: { count: number, revenue: number } } = {};
    const seasonsData: { [key: string]: { count: number, revenue: number } } = {
      'Winter': { count: 0, revenue: 0 },
      'Spring': { count: 0, revenue: 0 },
      'Summer': { count: 0, revenue: 0 },
      'Autumn': { count: 0, revenue: 0 }
    };

    orders.forEach(order => {
      const date = new Date(order.createdAt);
      const month = date.getMonth();
      const monthName = monthNames[month];
      const revenue = order.totalAmount || 0;

      if (!monthsData[monthName]) {
        monthsData[monthName] = { count: 0, revenue: 0 };
      }
      monthsData[monthName].count++;
      monthsData[monthName].revenue += revenue;

      // Season logic
      let season = '';
      if ([11, 0, 1].includes(month)) season = 'Winter';
      else if ([2, 3, 4].includes(month)) season = 'Spring';
      else if ([5, 6, 7].includes(month)) season = 'Summer';
      else season = 'Autumn';

      seasonsData[season].count++;
      seasonsData[season].revenue += revenue;
    });

    this.monthlyStats = Object.keys(monthsData).map(name => ({
      name,
      count: monthsData[name].count,
      revenue: monthsData[name].revenue
    })).sort((a, b) => monthNames.indexOf(a.name) - monthNames.indexOf(b.name));

    this.maxMonthlyCount = Math.max(...this.monthlyStats.map(s => s.count), 1);

    this.seasonalStats = Object.keys(seasonsData).map(name => ({
      name,
      count: seasonsData[name].count,
      revenue: seasonsData[name].revenue
    }));

    this.maxSeasonalCount = Math.max(...this.seasonalStats.map(s => s.count), 1);
  }

  scrollToStatistics(): void {
    const element = document.getElementById('order-patterns-stats');
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
    }
  }
}
