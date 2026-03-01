import { HttpInterceptorFn } from '@angular/common/http';

/**
 * Placeholder interceptor for future cross-cutting HTTP behavior.
 * Currently it simply forwards the request without modifications.
 */
export const apiPrefixInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req);
};
