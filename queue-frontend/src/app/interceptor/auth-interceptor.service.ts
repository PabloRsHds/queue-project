import {
  HttpInterceptorFn,
  HttpErrorResponse
} from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { HttpService } from '../services/backend/http.service';

// Interceptor HTTP:
// - adiciona accessToken automaticamente
// - renova accessToken usando refreshToken HttpOnly Cookie
// - faz logout caso a renovação falhe
export const AuthInterceptorService: HttpInterceptorFn = (req, next) => {

  const router = inject(Router);
  const api = inject(HttpService);

  const accessToken = localStorage.getItem('accessToken');

  // Não intercepta login nem refresh
  const isLogin = req.url.endsWith('/login');
  const isRefresh = req.url.endsWith('/login/refresh-tokens');
  const isLogout = req.url.endsWith('/login/logout');

  if (isLogin || isRefresh || isLogout) {
    return next(req);
  }

  let authReq = req;

  // Adiciona accessToken caso exista e ainda seja válido
  if (accessToken && !isTokenExpired(accessToken)) {

    authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${accessToken}`
      },
      withCredentials: true
    });
  } else {
    // Mesmo sem accessToken válido,
    // mantém o cookie disponível caso seja necessário
    authReq = req.clone({
      withCredentials: true
    });
  }

  return next(authReq).pipe(

    catchError((error: HttpErrorResponse) => {
      // Não é erro de autenticação
      if (error.status !== 401) {
        return throwError(() => error);
      }

      if (!accessToken) {
        logout(router);
        return throwError(() => error);
      }

      // Access token expirou
      // Solicita novo usando o cookie HttpOnly
      return api.refreshTokens().pipe(

        switchMap(tokens => {
          // Salva somente o novo accessToken
          localStorage.setItem(
            'accessToken',
            tokens.accessToken
          );

          // Refaz a requisição original com o novo token
          const newRequest = req.clone({
            setHeaders: {
              Authorization: `Bearer ${tokens.accessToken}`
            },
            withCredentials: true
          });
          return next(newRequest);
        }),

        catchError(refreshError => {
          logout(router);
          return throwError(() => refreshError);
        })
      );
    })
  );
};

function isTokenExpired(token: string): boolean {

  try {
    const payload = JSON.parse(
      atob(token.split('.')[1])
    );
    return payload.exp * 1000 <= Date.now();
  } catch {
    return true;
  }
}

function logout(router: Router): void {
  localStorage.removeItem('accessToken');
  router.navigate(['/login']);
}
