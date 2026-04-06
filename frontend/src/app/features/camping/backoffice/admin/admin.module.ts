import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AdminRoutingModule } from './admin-routing.module';
import { DashboardComponent } from './dashboard/dashboard.component';
import { CampingManagementComponent } from './camping-management/camping-management.component';
import { ReservationManagementComponent } from './reservation-management/reservation-management.component';
import { PaymentManagementComponent } from './payment-management/payment-management.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';


@NgModule({
  declarations: [
    DashboardComponent,
    CampingManagementComponent,
    ReservationManagementComponent,
    PaymentManagementComponent
  ],
  imports: [
    CommonModule, FormsModule,
    ReactiveFormsModule,
    AdminRoutingModule
  ]
})
export class AdminModule { }
