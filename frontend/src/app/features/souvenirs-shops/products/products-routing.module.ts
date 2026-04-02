import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ProductListComponent } from './pages/product-list/product-list.component';
import { ProductDetailComponent } from './pages/product-detail/product-detail.component';
import { ProductFormComponent } from './pages/product-form/product-form.component';
import { AuthGuard } from '../../../features/auth/auth.guard';

const routes: Routes = [
  { path: 'shop/:shopId/new', component: ProductFormComponent, canActivate: [AuthGuard] },
  { path: 'shop', component: ProductListComponent, canActivate: [AuthGuard] },
  { path: 'shop/:shopId', component: ProductListComponent, canActivate: [AuthGuard] },
  { path: 'new', component: ProductFormComponent, canActivate: [AuthGuard] },
  { path: ':id/edit', component: ProductFormComponent, canActivate: [AuthGuard] },
  { path: ':id', component: ProductDetailComponent, canActivate: [AuthGuard] },
  { path: '', component: ProductListComponent, canActivate: [AuthGuard] }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ProductsRoutingModule { }