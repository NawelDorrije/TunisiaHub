import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './dashboard/dashboard.component';
import { MyCampingsComponent } from './my-campings/my-campings.component';
import { CampingFormComponent } from './my-campings/camping-form/camping-form.component';
import { SpotManagementComponent } from './spot-management/spot-management.component';
import { SpotFormComponent } from './spot-management/spot-form/spot-form.component';
import { ActivityManagementComponent } from './activity-management/activity-management.component';
import { EquipmentManagementComponent } from './equipment-management/equipment-management.component';
import { MyReservationsComponent } from './my-reservations/my-reservations.component';

const routes: Routes = [
  {
    path: '',
    component: DashboardComponent
  },
  { path: '', component: MyCampingsComponent },
  { path: 'new', component: CampingFormComponent },
  { path: ':id/edit', component: CampingFormComponent },
  { path: ':campingId/spots', component: SpotManagementComponent },
  { path: ':campingId/spots/new', component: SpotFormComponent },
  { path: ':campingId/spots/:spotId/edit', component: SpotFormComponent },
  { path: ':campingId/activities', component: ActivityManagementComponent },
  { path: ':campingId/equipment', component: EquipmentManagementComponent },
  { path: 'reservations', component: MyReservationsComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class OwnerRoutingModule { }
