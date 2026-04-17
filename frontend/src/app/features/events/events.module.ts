import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';


import { EventsRoutingModule } from './events-routing.module';
import { AddEventComponent } from './components/add-event/add-event.component';
import { ListEventsComponent } from './components/list-events/list-events.component';

import { ReactiveFormsModule } from '@angular/forms';
import { EditEventComponent } from './components/edit-event/edit-event.component';
import { EventDetailsComponent } from './components/event-details/event-details.component';
import { ListEventsUserComponent } from './components/list-events-user/list-events-user.component';
import { ReservationEventComponent } from './components/reservation-event/reservation-event.component';
import { StripePaymentComponent } from './components/stripe-payment/stripe-payment.component';
import { ReviewEventComponent } from './components/review-event/review-event.component';



@NgModule({
  declarations: [
    AddEventComponent,
    ListEventsComponent,
    EditEventComponent,
    EventDetailsComponent,
    ListEventsUserComponent,
    ReservationEventComponent,
    StripePaymentComponent,
    ReviewEventComponent
    
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    EventsRoutingModule
  ]
})
export class EventsModule { }
