import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { NotFoundComponent } from './features/not-found/not-found.component';
import { HomeComponent } from './core/home/home.component';
import { CartComponent } from './features/souvenirs-shops/cart/cart.component';

import { AuthGuard } from './features/auth/auth.guard';
import { OwnerGuard } from './features/auth/owner.guard';

export const routes: Routes = [

  // ==================== HOME ====================
  { path: 'home', component: HomeComponent },
  { path: '', redirectTo: 'home', pathMatch: 'full' },

  // ==================== AUTH ====================
  {
    path: 'auth',
    loadChildren: () =>
      import('./features/auth/auth.module').then(m => m.AuthModule),
  },

  // ==================== CAMPING ====================
  {
    path: 'camping',
    loadChildren: () =>
      import('./features/camping/camping.module').then(m => m.CampingModule),
  },

  

  // ==================== SOUVENIR SHOPS ====================
  {
    path: 'shops',
    loadChildren: () =>
      import('./features/souvenirs-shops/shops/shops.module').then(m => m.ShopsModule),
  },
  {
    path: 'products',
    loadChildren: () =>
      import('./features/souvenirs-shops/products/products.module').then(m => m.ProductsModule),
  },
  {
    path: 'orders',
    loadChildren: () =>
      import('./features/souvenirs-shops/orders/orders.module').then(m => m.OrdersModule),
  },
  {
    path: 'payments',
    loadChildren: () =>
      import('./features/souvenirs-shops/payments/payments.module').then(m => m.PaymentsModule),
  },
  {
    path: 'reviews',
    loadChildren: () =>
      import('./features/souvenirs-shops/reviews/reviews.module').then(m => m.ReviewsModule),
  },
  {
    path: 'cart',
    component: CartComponent,
  },
  {
    path: 'owner-dashboard',
    loadComponent: () =>
      import('./features/souvenirs-shops/owner-dashboard/owner-dashboard.component')
        .then(m => m.OwnerDashboardComponent),
    canActivate: [OwnerGuard],
  },

  // ==================== ADMIN ====================
  {
    path: 'admin-dashboard',
    loadChildren: () =>
      import('./features/admin-dashboard/admin-dashboard.module')
        .then(m => m.AdminDashboardModule),
  },

  // ==================== ACCOMMODATIONS ====================
  {
    path: 'accommodations',
    loadChildren: () =>
      import('./features/accommodations/accommodations.module')
        .then(m => m.AccommodationsModule),
  },

  // ==================== USER ====================
  {
    path: 'my-reservations',
    loadChildren: () =>
      import('./features/my-reservations/my-reservations.module')
        .then(m => m.MyReservationsModule),
    canActivate: [AuthGuard],
  },

  // ==================== TRENDY PLACES ====================
  {
    path: 'trendy-places',
    loadChildren: () =>
      import('./features/trendy-places/trendy-places.module')
        .then(m => m.TrendyPlacesModule),
  },

  // ==================== NOT FOUND ====================
  {
    path: '**',
    component: NotFoundComponent,
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }