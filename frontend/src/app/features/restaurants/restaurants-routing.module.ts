import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AdminGuard } from '../auth/admin.guard';
import { AdminRestaurantReservationsComponent } from './admin-restaurant-reservations/admin-restaurant-reservations.component';
import { RestaurantsComponent } from './restaurants.component';

const routes: Routes = [
  { path: '', component: RestaurantsComponent },
  {
    path: 'reservations',
    component: AdminRestaurantReservationsComponent,
    canActivate: [AdminGuard],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class RestaurantsRoutingModule { }
