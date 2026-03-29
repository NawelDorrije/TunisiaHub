import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { NotFoundComponent } from './features/not-found/not-found.component';
import { HomeComponent } from './core/home/home.component';

const routes: Routes = [
  { path: 'home', component: HomeComponent },
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  {
    path: 'campings',
    loadChildren: () =>
      import('./features/campings/campings.module').then(
        (m) => m.CampingsModule,
      ),
  },
  {
    path: 'carpooling',
    loadChildren: () =>
      import('./features/Carpooling/carpooling.module').then(
        (m) => m.CarpoolingModule,
      ),
  },
  {
    path: 'auth',
    loadChildren: () =>
      import('./features/auth/auth.module').then((m) => m.AuthModule),
  },
   {
  path: 'accommodations',
  loadChildren: () =>
    import('./features/accommodations/accommodations.module')
      .then(m => m.AccommodationsModule)
},

  {
      path: 'trendy-places',
      loadChildren: () =>
        import('./features/trendy-places/trendy-places.module')
          .then(m => m.TrendyPlacesModule)
    },
  {
    path: '**',
    component: NotFoundComponent,
  },
  
  

];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
