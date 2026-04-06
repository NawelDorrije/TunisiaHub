import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'admin',
    loadChildren: () =>
      import('./admin/admin.module').then(m => m.AdminModule)
  },
  {
    path: 'owner',
    loadChildren: () =>
      import('./owner/owner.module').then(m => m.OwnerModule)
  }
];
@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule,
            ReactiveFormsModule
  ]
})
export class BackofficeRoutingModule { }
