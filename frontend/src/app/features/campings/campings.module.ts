import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { CampingsRoutingModule } from './campings-routing.module';
import { CampingsComponent } from './campings.component';
import { CampingListComponent } from './camping-list/camping-list.component';

@NgModule({
  declarations: [
    CampingsComponent,
    CampingListComponent
  ],
  imports: [
    CommonModule,
    CampingsRoutingModule
  ]
})
export class CampingsModule { }
