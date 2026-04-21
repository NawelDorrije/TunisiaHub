import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ListEventsComponent } from './components/list-events/list-events.component';
import { AddEventComponent } from './components/add-event/add-event.component';
import { EditEventComponent } from './components/edit-event/edit-event.component';
import { EventDetailsComponent } from './components/event-details/event-details.component';
import { ListEventsUserComponent } from './components/list-events-user/list-events-user.component';
import { ReservationEventComponent } from './components/reservation-event/reservation-event.component';

//import { CalendarEventsComponent } from './components/calendar-events/calendar-events.component';





const routes: Routes = [
  { path: '', component: ListEventsComponent },
  { path: 'add', component: AddEventComponent },
  { path: 'edit/:id', component: EditEventComponent },
  { path: 'details/:id', component: EventDetailsComponent },
  { path: 'user/events', component: ListEventsUserComponent },
  { path: 'reserve/:id', component: ReservationEventComponent },
  
 //{ path: 'calendar', component: CalendarEventsComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class EventsRoutingModule { }
