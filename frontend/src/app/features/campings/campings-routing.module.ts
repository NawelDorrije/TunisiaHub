import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CampingsComponent } from './campings.component';
import { CampingListComponent } from './camping-list/camping-list.component';
import { CampingDetailsComponent } from './camping-details/camping-details.component';

const routes: Routes = [{ path: '', component: CampingsComponent },
  {path:'list', component: CampingListComponent},
    { path: 'details/:id', component: CampingDetailsComponent }

];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CampingsRoutingModule { }
