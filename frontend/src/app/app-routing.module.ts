import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { NotFoundComponent } from './features/not-found/not-found.component';
import { HomeComponent } from './core/home/home.component';
import { CartComponent } from './features/souvenirs-shops/cart/cart.component';
import { AuthGuard } from './features/auth/auth.guard';
import { OwnerGuard } from './features/auth/owner.guard';

export const routes: Routes = [
  { path: 'home', component: HomeComponent },
  { path: '', redirectTo: 'home', pathMatch: 'full' },

  // Existing features
  {
    path: 'campings',
    loadChildren: () =>
      import('./features/campings/campings.module').then((m) => m.CampingsModule),
  },
  {
    path: 'carpooling',
    loadChildren: () =>
      import('./features/Carpooling/carpooling.module').then((m) => m.CarpoolingModule),
  },
  {
    path: 'auth',
    loadChildren: () =>
      import('./features/auth/auth.module').then((m) => m.AuthModule),
  },

  // ==================== NEW: Souvenirs Shops ====================
  {
    path: 'shops',
    loadChildren: () =>
      import('./features/souvenirs-shops/shops/shops.module').then((m) => m.ShopsModule),
  },
  {
    path: 'products',
    loadChildren: () =>
      import('./features/souvenirs-shops/products/products.module').then((m) => m.ProductsModule),
  },
  {
    path: 'orders',
    loadChildren: () =>
      import('./features/souvenirs-shops/orders/orders.module').then((m) => m.OrdersModule),
  },
  {
    path: 'payments',
    loadChildren: () =>
      import('./features/souvenirs-shops/payments/payments.module').then((m) => m.PaymentsModule),
  },
  {
    path: 'reviews',
    loadChildren: () =>
      import('./features/souvenirs-shops/reviews/reviews.module').then((m) => m.ReviewsModule),
  },
  {
    path: 'cart',
    component: CartComponent,
  },
  {
    path: 'owner-dashboard',
    loadComponent: () => import('./features/souvenirs-shops/owner-dashboard/owner-dashboard.component')
      .then(m => m.OwnerDashboardComponent),
    canActivate: [OwnerGuard],
  },
  // {
  //   path: 'promotions',
  //   loadComponent: () =>
  //     import('./features/souvenirs-shops/promotions/components/promote/promote.component')
  //       .then((m) => m.PromoteComponent),
  //   canActivate: [OwnerGuard],
  // },

  {
    path: 'admin-dashboard',
    loadChildren: () =>
      import('./features/admin-dashboard/admin-dashboard.module')
        .then(m => m.AdminDashboardModule),
  },

  {
    path: 'accommodations',
    loadChildren: () =>
      import('./features/accommodations/accommodations.module')
        .then(m => m.AccommodationsModule),
},
{
  path: 'my-reservations',
  loadChildren: () =>
    import('./features/my-reservations/my-reservations.module')
      .then(m => m.MyReservationsModule),
  canActivate: [AuthGuard]
},
  {
    path: '**',
    component: NotFoundComponent,
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule { }
