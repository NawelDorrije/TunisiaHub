import { NgModule } from '@angular/core';
import {
  BrowserModule,
  provideClientHydration,
} from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { FooterComponent } from './core/footer/footer.component';
import { HeaderComponent } from './core/header/header.component';
import { NotFoundComponent } from './features/not-found/not-found.component';
import { HomeComponent } from './core/home/home.component';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { EventsModule } from './features/events/events.module'; // ✅ AJOUT
//import { FullCalendarModule } from '@fullcalendar/angular';
//import { CalendarEventsComponent } from './features/events/components/calendar-events/calendar-events.component';
//import { FormsModule } from '@angular/forms';
//import { ReviewEventComponent } from './pages/review-event/review-event.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ToastrModule } from 'ngx-toastr';
import { provideToastr } from 'ngx-toastr';

@NgModule({
  declarations: [
    AppComponent,
    FooterComponent,
    HeaderComponent,
    NotFoundComponent,
    HomeComponent,
    //CalendarEventsComponent,
    
    //ReviewEventComponent,
  ],
  imports: [BrowserModule, AppRoutingModule, HttpClientModule, BrowserAnimationsModule, FormsModule, EventsModule,  // ✅ IMPORTANT
    ToastrModule.forRoot()],
  providers: [provideClientHydration(), provideToastr()],
  bootstrap: [AppComponent],
})
export class AppModule {}
