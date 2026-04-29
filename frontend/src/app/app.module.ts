import { NgModule } from '@angular/core';
import { BrowserModule, provideClientHydration } from '@angular/platform-browser';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { ReactiveFormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

// Core Components
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

// Interceptor
import { AuthInterceptor } from './features/auth/auth.interceptor';

// Shared Components
import { ChatWidgetComponent } from './shared/components/chat-widget/chat-widget.component';

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    FooterComponent,
    HomeComponent,
    NotFoundComponent,
    CartComponent,
    ChatWidgetComponent,
  ],
  imports: [
    // Core Angular Modules
    BrowserModule,
    HttpClientModule,
    ReactiveFormsModule,
    NoopAnimationsModule,

    // Routing & Feature Modules
    AppRoutingModule,
    SouvenirsShopsModule,
  ],
  providers: [
    provideClientHydration(),

    // HTTP Interceptor
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true,
    },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}