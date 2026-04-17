import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { OwnerRoutingModule } from './owner-routing.module';
import { DashboardComponent } from './dashboard/dashboard.component';
import { MyCampingsComponent } from './my-campings/my-campings.component';
import { CampingFormComponent } from './my-campings/camping-form/camping-form.component';
import { CampingDetailComponent } from './my-campings/camping-detail/camping-detail.component';
import { SpotManagementComponent } from './spot-management/spot-management.component';
import { SpotFormComponent } from './spot-management/spot-form/spot-form.component';
import { ActivityManagementComponent } from './activity-management/activity-management.component';
import { EquipmentManagementComponent } from './equipment-management/equipment-management.component';
import { MyReservationsComponent } from './my-reservations/my-reservations.component';
import { ReactiveFormsModule } from '@angular/forms';
import { OverviewComponent } from './overview/overview.component';
import { EquipmenntFormComponent } from './equipment-management/equipmennt-form/equipmennt-form.component';


@NgModule({
  declarations: [
    DashboardComponent,
    MyCampingsComponent,
    CampingFormComponent,
    CampingDetailComponent,
    SpotManagementComponent,
    SpotFormComponent,
    ActivityManagementComponent,
    EquipmentManagementComponent,
    MyReservationsComponent,
    OverviewComponent,
    EquipmenntFormComponent
  ],
  imports: [
    CommonModule,
    OwnerRoutingModule,
    ReactiveFormsModule
  ]
})
export class OwnerModule { }
