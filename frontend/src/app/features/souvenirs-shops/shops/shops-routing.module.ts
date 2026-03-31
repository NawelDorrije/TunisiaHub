import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ShopListComponent } from './pages/shop-list/shop-list.component';
import { ShopDetailComponent } from './pages/shop-detail/shop-detail.component';
import { ShopFormComponent } from './pages/shop-form/shop-form.component';

const routes: Routes = [
  { path: '', component: ShopListComponent },
  { path: 'new', component: ShopFormComponent },
  { path: ':id', component: ShopDetailComponent },
  { path: ':id/edit', component: ShopFormComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ShopsRoutingModule { }