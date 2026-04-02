import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { RestaurantsRoutingModule } from './restaurants-routing.module';
import { AdminRestaurantReservationsComponent } from './admin-restaurant-reservations/admin-restaurant-reservations.component';
import { RestaurantListComponent } from './restaurant-list/restaurant-list.component';
import { RestaurantsComponent } from './restaurants.component';


@NgModule({
  declarations: [
    RestaurantListComponent,
    RestaurantsComponent,
    AdminRestaurantReservationsComponent,
  ],
  imports: [
    CommonModule,
    RouterModule,
    RestaurantsRoutingModule,
    FormsModule
  ]
})
export class RestaurantsModule { }
