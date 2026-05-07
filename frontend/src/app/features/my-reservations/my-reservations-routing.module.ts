import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MyReservationsHubComponent } from './components/my-reservations-hub/my-reservations-hub.component';
import { MyAccommodationReservationsComponent } from './components/my-accommodation-reservations/my-accommodation-reservations.component';

const routes: Routes = [
  { path: '', component: MyReservationsHubComponent },
  { path: 'accommodations', component: MyAccommodationReservationsComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class MyReservationsRoutingModule { }