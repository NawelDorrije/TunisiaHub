import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { CampingsRoutingModule } from './campings-routing.module';
import { CampingsComponent } from './campings.component';
import { CampingListComponent } from './camping-list/camping-list.component';
import { ListSpotComponent } from './list-spot/list-spot.component';
import { CampingDetailsComponent } from './camping-details/camping-details.component';

@NgModule({
  declarations: [
    CampingsComponent,
    CampingListComponent,
    ListSpotComponent,
    CampingDetailsComponent
  ],
  imports: [
    CommonModule,
    CampingsRoutingModule
  ]
})
export class CampingsModule { }
