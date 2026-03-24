import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { CampingsRoutingModule } from './campings-routing.module';
import { CampingsComponent } from './campings.component';
import { CampingListComponent } from './camping-list/camping-list.component';
import { ListSpotComponent } from './list-spot/list-spot.component';
import { CampingDetailsComponent } from './camping-details/camping-details.component';
import { FormCampingComponent } from './admin/form-camping/form-camping.component';
import { ListCampingComponent } from './admin/list-camping/list-camping.component';
import { ReactiveFormsModule } from '@angular/forms';

@NgModule({
  declarations: [
    CampingsComponent,
    CampingListComponent,
    ListSpotComponent,
    CampingDetailsComponent,
    FormCampingComponent,
    ListCampingComponent,
  ],
  imports: [
    CommonModule,
    CampingsRoutingModule,
    ReactiveFormsModule
  ]
})
export class CampingsModule { }
