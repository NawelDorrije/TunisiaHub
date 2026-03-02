import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { environment } from '../../../environments/environment';
import { SessionService } from '../services/session.service';

/**
 * Adds API prefix for relative URLs and injects session identity headers.
 */
export const apiPrefixInterceptor: HttpInterceptorFn = (req, next) => {
  const sessionService = inject(SessionService);

  const isAbsoluteUrl = /^https?:\/\//i.test(req.url);
  const nextUrl = isAbsoluteUrl ? req.url : `${environment.apiUrl}${req.url}`;

  const request = req.clone({
    url: nextUrl,
    setHeaders: {
      'X-USER-ID': String(sessionService.userId()),
      'X-ROLE': sessionService.role(),
    },
  });

  return next(request);
};
