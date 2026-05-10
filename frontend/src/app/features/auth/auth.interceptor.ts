// auth.interceptor.ts
// Placer dans : src/app/core/interceptors/auth.interceptor.ts

import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';

import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';

import { AuthService } from './services/auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  intercept(
    req: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {

    // Ignore auth for requests with X-Skip-Auth
    if (req.headers.has('X-Skip-Auth')) {
      const cleanedRequest = req.clone({
        headers: req.headers.delete('X-Skip-Auth')
      });

      return next.handle(cleanedRequest);
    }

    const token = this.authService.getToken();

    let headers: { [key: string]: string } = {
      'ngrok-skip-browser-warning': 'true'
    };

    // Add token if exists
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    // Clone request with headers
    const clonedRequest = req.clone({
      setHeaders: headers
    });

    return next.handle(clonedRequest).pipe(
      catchError((error: HttpErrorResponse) => {

        // If unauthorized → logout and redirect
        if (error.status === 401) {
          this.authService.logout();
          this.router.navigate(['/auth/sign-in']);
        }

        return throwError(() => error);
      })
    );
  }
}