import { NgModule, CUSTOM_ELEMENTS_SCHEMA,  } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';


import { EventsRoutingModule } from './events-routing.module';
import { AddEventComponent } from './components/add-event/add-event.component';
import { ListEventsComponent } from './components/list-events/list-events.component';

import { ReactiveFormsModule } from '@angular/forms';
import { EditEventComponent } from './components/edit-event/edit-event.component';
import { EventDetailsComponent } from './components/event-details/event-details.component';
import { ListEventsUserComponent } from './components/list-events-user/list-events-user.component';
import { ReservationEventComponent } from './components/reservation-event/reservation-event.component';
import { StripePaymentComponent } from './components/stripe-payment/stripe-payment.component';
import { FullCalendarModule } from '@fullcalendar/angular';
import { AiRecommendationsComponent } from './components/ai-recommendations/ai-recommendations.component';
//import { CalendarEventsComponent } from './components/calendar-events/calendar-events.component';


@NgModule({
  declarations: [
    AddEventComponent,
    ListEventsComponent,
    EditEventComponent,
    EventDetailsComponent,
    ListEventsUserComponent,
    ReservationEventComponent,
    StripePaymentComponent,
    AiRecommendationsComponent,
    
    //CalendarEventsComponent,
    
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    EventsRoutingModule,
    FullCalendarModule,
    RouterModule
  ],
  //schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class EventsModule { }
