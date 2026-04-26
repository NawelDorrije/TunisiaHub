import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { AdminGuard } from '../../auth/admin.guard';
import { OwnerGuard } from '../../auth/owner.guard';

const routes: Routes = [
  {
    path: 'admin',
    loadChildren: () =>
      import('./admin/admin.module').then(m => m.AdminModule),
    canActivate: [AdminGuard]   // ADMIN seulement
  },
  {
    path: 'owner',
    loadChildren: () =>
      import('./owner/owner.module').then(m => m.OwnerModule),
    canActivate: [OwnerGuard]   // OWNER (ou ADMIN) ✓
  }
];
@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule,
            ReactiveFormsModule
  ]
})
export class BackofficeRoutingModule { }
