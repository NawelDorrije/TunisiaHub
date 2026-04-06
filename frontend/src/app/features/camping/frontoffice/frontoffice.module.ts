import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { FrontofficeRoutingModule } from './frontoffice-routing.module';
import { ListCampingsComponent } from './list-campings/list-campings.component';
import { CampingDetailComponent } from './camping-detail/camping-detail.component';
import { CampingMapComponent } from './camping-map/camping-map.component';
import { ReservationFormComponent } from './reservation-form/reservation-form.component';
import { MyReservationsComponent } from './my-reservations/my-reservations.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';


@NgModule({
  declarations: [
    ListCampingsComponent,
    CampingDetailComponent,
    CampingMapComponent,
    ReservationFormComponent,
    MyReservationsComponent
  ],
  imports: [
    CommonModule,
    FrontofficeRoutingModule
,
 ReactiveFormsModule,
  FormsModule,
    HttpClientModule  ]
})
export class FrontofficeModule { }
