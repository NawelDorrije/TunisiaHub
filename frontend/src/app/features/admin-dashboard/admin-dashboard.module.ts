import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminDashboardRoutingModule } from './admin-dashboard-routing.module';
import { AdminDashboardComponent } from './admin-dashboard.component';
import { OverviewComponent } from './pages/overview/overview.component';
import { UsersComponent } from './pages/users/users.component';
import { ShopsComponent } from './pages/shops/shops.component';
import { OrdersComponent } from './pages/orders/orders.component';
import { ReviewsComponent } from './pages/reviews/reviews.component';
import { InsightsComponent } from './pages/insights/insights.component';

@NgModule({
  declarations: [
    AdminDashboardComponent,
    OverviewComponent,
    UsersComponent,
    ShopsComponent,
    OrdersComponent,
    ReviewsComponent,
    InsightsComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    AdminDashboardRoutingModule
  ]
})
export class AdminDashboardModule { }
