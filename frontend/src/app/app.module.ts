import { NgModule } from '@angular/core';
import { BrowserModule, provideClientHydration } from '@angular/platform-browser';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';

import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { BrowserAnimationsModule, NoopAnimationsModule } from '@angular/platform-browser/animations';

// Routing
import { AppRoutingModule } from './app-routing.module';

// Core
import { AppComponent } from './app.component';
import { HeaderComponent } from './core/header/header.component';
import { FooterComponent } from './core/footer/footer.component';
import { HomeComponent } from './core/home/home.component';

// Features
import { NotFoundComponent } from './features/not-found/not-found.component';
import { EventsModule } from './features/events/events.module';
import { CartComponent } from './features/souvenirs-shops/cart/cart.component';
import { SouvenirsShopsModule } from './features/souvenirs-shops/souvenirs-shops.module';

// Shared / UI
import { ChatWidgetComponent } from './shared/components/chat-widget/chat-widget.component';
import { TourOverlayComponent } from './core/components/tour-overlay/tour-overlay.component';

// Interceptors
import { AuthInterceptor } from './features/auth/auth.interceptor';

// Toastr
import { ToastrModule, provideToastr } from 'ngx-toastr';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    FooterComponent,
    HomeComponent,
    NotFoundComponent,

    // features
    CartComponent,

    // shared UI components
    ChatWidgetComponent,
  ],

  imports: [
    BrowserModule,
    AppRoutingModule,

    HttpClientModule,

    FormsModule,
    ReactiveFormsModule,

    BrowserAnimationsModule,
    NoopAnimationsModule,

    // modules features
    EventsModule,
    SouvenirsShopsModule,

    // UI libs
    ToastrModule.forRoot(),

    // standalone component (tour overlay)
    TourOverlayComponent,
  ],

  providers: [
    provideClientHydration(),
    provideAnimationsAsync(),
    provideToastr(),

    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true,
    },
  ],

  bootstrap: [AppComponent],
})
export class AppModule {}