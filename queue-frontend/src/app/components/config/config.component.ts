import { CommonModule } from '@angular/common';
import { Component, effect, inject, OnInit } from '@angular/core';
import { UserStateService } from '../../services/states/user/user-state.service';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { NgxMaskDirective } from "ngx-mask";


@Component({
  selector: 'app-config',
  imports: [CommonModule, FormsModule, ReactiveFormsModule, NgxMaskDirective],
  templateUrl: './config.component.html',
  styleUrl: './config.component.css'
})
export class ConfigComponent implements OnInit {

  // ==================== INJEÇÕES DE DEPENDÊNCIA ====================

  /** Service para gerenciar estado do usuário */
  public fb = inject(FormBuilder);

  /** Service para gerenciar estado do usuário */
  public userState = inject(UserStateService);

  /** Service para exibir notificações toast */
  public snackBar = inject(MatSnackBar);

  // ==================== PROPRIEDADES PÚBLICAS ====================

  /** Usuário atualmente logado (signal) */
  public userLogged = this.userState.userLogged;

  /** Item de navegação ativo no menu lateral */
  public navItem:string = 'profile';

  /** Controla visibilidade do campo de senha */
  public showPassword: boolean = false;

  /** Formulário reativo do perfil do usuário */
  public profileForm!: FormGroup;

  // ==================== CICLO DE VIDA ====================

  /**
   * Inicializa o componente
   * - Busca dados do usuário pelo token JWT
   * - Cria a estrutura do formulário
   */
  ngOnInit(): void {
    // Inicializa formulário com campos vazios
    this.profileForm = this.fb.group({
      name: '',              // Nome do usuário
      email: '',             // E-mail do usuário
      phone: '',             // Telefone do usuário
      role: '',              // Cargo/função do usuário
      counterNumber: '',     // Número do balcão/guichê
      oldPassword: '',       // Senha atual
      newPassword: '',       // Nova senha
      confirmPassword: ''    // Confirmação da nova senha
    });
  }

  /**
   * Construtor do componente
   * Configura efeitos reativos para:
   * 1. Preencher formulário quando dados do usuário são carregados
   * 2. Monitorar status de atualização do usuário
   */
  constructor() {

    /**
     * Efeito 1: Atualiza o formulário quando o usuário logado muda
     * Preenche os campos com os dados mais recentes do usuário
     */
    effect(() => {

      if (this.userLogged() != null) {

        const user = this.userLogged();

        this.profileForm.patchValue({
          name: user?.name,
          email: user?.email,
          phone: user?.phone,
          role: this.getRoleDisplayName(user?.role ?? ''),
          counterNumber: user?.counterNumber
        });

      }
    })

    /**
     * Efeito 2: Monitora o status da atualização do usuário
     * - Sucesso: exibe mensagem positiva e limpa campos de senha
     * - Erro: exibe mensagem de erro e limpa campos de senha
     */
    effect(() => {

      if (this.userState.updateStatus() == 'success') {

        this.snackBar.open(this.userState.updateMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-success']
        });

        // Limpa campos de senha após atualização bem-sucedida
        this.profileForm.patchValue({
          oldPassword: '',
          newPassword: '',
          confirmPassword: ''
        });
        this.userState.resetStatus();
      }

      if (this.userState.updateStatus() == 'error') {

        this.snackBar.open(this.userState.updateMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-error']
        });

        // Limpa campos de senha mesmo em caso de erro
        this.profileForm.patchValue({
          oldPassword: '',
          newPassword: '',
          confirmPassword: ''
        });
        this.userState.resetStatus();
      }
    })
  }

  // ==================== MÉTODOS PÚBLICOS ====================

  /**
   * Converte o código do cargo para nome amigável em português
   * @param role - Código do cargo (MANAGER, ATTENDANT, RECEPTION)
   * @returns Nome do cargo em português
   */
  public getRoleDisplayName(role: string): string {
    switch (role) {
      case 'MANAGER': return 'Gerente';
      case 'ATTENDANT': return 'Atendente';
      case 'RECEPTION': return 'Recepcionista';
      default: return 'Administrador';
    }
  }

  /**
   * Altera o item de navegação ativo
   * @param item - Nome do item de navegação
   */
  public navItemChange(item: string) {
    this.navItem = item;
  }

  /**
   * Atualiza os dados do usuário
   * - Verifica se usuário está logado
   * - Valida se senhas nova e confirmação são iguais
   * - Chama service para atualizar usuário
   */
  updateUser() {

    // Verifica se há usuário logado
    if (this.userLogged() == null) return;

    // Valida se as senhas nova e confirmação são iguais
    if (this.profileForm.value.newPassword !== this.profileForm.value.confirmPassword) {
      this.snackBar.open('Senhas não conferem', 'Fechar', {
        duration: 3000,
        panelClass: ['snackbar-error']
      });
      return;
    };

    // Atualiza usuário com telefone, nova senha e ID do usuário
    this.userState.updateUser({
      phone: this.profileForm.value.phone,
      password: this.profileForm.value.newPassword,
      userId: this.userLogged()!.userId
    });
  }

}
