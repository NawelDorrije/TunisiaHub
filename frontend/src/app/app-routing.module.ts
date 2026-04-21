import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { NotFoundComponent } from './features/not-found/not-found.component';
import { HomeComponent } from './core/home/home.component';
import { AuthGuard } from './features/auth/auth.guard';
//import { CalendarEventsComponent } from './features/events/components/calendar-events/calendar-events.component';
import { FullCalendarModule } from '@fullcalendar/angular';

const routes: Routes = [
  { path: 'home', component: HomeComponent },
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  //{ path: 'calendar', component: CalendarEventsComponent },
  {
    path: 'events',
    loadChildren: () =>
      import('./features/events/events.module').then(m => m.EventsModule)
  },
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
        .then(m => m.AccommodationsModule),
  },
  {
    path: '**',
    component: NotFoundComponent,
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes), FullCalendarModule],
  exports: [RouterModule],
})
export class AppRoutingModule {}
