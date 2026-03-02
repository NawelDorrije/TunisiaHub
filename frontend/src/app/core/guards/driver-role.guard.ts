import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SessionService } from '../services/session.service';

export const driverRoleGuard: CanActivateFn = () => {
  const sessionService = inject(SessionService);
  const router = inject(Router);

  if (sessionService.role() === 'DRIVER') {
    return true;
  }

  return router.parseUrl('/carpooling/trips');
};
