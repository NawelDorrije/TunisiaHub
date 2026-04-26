import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { NotFoundComponent } from './features/not-found/not-found.component';
import { HomeComponent } from './core/home/home.component';
import { AuthGuard } from './features/auth/auth.guard';
import { CheckInComponent } from './features/restaurants/admin/check-in/check-in.component';

const routes: Routes = [
  { path: 'home', component: HomeComponent },
  { path: 'checkin', component: CheckInComponent },
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  {
    path: 'campings',
    loadChildren: () =>
      import('./features/campings/campings.module').then(
        (m) => m.CampingsModule,
      ),
  },
  {
    path: 'carpooling',
    loadChildren: () =>
      import('./features/Carpooling/carpooling.module').then(
        (m) => m.CarpoolingModule,
      ),
  },
  {
    path: 'auth',
    loadChildren: () =>
      import('./features/auth/auth.module').then((m) => m.AuthModule),
  },
  {
    path: 'restaurants',
    loadChildren: () =>
      import('./features/restaurants/restaurants.module').then(
        (m) => m.RestaurantsModule,
      ),
  },
  {
    path: 'menus',
    loadChildren: () =>
      import('./features/menus/menus.module').then(
        (m) => m.MenusModule,
      ),
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
