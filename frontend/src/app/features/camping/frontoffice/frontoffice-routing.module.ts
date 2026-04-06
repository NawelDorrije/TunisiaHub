import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ListCampingsComponent } from './list-campings/list-campings.component';
import { CampingMapComponent } from './camping-map/camping-map.component';
import { CampingDetailComponent } from './camping-detail/camping-detail.component';
import { ReservationFormComponent } from './reservation-form/reservation-form.component';
import { MyReservationsComponent } from './my-reservations/my-reservations.component';

const routes: Routes = [
  { path: '', component: ListCampingsComponent },
  { path: 'map', component: CampingMapComponent },
  { path: 'my-reservations', component: MyReservationsComponent },
  { path: ':id', component: CampingDetailComponent },
  { path: ':id/reserve', component: ReservationFormComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class FrontofficeRoutingModule { }
