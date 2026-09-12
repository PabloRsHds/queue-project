import { inject, Injectable, signal } from '@angular/core';
import { HttpService } from '../../backend/http.service';
import { LoginDto } from '../../../dtos/login/LoginDto';
import { HttpErrorResponse } from '@angular/common/http';
import { interval } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class LoginStateService {

  // Injections
  private http = inject(HttpService);

  // ===================== RESPONSE STATUS =====================
  public loginLoading = signal(false);
  public loginMessage = signal('');
  public loginStatus = signal<'success' | 'error' | 'default'>('default');

  public logoutMessage = signal('');
  public logoutStatus = signal<'success' | 'error' | 'default'>('default');


  // login
  login(request: LoginDto) {

    this.loginLoading.set(true);

    this.http.login(request).subscribe({
      next: (response) => {

        // Agora salva somente o accessToken
        localStorage.setItem('accessToken', response.accessToken);

        this.loginMessage.set('Login feito com sucesso');
        this.loginStatus.set('success');
        this.loginLoading.set(false);
      },

      error: (error: HttpErrorResponse) => {
        this.loginMessage.set(error.error?.message || 'Erro ao tentar logar');
        this.loginStatus.set('error');
        this.loginLoading.set(false);
      }
    });
  }

  logout() {
    this.http.logout().subscribe({
      next: () => {
        this.logoutMessage.set('Logout realizado com sucesso');
        this.logoutStatus.set('success');
      },
      error: (error: HttpErrorResponse) => {
        this.logoutMessage.set(error.error?.message || 'Erro ao realizar logout');
        this.logoutStatus.set('error');
      }
    });
  }

  // Resets
  resetStatus() {
    this.loginStatus.set('default');
    this.logoutStatus.set('default');
  }

}
