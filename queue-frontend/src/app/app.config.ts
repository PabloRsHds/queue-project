import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideEnvironmentNgxMask } from 'ngx-mask';

import { routes } from './app.routes';
import { AuthInterceptorService } from './interceptor/auth-interceptor.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideEnvironmentNgxMask(),
    provideZoneChangeDetection(),
    provideHttpClient(withInterceptors([AuthInterceptorService]))
  ]
};

