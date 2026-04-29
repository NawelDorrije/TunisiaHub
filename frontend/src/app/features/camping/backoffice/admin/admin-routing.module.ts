import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './dashboard/dashboard.component';
import { CampingManagementComponent } from './camping-management/camping-management.component';
import { ReservationManagementComponent } from './reservation-management/reservation-management.component';
import { PaymentManagementComponent } from './payment-management/payment-management.component';

const routes: Routes = [
  {
    path: '',  component: DashboardComponent
  },
  { path: 'campings', component: CampingManagementComponent },
  { path: 'reservations', component: ReservationManagementComponent },
  { path: 'payments', component: PaymentManagementComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AdminRoutingModule { }
