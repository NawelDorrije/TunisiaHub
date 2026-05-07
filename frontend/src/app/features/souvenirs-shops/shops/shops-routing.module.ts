import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ShopListComponent } from './pages/shop-list/shop-list.component';
import { ShopDetailComponent } from './pages/shop-detail/shop-detail.component';
import { ShopFormComponent } from './pages/shop-form/shop-form.component';
import { AuthGuard } from '../../../features/auth/auth.guard';

const routes: Routes = [
  { path: '', component: ShopListComponent, canActivate: [AuthGuard] },
  { path: 'new', component: ShopFormComponent, canActivate: [AuthGuard] },
  { path: ':id', component: ShopDetailComponent, canActivate: [AuthGuard] },
  { path: ':id/edit', component: ShopFormComponent, canActivate: [AuthGuard] }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ShopsRoutingModule { }