import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const authGuard: CanActivateFn = () => {

  const router = inject(Router);

  if (typeof window === 'undefined') {
    return false;
  }

  const accessToken = localStorage.getItem('accessToken');

  if (!accessToken) {
    router.navigate(['/login']);
    return false;
  }

  if (isTokenExpired(accessToken)) {

    localStorage.removeItem('accessToken');
    router.navigate(['/login']);
    return false;
  }

  return true;
};


function isTokenExpired(token: string): boolean {

  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.exp * 1000 <= Date.now();

  } catch {
    return true;
  }

}
