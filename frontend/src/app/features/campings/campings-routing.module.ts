import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CampingsComponent } from './campings.component';
import { CampingListComponent } from './camping-list/camping-list.component';
import { CampingDetailsComponent } from './camping-details/camping-details.component';
import { ListCampingComponent } from './admin/list-camping/list-camping.component';
import { FormCampingComponent } from './admin/form-camping/form-camping.component';
import { ListSpotComponent } from './admin/list-spot/list-spot.component';
import { DetailsCampingComponent } from './admin/details-camping/details-camping.component';
import { ReservationComponent } from './admin/reservation/reservation.component';

const routes: Routes = [{ path: '', component: CampingListComponent },
  {path:'', component: CampingListComponent},
    { path: 'details/:id', component: CampingDetailsComponent },
    {
  path: 'admin',
  component: ListCampingComponent
},

{
  path: 'admin/add-camping',
  component: FormCampingComponent
},

{
  path: 'admin/edit-camping/:id',
  component: FormCampingComponent
},

{
  path: 'admin/spots/:campingId',
  component: ListSpotComponent
}
,

{
  path: 'admin/details/:id',
  component: DetailsCampingComponent
},
{
  path: 'admin/resrvation',
  component: ReservationComponent
}
];
@NgModule({

  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CampingsRoutingModule { }
