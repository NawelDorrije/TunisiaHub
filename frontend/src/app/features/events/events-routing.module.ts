import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ListEventsComponent } from './components/list-events/list-events.component';
import { AddEventComponent } from './components/add-event/add-event.component';
import { EditEventComponent } from './components/edit-event/edit-event.component';
import { EventDetailsComponent } from './components/event-details/event-details.component';
import { ListEventsUserComponent } from './components/list-events-user/list-events-user.component';
import { ReservationEventComponent } from './components/reservation-event/reservation-event.component';
//import { ReviewEventComponent } from './components/review-event/review-event.component';




const routes: Routes = [
  { path: '', component: ListEventsComponent },
  { path: 'add', component: AddEventComponent },
  { path: 'edit/:id', component: EditEventComponent },
  { path: 'details/:id', component: EventDetailsComponent },
  { path: 'user/events', component: ListEventsUserComponent },
  { path: 'reserve/:id', component: ReservationEventComponent },
  {path: 'review/:reservationId',
    loadComponent: () =>
      import('./components/review-event/review-event.component')
        .then(m => m.ReviewEventComponent)}
  //{ path: 'review/:reservationId', component: ReviewEventComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class EventsRoutingModule { }
