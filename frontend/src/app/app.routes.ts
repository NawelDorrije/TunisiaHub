import { Routes } from '@angular/router';
import { HomeComponent } from './features/home/home.component';
import { TripListComponent } from './features/carpooling/pages/trip-list/trip-list.component';
import { TripDetailsComponent } from './features/carpooling/pages/trip-details/trip-details.component';
import { TripCreateComponent } from './features/carpooling/pages/trip-create/trip-create.component';
import { MyTripsComponent } from './features/carpooling/pages/my-trips/my-trips.component';
import { driverRoleGuard } from './core/guards/driver-role.guard';

export const routes: Routes = [
  { path: '', component: HomeComponent, pathMatch: 'full' },
  { path: 'carpooling/trips', component: TripListComponent },
  { path: 'carpooling/trips/new', component: TripCreateComponent, canActivate: [driverRoleGuard] },
  { path: 'carpooling/trips/:id', component: TripDetailsComponent },
  { path: 'carpooling/my-trips', component: MyTripsComponent, canActivate: [driverRoleGuard] },
  { path: '**', redirectTo: '' },
];
