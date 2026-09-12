import { Routes } from '@angular/router';
import { HomeComponent } from './page/home/home.component';
import { LoginComponent } from './page/login/login.component';
import { authGuard } from './guards/auth/auth.guard';
import { QueueDisplayComponent } from './components/queue-display/queue-display.component';
import { guestGuard } from './guards/guest/guest.guard';

export const routes: Routes = [

  { path: '', redirectTo: 'login', pathMatch: 'full' },

  { path: 'login', component: LoginComponent, canActivate: [guestGuard] },

  { path: 'home', component: HomeComponent, canActivate: [authGuard] },

  { path: 'queue-display', component: QueueDisplayComponent, canActivate: [authGuard] },

  { path: '**', redirectTo: 'login' }
];
