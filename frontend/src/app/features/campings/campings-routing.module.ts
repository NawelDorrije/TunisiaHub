import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CampingsComponent } from './campings.component';

const routes: Routes = [{ path: '', component: CampingsComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CampingsRoutingModule { }
