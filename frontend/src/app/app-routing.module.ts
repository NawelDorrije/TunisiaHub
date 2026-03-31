import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { NotFoundComponent } from './features/not-found/not-found.component';
import { HomeComponent } from './core/home/home.component';
import { CartComponent } from './features/souvenirs-shops/cart/cart.component';
import { AuthGuard } from './features/auth/auth.guard';

const routes: Routes = [
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
   path: 'accommodations',
    loadChildren: () =>
      import('./features/accommodations/accommodations.module')
        .then(m => m.AccommodationsModule),
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
export class AppRoutingModule {}