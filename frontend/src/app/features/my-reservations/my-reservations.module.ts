import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MyReservationsRoutingModule } from './my-reservations-routing.module';
import { MyReservationsHubComponent } from './components/my-reservations-hub/my-reservations-hub.component';
import { MyAccommodationReservationsComponent } from './components/my-accommodation-reservations/my-accommodation-reservations.component';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';

@NgModule({
  declarations: [
    MyReservationsHubComponent,
    MyAccommodationReservationsComponent
  ],
  imports: [
    CommonModule,
    MyReservationsRoutingModule,
    ReactiveFormsModule,
    FormsModule
  ]
})
export class MyReservationsModule { }