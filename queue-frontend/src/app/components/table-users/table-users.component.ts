import { Component, effect, HostListener, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserStateService } from '../../services/states/user/user-state.service';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ServiceManagementService } from '../../services/states/serviceManagement/service-management.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { NgxMaskDirective } from 'ngx-mask';

@Component({
  selector: 'app-table-users',
  imports: [CommonModule, FormsModule, ReactiveFormsModule, NgxMaskDirective],
  templateUrl: './table-users.component.html',
  styleUrl: './table-users.component.css'
})
export class TableUsersComponent implements OnInit {

  // ==================== INJEÇÕES DE DEPENDÊNCIA ====================

  /** Service para gerenciar estado do usuário */
  private userState = inject(UserStateService);

  /** Service para gerenciar estado do serviço */
  private serviceState = inject(ServiceManagementService);

  /** Construtor de formulários reativos */
  private fb = inject(FormBuilder);

  /** Service para exibir notificações toast */
  private snackBar = inject(MatSnackBar);

  // ==================== ESTADOS ====================

  /** Lista de usuários */
  public users = this.userState.users;

  /** Informações detalhadas de um usuário */
  public userInfo = this.userState.userInfo;

  /** Lista de nomes de serviços e departamentos */
  public serviceNamesAndDepartments = this.serviceState.serviceNamesAndDepartments;

  /** Total de usuários cadastrados */
  public totalUsers = this.userState.countTotalUsersStatistics;

  /** Percentual de usuários por cargo */
  public percentage = this.userState.userPercentagesStatistics;

  /** Página atual da listagem */
  public page = this.userState.page;

  /** Total de páginas */
  public totalPages = this.userState.totalPages;

  /** Total de elementos */
  public totalElements = this.userState.totalElements;

  // ==================== CONTROLE DE MODAIS ====================

  /** Modal de registro de usuário */
  public modalRegister = this.userState.modalRegister;

  /** Modal de atualização de usuário */
  public modalUpdate: boolean = false;

  /** Modal de exclusão de usuário */
  public modalDelete: boolean = false;

  /** Modal de visualização de usuário */
  public modalView: boolean = false;

  /** Passo atual do formulário de registro (1, 2 ou 3) */
  public currentStep: number = 1;

  // ==================== FORMULÁRIOS ====================

  /** Formulário de registro de usuário */
  public registerForm!: FormGroup;

  /** Formulário de atualização de usuário */
  public updateForm!: FormGroup;

  // ==================== CONTROLE DE CARGO E PERMISSÕES ====================

  /** Cargo selecionado no formulário */
  public selectedRole = signal<string>('');

  /** Permissões selecionadas para exibição (filtradas por cargo) */
  public selectedPermissions: string[] = [
    'Gerenciar usuários',
    'Gerenciar departamentos',
    'Gerenciar serviços',
    'Agendamentos',
    'Atendimentos',
    'Relatórios',
    'Configurações'
  ];

  /** Todas as permissões disponíveis no sistema */
  public allPermissions: string[] = [
    'Gerenciar usuários',
    'Gerenciar departamentos',
    'Gerenciar serviços',
    'Agendamentos',
    'Atendimentos',
    'Relatórios',
    'Configurações',
    'Chamadas'
  ];

  // ==================== CONTROLE DE VISIBILIDADE DE SENHA ====================

  /** Controla visibilidade do campo de senha */
  public showPassword: boolean = false;

  /** Controla visibilidade do campo de confirmação de senha */
  public showConfirmPassword: boolean = false;

  /** Controla visibilidade do campo de revisão de senha */
  public showReviewPassword: boolean = false;

  // ==================== VARIÁVEIS DE CONTROLE ====================

  /** Índice do dropdown ativo (null quando nenhum está aberto) */
  public dropDown: number | null = null;

  /** Quantidade de itens por página */
  public itemsPerPage = 4;

  // ==================== MODAL DE SUCESSO ====================

  /** Controla exibição do modal de sucesso */
  public showSuccessModal: boolean = false;

  /** Mensagem exibida no modal de sucesso */
  public successMessage: string = 'Usuário criado com sucesso!';

  // ==================== SERVIÇOS SELECIONADOS ====================

  /** IDs dos serviços selecionados para o usuário */
  selectedServices: string[] = [];

  /** Nomes dos serviços selecionados para o usuário */
  selectedServiceNames: string[] = [];

  // ==================== CICLO DE VIDA ====================

  /**
   * Inicializa o componente
   * - Carrega lista de usuários
   * - Carrega estatísticas
   * - Inicializa formulários
   */
  ngOnInit(){
    this.userState.loadingAllUsers();
    this.userState.loadStatistics();
    this.initRegisterForm();
    this.initUpdateForm();
  }

  // ==================== EFEITOS DE REATIVIDADE ====================

  constructor() {

    /**
     * Efeito: Monitora status de registro de usuário
     * - Sucesso: exibe mensagem positiva, fecha modal e reseta formulário
     * - Erro: exibe mensagem de erro
     */
    effect(() => {

      if (this.userState.registerStatus() === 'success') {
        this.snackBar.open(this.userState.registerMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-success']
        });
        this.userState.resetStatus();
        this.registerForm.reset();
        this.currentStep = 1;
        this.modalRegister.set(false);
      }

      if (this.userState.registerStatus() === 'error') {
        this.snackBar.open(this.userState.registerMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-error']
        });
        this.userState.resetStatus();
      }
    })

    /**
     * Efeito: Monitora status de atualização de usuário
     * - Sucesso: exibe mensagem positiva, fecha modal e limpa dados
     * - Erro: exibe mensagem de erro
     */
    effect(() => {

      if (this.userState.updateStatus() === 'success') {
        this.snackBar.open(this.userState.updateMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-success']
        });
        this.userState.resetStatus();
        this.userInfo.set(null);
        this.modalUpdate = false;
        this.selectedServices = [];
      }

      if (this.userState.updateStatus() === 'error') {
        this.snackBar.open(this.userState.updateMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-error']
        });
        this.userState.resetStatus();
      }
    })

    /**
     * Efeito: Monitora status de exclusão de usuário
     * - Sucesso: exibe mensagem positiva, fecha modal e limpa dados
     * - Erro: exibe mensagem de erro
     */
    effect(() => {

      if (this.userState.deleteStatus() === 'success') {
        this.snackBar.open(this.userState.deleteMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-success']
        });
        this.userState.resetStatus();
        this.userInfo.set(null);
        this.modalDelete = false;
      }

      if (this.userState.deleteStatus() === 'error') {
        this.snackBar.open(this.userState.deleteMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-error']
        });
        this.userState.resetStatus();
      }
    })

    /**
     * Efeito: Limpa serviços selecionados quando cargo é RECEPTION
     * Recepcionistas não possuem serviços associados
     */
    effect(() => {

      if (this.selectedRole() === 'RECEPTION') {
        this.registerForm.patchValue({
          serviceIds: []
        });

        this.selectedServices = [];
        this.selectedServiceNames = [];
      }
    })

    /**
     * Efeito: Preenche formulário de atualização quando usuário é selecionado
     * Atualiza os campos do formulário com os dados do usuário
     */
    effect(() => {

      if (this.userInfo() != null && this.modalUpdate) {

        this.selectedRole.set(this.userInfo()?.role ?? '');

        this.updateForm.patchValue({
          userId: this.userInfo()?.userId,
          name: this.userInfo()?.name,
          surname: this.userInfo()?.surname,
          phone: this.userInfo()?.phone,
          email: this.userInfo()?.email,
          username: this.userInfo()?.username,
          role: this.userInfo()?.role,
          active: this.userInfo()?.active,
          counterNumber: this.userInfo()?.counterNumber
        });
      }
    })

    /**
     * Efeito: Preenche serviços selecionados na atualização
     * Mapeia os serviços do usuário para os IDs correspondentes
     */
    effect(() => {

      if (this.userInfo() != null && this.modalUpdate) {

          this.serviceNamesAndDepartments().forEach(service => {
          this.userInfo()?.serviceNames.forEach(serviceName => {

            if (service.name === serviceName) {
              this.selectedServices.push(service.serviceManagementId);
            }
          })
        });
      }
    });
  }

  // ==================== GERENCIAMENTO DE SERVIÇOS ====================

  /**
   * Alterna seleção de um serviço pelo ID
   * @param serviceId - ID do serviço
   * @param event - Evento do checkbox
   */
  public toggleService(serviceId: string, event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;

    if (checked) {
      if (!this.selectedServices.includes(serviceId)) {
        this.selectedServices.push(serviceId);
      }
    } else {
      this.selectedServices = this.selectedServices.filter(
        id => id !== serviceId
      );
    }
  }

  /**
   * Alterna seleção de um serviço pelo nome
   * @param serviceName - Nome do serviço
   * @param event - Evento do checkbox
   */
  public toggleServiceName(serviceName: string, event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;

    if (checked) {
      if (!this.selectedServiceNames.includes(serviceName)) {
        this.selectedServiceNames.push(serviceName);
      }
    } else {
      this.selectedServiceNames = this.selectedServiceNames.filter(
        name => name !== serviceName
      );
    }
  }

  // ==================== INICIALIZAÇÃO DE FORMULÁRIOS ====================

  /**
   * Inicializa o formulário de registro de usuário
   * Campos: dados pessoais, credenciais, cargo e serviços
   */
  private initRegisterForm() {
    this.registerForm = this.fb.group({
      name: ['', Validators.required],
      surname: ['', Validators.required],
      email: [''],
      username: ['', Validators.required],
      phone: [null],
      password: ['', Validators.required],
      confirmPassword: ['', Validators.required],
      counterNumber: [null, Validators.required],
      role: ['', Validators.required],
      serviceIds: [[]],
      active: [true]}
    );
  }

  /**
   * Inicializa o formulário de atualização de usuário
   * Campos similares ao registro, sem validações obrigatórias
   */
  private initUpdateForm() {
    this.updateForm = this.fb.group({
      userId: [''],
      username: [''],
      name: [''],
      surname: [''],
      phone: [''],
      email: [''],
      password: [''],
      confirmPassword: [''],
      role: [''],
      serviceIds: [[]],
      active: [false],
      counterNumber: [null]
    });
  }

  // ==================== GETTERS DO FORMULÁRIO ====================

  /** Retorna o nome completo do usuário no formulário */
  get fullName(): string {
    const name = this.registerForm?.get('name')?.value ?? '';
    const surname = this.registerForm?.get('surname')?.value ?? '';

    return `${name} ${surname}`.trim();
  }

  /** Retorna o nome do usuário ou '-' se vazio */
  get name(): string {
    return this.registerForm?.get('name')?.value ?? '-';
  }

  /** Retorna o sobrenome do usuário ou '-' se vazio */
  get surname(): string {
    return this.registerForm?.get('surname')?.value ?? '-';
  }

  /** Retorna as iniciais do nome e sobrenome */
  get siglas(): string {
    const name = this.registerForm?.get('name')?.value ?? '';
    const surname = this.registerForm?.get('surname')?.value ?? '';

    return `${name[0]}${surname[0]}`;
  }

  /** Retorna o username ou '-' se vazio */
  get username(): string {
    return this.registerForm?.get('username')?.value ?? '-';
  }

  /** Retorna o email ou '-' se vazio */
  get email(): string {
    return this.registerForm?.get('email')?.value ?? '-';
  }

  /** Retorna o telefone ou '-' se vazio */
  get phone(): string {
    return this.registerForm?.get('phone')?.value ?? '-';
  }

  // ==================== NAVEGAÇÃO ENTRE PASSOS ====================

  /**
   * Avança para o próximo passo do formulário
   * Valida campos do passo atual antes de prosseguir
   */
  nextStep() {

    if (this.modalRegister()) {

      const step1Fields = ['name', 'surname', 'email', 'username', 'password', 'confirmPassword'];

      const isStepValid1 = step1Fields.every(field =>
        this.registerForm.get(field)?.valid
      );

      // Verifica se as senhas coincidem e se os campos são válidos
      if (this.registerForm.get('password')?.value === this.registerForm.get('confirmPassword')?.value
        && isStepValid1) {
        this.currentStep ++;
      } else {
        // Marca campos como tocados para exibir erros
        step1Fields.forEach(field => {
          this.registerForm.get(field)?.markAsTouched();
        });

        this.snackBar.open('As senhas não coincidem', 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-error'],
        });
      }
    }

    if (this.modalUpdate) {
      this.currentStep ++;
    }

  }

  /**
   * Volta para o passo anterior
   */
  previousStep() {
    this.currentStep--;
  }

  /**
   * Função para os botões na tela de registro
   */
  showNextButton(): boolean {
    const role = this.selectedRole();

    return (
      this.currentStep < 4 &&
      role !== 'RECEPTION' &&
      role !== 'MANAGER'
    ) || (
      this.currentStep < 3 &&
      (role === 'RECEPTION' || role === 'MANAGER')
    );
  }

  showCreateButton(): boolean {
    const role = this.selectedRole();

    return (
      this.currentStep === 4 ||
      (
        this.currentStep === 3 &&
        (role === 'RECEPTION' || role === 'MANAGER')
      )
    );
  }

  showBackButton(): boolean {
    return this.currentStep > 1;
  }

  // ==================== SELEÇÃO DE CARGO ====================

  /**
   * Seleciona um cargo para o usuário
   * Atualiza formulário e permissões exibidas
   * @param role - Cargo selecionado
   */
  public selectRole(role: string): void {
    this.selectedRole.set(role);

    this.registerForm.patchValue({
      role: role
    });

    this.updateForm.patchValue({
      role: role
    });

    this.selectedPermissions = [...this.permissionsByRole[role]];
  }

  /**
   * Retorna a classe CSS para o ícone do cargo
   * @param role - Cargo
   * @returns Nome da classe CSS
   */
  public getRoleIcon(role: string): string {
    switch(role) {
      case 'MANAGER': return 'supervisor-bg';
      case 'RECEPTION': return 'attendant-bg';
      case 'ATTENDANT': return 'viewer-bg';
      default: return 'supervisor-bg';
    }
  }

  /**
   * Retorna o nome do cargo em português
   * @param role - Cargo em inglês
   * @returns Nome em português
   */
  public getRoleDisplayName(role: string): string {
    switch (role) {
      case 'MANAGER': return 'Gerente';
      case 'ATTENDANT': return 'Atendente';
      case 'RECEPTION': return 'Recepcionista';
      default: return 'Gerente';
    }
  }

  /**
   * Retorna a descrição do cargo
   * @param role - Cargo
   * @returns Descrição das responsabilidades
   */
  public getRoleDescription(role: string): string {
    switch (role) {
      case 'MANAGER': return 'Gerencia atendentes, recepcionistas e relatórios';
      case 'ATTENDANT': return 'Realiza atendimentos';
      case 'RECEPTION': return 'Realiza agendamentos';
      default: return 'Gerencia atendentes, recepcionistas e relatórios';
    }
  }

  // ==================== GERENCIAMENTO DE PERMISSÕES ====================

  /** Mapeamento de cargos para permissões associadas */
  public permissionsByRole: Record<string, string[]> = {
    MANAGER: [
      'Gerenciar departamentos',
      'Gerenciar serviços',
      'Gerenciar usuários',
      'Visualizar relatórios',
      'Configurações'
    ],

    ATTENDANT: [
      'Painel de Atendimento',
      'Visualizar relatórios',
      'Configurações'
    ],

    RECEPTION: [
      'Gerenciar agendamentos',
      'Visualizar relatórios',
      'Configurações'
    ]
  };

  /**
   * Navega para um passo específico com validação
   * @param step - Número do passo (1, 2 ou 3)
   */
  handleCurrentStep(step: number) {

    const step1Fields = ['name', 'surname', 'email', 'username', 'password', 'confirmPassword'];
    const step2Fields = ['role'];

    const isStepValid1 = step1Fields.every(field =>
      this.registerForm.get(field)?.valid
    );

    const isStepValid2 = step2Fields.every(field =>
      this.registerForm.get(field)?.valid
    );

    if (step === 1) {
      this.currentStep = 1;
    } else if (step === 2 && isStepValid1) {
      this.currentStep = 2;
    } else if (step === 3 && isStepValid1 && isStepValid2) {
      this.currentStep = 3;
    }
  }

  // ==================== CONTROLE DE MODAIS ====================

  /**
   * Abre modal de registro de usuário
   * Carrega lista de serviços para seleção
   */
  public openModalRegister(): void {
    this.modalRegister.set(true);
    this.serviceState.loadServiceNamesAndDepartments();
  }

  /**
   * Fecha modal de registro de usuário
   * Reseta formulário e passo atual
   */
  public closeModalRegister(): void {
    this.modalRegister.set(false);
    this.registerForm.reset();
    this.currentStep = 1;
  }

  /**
   * Abre modal de atualização de usuário
   * Busca dados do usuário e carrega serviços
   * @param userId - ID do usuário
   */
  public openModalUpdate(userId: string) {
    this.userState.getUserById(userId);
    this.serviceState.loadServiceNamesAndDepartments();
    this.modalUpdate = true;
  }

  /**
   * Fecha modal de atualização de usuário
   * Reseta dados e limpa seleções
   */
  public closeModalUpdate(): void {
    this.modalUpdate = false;
    this.currentStep = 1;
    this.userState.resetInfoUser();
    this.selectedRole.set('');
    this.selectedServices = [];
  }

  /**
   * Abre modal de exclusão de usuário
   * Busca dados do usuário e fecha dropdown
   * @param userId - ID do usuário
   */
  public openModalDelete(userId: string): void {
    this.modalDelete = true;
    this.dropDown = null;
    this.userState.getUserById(userId);
  }

  /**
   * Fecha modal de exclusão de usuário
   * Reseta dados do usuário
   */
  public closeModalDelete(): void {
    this.modalDelete = false;
    this.userState.resetInfoUser();
  }

  /**
   * Abre modal de visualização de usuário
   * Busca dados do usuário
   * @param userId - ID do usuário
   */
  public openModalView(userId: string) {
    this.userState.getUserById(userId);
    this.modalView = true;
  }

  /**
   * Fecha modal de visualização de usuário
   * Reseta dados do usuário
   */
  public closeModalView() {
    this.modalView = false;
    this.userState.resetInfoUser();
  }

  // ==================== CONTROLE DE DROPDOWN ====================

  /**
   * Fecha dropdown ao clicar fora dele
   * Listener global de clique no documento
   * @param event - Evento de clique
   */
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    const clickedInsideDropdown = target.closest('.button-menu-table') || target.closest('.drop-down-delete');
    if (!clickedInsideDropdown) {
      this.closeDropDown();
    }
  }

  /**
   * Abre ou fecha dropdown de um item específico
   * @param index - Índice do item
   */
  openDropDown(index: number): void {
    this.dropDown = this.dropDown === index ? null : index;
  }

  /**
   * Fecha qualquer dropdown aberto
   */
  closeDropDown(): void {
    this.dropDown = null;
  }

  // ==================== MÉTODOS AUXILIARES ====================

  /**
   * Retorna o nome do cargo em português (versão estendida)
   * @param role - Cargo em inglês
   * @returns Nome em português completo
   */
  getRole(role: string): string {
    switch (role) {
      case 'ADMIN': return 'Administrador';
      case 'MANAGER': return 'Gerente';
      case 'RECEPTION': return 'Recepcionista';
      case 'ATTENDANT': return 'Atendente';
      default: return 'Desconhecido';
    }
  }

  // ==================== BUSCA E PAGINAÇÃO ====================

  /**
   * Executa busca ao digitar no campo de pesquisa
   * @param event - Evento do input
   */
  onSearch(event: any): void {
    this.userState.setSearch(event.target.value);
  }

  /**
   * Avança para a próxima página
   */
  nextPage(): void {
    this.userState.nextPage();
  }

  /**
   * Volta para a página anterior
   */
  previousPage(): void {
    this.userState.previousPage();
  }

  /**
   * Navega para uma página específica
   * @param page - Número da página (base 0)
   */
  goToPage(page: number): void {
    this.userState.goToPage(page);
  }

  /**
   * Calcula o índice inicial dos itens exibidos
   * @returns Índice inicial (base 1)
   */
  getStartIndex(): number {
    return this.page() * this.itemsPerPage + 1;
  }

  /**
   * Calcula o índice final dos itens exibidos
   * @returns Índice final
   */
  getEndIndex(): number {
    return Math.min((this.page() + 1) * this.itemsPerPage, this.totalElements());
  }

  /**
   * Gera array de números de páginas visíveis (máximo 4)
   * @returns Array com números das páginas
   */
  getPagesArray(): number[] {
    const total = this.totalPages();
    const current = this.page();
    const maxVisible = 4;

    let start = current - Math.floor(maxVisible / 2);
    let end = current + Math.floor(maxVisible / 2) + 1;

    if (start < 0) {
      start = 0;
      end = Math.min(maxVisible, total);
    }

    if (end > total) {
      end = total;
      start = Math.max(0, total - maxVisible);
    }

    return Array.from({ length: end - start }, (_, i) => start + i);
  }

  // ==================== CONTROLE DE VISIBILIDADE DE SENHA ====================

  /** Alterna visibilidade do campo de senha */
  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  /** Alterna visibilidade do campo de confirmação de senha */
  toggleConfirmPassword(): void {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  /** Alterna visibilidade do campo de revisão de senha */
  toggleReviewPassword(): void {
    this.showReviewPassword = !this.showReviewPassword;
  }

  // ==================== CICLO DE VIDA - EVENTOS ====================

  /**
   * Fecha modais ao pressionar tecla ESC
   * Listener de teclado global
   */
  @HostListener('window:keydown.escape', ['$event'])
  onEscapePressed(event: Event): void {
    if (this.modalRegister()) this.closeModalRegister();
    if (this.modalUpdate) this.closeModalUpdate();
    if (this.modalDelete) this.closeModalDelete();
    if (this.modalView) this.closeModalView();
  }

  // ==================== MÉTODOS DE CRUD ====================

  /**
   * Registra um novo usuário
   * Combina dados do formulário com serviços selecionados
   */
  registerUser() {
    this.userState.registerUser({...this.registerForm.value, serviceIds: this.selectedServices});
  }

  /**
   * Atualiza um usuário existente
   * Valida senhas antes de enviar
   */
  updateUser() {
    if (this.updateForm.get('password')?.touched && this.updateForm.get('password')?.value !== this.updateForm.get('confirmPassword')?.value) return;
    this.userState.updateUser({...this.updateForm.value, serviceIds: this.selectedServices});
  }

  /**
   * Exclui o usuário atualmente selecionado
   */
  deleteUser () {
    this.userState.deleteUser();
  }
}
