import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [
   // FRONT OFFICE (client)
  {
    path: '',
    loadChildren: () =>
      import('./frontoffice/frontoffice.module')
        .then(m => m.FrontofficeModule)
  },

  // BACK OFFICE (admin + owner)
  {
    path: 'backoffice',
    loadChildren: () =>
      import('./backoffice/backoffice.module')
        .then(m => m.BackofficeModule)
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CampingRoutingModule { }
