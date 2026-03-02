import { Routes } from '@angular/router';
import { HomeComponent } from './features/home/home.component';
import { CarpoolingShellComponent } from './features/carpooling/carpooling-shell/carpooling-shell.component';
import { TripListComponent } from './features/carpooling/pages/trip-list/trip-list.component';
import { TripDetailsComponent } from './features/carpooling/pages/trip-details/trip-details.component';
import { TripCreateComponent } from './features/carpooling/pages/trip-create/trip-create.component';
import { MyTripsComponent } from './features/carpooling/pages/my-trips/my-trips.component';
import { driverRoleGuard } from './core/guards/driver-role.guard';
import { ComingSoonComponent } from './shared/components/coming-soon/coming-soon.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'home' },
  { path: 'home', component: HomeComponent },
  { path: 'places', component: ComingSoonComponent, data: { module: 'Places' } },
  { path: 'events', component: ComingSoonComponent, data: { module: 'Events' } },
  { path: 'restaurants', component: ComingSoonComponent, data: { module: 'Restaurants' } },
  { path: 'campings', component: ComingSoonComponent, data: { module: 'Campings' } },
  { path: 'stays', component: ComingSoonComponent, data: { module: 'Stays' } },
  { path: 'shops', component: ComingSoonComponent, data: { module: 'Shops' } },
  {
    path: 'carpooling',
    component: CarpoolingShellComponent,
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'trips' },
      { path: 'trips', component: TripListComponent },
      { path: 'trips/new', component: TripCreateComponent, canActivate: [driverRoleGuard] },
      { path: 'trips/:id', component: TripDetailsComponent },
      { path: 'my-trips', component: MyTripsComponent, canActivate: [driverRoleGuard] },
    ],
  },
  { path: '**', redirectTo: 'home' },
];
