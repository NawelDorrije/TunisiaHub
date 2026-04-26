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

  constructor(private authService: AuthService, private router: Router) {}
intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
  const token = this.authService.getToken();

  let headers: { [key: string]: string } = {
    'ngrok-skip-browser-warning': 'true'
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  request = request.clone({ setHeaders: headers });

  return next.handle(request).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        this.authService.logout();
        this.router.navigate(['/auth/sign-in']);
      }
      return throwError(() => error);
    })
  );
}
}
