import { Component, computed, effect, HostListener, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { LoginStateService } from '../../services/states/login/login-state.service';
import { S } from '@angular/cdk/keycodes';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { UnitStateService } from '../../services/states/unit/unit-state.service';

@Component({
  selector: 'app-login',
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit{

  // ==================== INJEÇÕES DE DEPENDÊNCIA ====================

  /** Service para gerenciar estado do login */
  public loginState = inject(LoginStateService);

  /** Unidades disponíveis **/
  public unitState = inject(UnitStateService);
  public units = this.unitState.units;

  /** Construtor de formulários reativos */
  public fb = inject(FormBuilder);

  /** Service para exibir notificações toast */
  public snackBar = inject(MatSnackBar);

  /** Service para navegação entre rotas */
  public router = inject(Router);

  // ==================== ESTADOS DE RESPONSIVIDADE ====================

  /** Indica se está em dispositivo móvel (largura < 768px) */
  isMobile = signal(window.innerWidth < 768);

  // ==================== VARIÁVEIS DE CONTROLE ====================

  /** Controla visibilidade do campo de senha */
  viewPassword = false;

  // ==================== FORMULÁRIO ====================

  /** Formulário de login com campos de credenciais e remember me */
  loginForm!: FormGroup;

  /* Inicializa as unidades disponíveis */
  ngOnInit(): void {
    this.unitState.getAllUnitsForLogin();
  }

  // ==================== CONSTRUTOR ====================

  constructor() {
    /**
     * Recupera credenciais salvas no localStorage
     * emailOrUsername, password e rememberMe são persistidos quando usuário marca "Lembrar-me"
     */
    const unitId = localStorage.getItem('unitId');
    const emailOrUsername = localStorage.getItem('emailOrUsername');
    const password = localStorage.getItem('password');
    const rememberMe = localStorage.getItem('rememberMe');

    /**
     * Inicializa o formulário de login com valores salvos
     * Se não houver dados salvos, os campos ficam vazios
     */
    this.loginForm = this.fb.group({
      unitId: [unitId ??''],
      emailOrUsername: [emailOrUsername ?? ''],
      password: [password ?? ''],
      rememberMe: [rememberMe]
    });

    /**
     * Efeito: Monitora status de login
     * - Sucesso: exibe mensagem positiva, navega para home e reseta status
     * - Erro: exibe mensagem de erro e reseta status
     */
    effect(() => {

      if (this.loginState.loginStatus() === 'success') {

        this.snackBar.open(this.loginState.loginMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-success']
        });

        this.router.navigate(['/home']);

        this.loginState.resetStatus();
      }

      if (this.loginState.loginStatus() === 'error') {
        this.snackBar.open(this.loginState.loginMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-error']
        });

        this.loginState.resetStatus();
      }
    })
  }

  // ==================== MÉTODO DE LOGIN ====================

  /**
   * Realiza o login do usuário
   * - Extrai credenciais do formulário
   * - Persiste ou remove dados conforme opção "Lembrar-me"
   * - Chama service de login
   */
  login() {

    const unitId = this.loginForm.value.unitId;
    const emailOrUsername = this.loginForm.value.emailOrUsername;
    const password = this.loginForm.value.password;
    const rememberMe = this.loginForm.value.rememberMe;

    /**
     * Gerencia persistência das credenciais
     * Se "Lembrar-me" estiver marcado, salva os dados no localStorage
     * Caso contrário, remove os dados salvos
     */
    if (rememberMe) {
      localStorage.setItem('unitId', unitId);
      localStorage.setItem('emailOrUsername', emailOrUsername);
      localStorage.setItem('password', password);
      localStorage.setItem('rememberMe', 'true');
    } else {
      localStorage.removeItem('emailOrUsername');
      localStorage.removeItem('password');
      localStorage.removeItem('rememberMe');
    }

    // Envia credenciais para autenticação
    this.loginState.login({ unitId, emailOrUsername, password });
  }

  // ==================== MÉTODOS DE RESPONSIVIDADE ====================

  /**
   * Atualiza o estado de dispositivo móvel ao redimensionar a janela
   * Listener de redimensionamento da janela
   */
  @HostListener('window:resize')
  onResize() {
    this.isMobile.set(window.innerWidth < 768);
  }

}
