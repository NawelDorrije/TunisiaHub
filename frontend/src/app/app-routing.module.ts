import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { NotFoundComponent } from './features/not-found/not-found.component';
import { HomeComponent } from './core/home/home.component';

const routes: Routes = [
 { path : 'home', component: HomeComponent},
  { path : '', redirectTo: 'home', pathMatch: 'full'},

    { path: 'auth', loadChildren: () => import('./features/auth/auth.module').then(m => m.AuthModule) },
  {
    path: 'camping',
    loadChildren: () =>
      import('./features/camping/camping.module')
        .then(m => m.CampingModule)
  },
   
  {
    path: 'auth',
    loadChildren: () =>
      import('./features/auth/auth.module').then((m) => m.AuthModule),
  },
  {
    path: '**',
    component: NotFoundComponent
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
