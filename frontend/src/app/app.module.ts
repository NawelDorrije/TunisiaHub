import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { ReactiveFormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { HTTP_INTERCEPTORS, provideHttpClient, withFetch, withInterceptorsFromDi } from '@angular/common/http';

// Core
import { AppComponent } from './app.component';
import { HeaderComponent } from './core/header/header.component';
import { FooterComponent } from './core/footer/footer.component';
import { HomeComponent } from './core/home/home.component';

// Features
import { NotFoundComponent } from './features/not-found/not-found.component';
import { CartComponent } from './features/souvenirs-shops/cart/cart.component';

// Modules
import { AppRoutingModule } from './app-routing.module';
import { SouvenirsShopsModule } from './features/souvenirs-shops/souvenirs-shops.module';

// Shared
import { ChatWidgetComponent } from './shared/components/chat-widget/chat-widget.component';
import { TourOverlayComponent } from './core/components/tour-overlay/tour-overlay.component';

// Interceptor
import { AuthInterceptor } from './features/auth/auth.interceptor';

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    FooterComponent,
    HomeComponent,
    NotFoundComponent,
    CartComponent,
    ChatWidgetComponent
  ],

  imports: [
    BrowserModule,
    ReactiveFormsModule,
    NoopAnimationsModule,
    AppRoutingModule,
    SouvenirsShopsModule,
    TourOverlayComponent
  ],

  providers: [
    provideHttpClient(
      withFetch(),
      withInterceptorsFromDi()
    ),

    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    }
  ],

  bootstrap: [AppComponent]
})
export class AppModule {}