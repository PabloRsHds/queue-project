import { Component, effect, HostListener, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ServiceManagementService } from '../../services/states/serviceManagement/service-management.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { DepartmentStateService } from '../../services/states/department/department-state.service';

@Component({
  selector: 'app-table-services',
  imports: [ CommonModule, FormsModule, ReactiveFormsModule ],
  templateUrl: './table-services.component.html',
  styleUrl: './table-services.component.css'
})
export class TableServicesComponent implements OnInit{

  // ==================== CICLO DE VIDA ====================

  /**
   * Inicializa o componente
   * - Carrega lista de serviços
   * - Carrega estatísticas
   */
  ngOnInit(): void {
    this.serviceState.loadServices();
    this.serviceState.loadStatistics();
  }

  // ==================== INJEÇÕES DE DEPENDÊNCIA ====================

  /** Service para gerenciar estado do serviço */
  public serviceState = inject(ServiceManagementService);

  /** Service para gerenciar estado do departamento */
  private deparmentState = inject(DepartmentStateService);

  /** Construtor de formulários reativos */
  private fb = inject(FormBuilder);

  /** Service para exibir notificações toast */
  private snackBar = inject(MatSnackBar);

  // ==================== ESTADOS ====================

  /** Lista de serviços */
  public services = this.serviceState.services;

  /** Informações detalhadas de um serviço */
  public serviceInfo = this.serviceState.serviceInfo;

  /** Lista de nomes de departamentos */
  public departmentNames = this.deparmentState.departmentNames;

  /** Página atual da listagem */
  public page = this.serviceState.page;

  /** Total de páginas */
  public totalPages = this.serviceState.totalPages;

  /** Total de elementos */
  public totalElements = this.serviceState.totalElements;

  // ==================== MÉTRICAS ====================

  /** Total de serviços cadastrados */
  public totalServices = this.serviceState.countTotalServicesStatistics;

  /** Percentual de serviços por departamento */
  public percentage = this.serviceState.servicePercentagesStatistics;

  // ==================== FORMULÁRIO DE ATUALIZAÇÃO ====================

  /** Formulário de atualização de serviço */
  public updateForm!: FormGroup;

  // ==================== CONTROLE DE MODAIS ====================

  /** Índice do dropdown ativo (null quando nenhum está aberto) */
  public dropDown: number | null = null;

  /** Modal de registro de serviço */
  public modalRegister = this.serviceState.modalRegister;

  /** Modal de atualização de serviço */
  public modalUpdate: boolean = false;

  /** Modal de exclusão de serviço */
  public modalDelete: boolean = false;

  /** Modal de visualização de serviço */
  public modalView: boolean = false;

  /** Modal de visualização de descrição */
  public modalDescription: boolean = false;

  // ==================== PAGINAÇÃO ====================

  /** Quantidade de itens por página */
  itemsPerPage = 4;

  // ==================== EFEITOS DE REATIVIDADE ====================

  constructor() {

    /**
     * Efeito: Preenche formulário de atualização quando serviço é selecionado
     * Atualiza os campos do formulário com os dados do serviço
     */
    effect(() => {

      const service = this.serviceInfo();

      if (this.modalUpdate && this.serviceInfo() != null) {

        this.updateForm.patchValue({
          name: service?.name,
          code: service?.code,
          description: service?.description,
          departmentName: service?.departmentName,
          active: service?.active
        });
      }
    });

    /**
     * Efeito: Monitora status de registro de serviço
     * - Sucesso: exibe mensagem positiva, fecha modal e reseta formulário
     * - Erro: exibe mensagem de erro
     */
    effect(() => {

      if (this.serviceState.registerStatus() === 'success') {

        this.registerForm.reset();
        this.serviceState.resetStatus();
        this.modalRegister.set(false);

        this.snackBar.open(this.serviceState.registerMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-success']
        });
      }

      if (this.serviceState.registerStatus() === 'error') {

        this.serviceState.resetStatus();

        this.snackBar.open(this.serviceState.registerMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-error']
        });
      }
    });

    /**
     * Efeito: Monitora status de atualização de serviço
     * - Sucesso: exibe mensagem positiva, fecha modal e reseta formulário
     * - Erro: exibe mensagem de erro
     */
    effect(() => {

      if (this.serviceState.updateStatus() === 'success') {

        this.updateForm.reset();
        this.serviceState.resetStatus();
        this.modalUpdate = false;

        this.snackBar.open(this.serviceState.updateMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-success']
        });
      }

      if (this.serviceState.updateStatus() === 'error') {

        this.serviceState.resetStatus();

        this.snackBar.open(this.serviceState.updateMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-error']
        });
      }
    });

    /**
     * Efeito: Monitora status de exclusão de serviço
     * - Sucesso: exibe mensagem positiva e fecha modal
     * - Erro: exibe mensagem de erro
     */
    effect(() => {

      if (this.serviceState.deleteStatus() === 'success') {

        this.serviceState.resetStatus();
        this.modalDelete = false;

        this.snackBar.open(this.serviceState.deleteMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-success']
        });
      }

      if (this.serviceState.deleteStatus() === 'error') {

        this.serviceState.resetStatus();

        this.snackBar.open(this.serviceState.deleteMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-error']
        });
      }
    });
  }

  // ==================== FORMULÁRIO DE REGISTRO ====================

  /** Formulário de registro de serviço */
  public registerForm!: FormGroup;

  /**
   * Inicializa o formulário de registro
   * Campos: nome, código, descrição e departamento
   */
  initializeRegisterForm() {

    this.registerForm = this.fb.group({
      name: [''],
      code: [''],
      description: [''],
      departmentName: ['']
    });
  }

  /**
   * Inicializa o formulário de atualização
   * Campos: nome, código, descrição, departamento e status ativo
   */
  initializeUpdateForm() {

    this.updateForm = this.fb.group({
      name: [''],
      code: [''],
      description: [''],
      departmentName: [''],
      active: [false]
    });
  }

  // ==================== MÉTODOS DE CONTROLE DE DROPDOWN ====================

  /**
   * Fecha dropdown ao clicar fora dele
   * Listener global de clique no documento
   * @param event - Evento de clique
   */
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {

    const target = event.target as HTMLElement;

    // Verifica se clicou dentro do menu dropdown
    const clickedInsideDropdown = target.closest('.button-menu-table')
      || target.closest('.drop-down-delete');

    if (!clickedInsideDropdown) {
      this.closeDropDown();
    }
  }

  /**
   * Abre ou fecha dropdown de um item específico
   * @param index - Índice do item
   */
  openDropDown(index: number) {

    if (this.dropDown === index) {
      this.dropDown = null;
      return;
    }

    this.dropDown = index;
  }

  /**
   * Fecha qualquer dropdown aberto
   */
  closeDropDown() {
    this.dropDown = null;
  }

  // ==================== MÉTODOS DE CRUD - REGISTRO ====================

  /**
   * Registra um novo serviço
   * Verifica validade do formulário antes de enviar
   */
  registerServiceManagement() {
    if (this.registerForm.invalid) return;
    this.serviceState.registerServiceManagenent(this.registerForm.value);
  }

  // ==================== MÉTODOS DE CRUD - ATUALIZAÇÃO ====================

  /**
   * Atualiza um serviço existente
   * Combina o ID do serviço com os dados do formulário
   */
  updateServiceManagement() {
    if (this.updateForm.invalid) return;
    this.serviceState.updateServiceManagement(
      { serviceManagementId: this.serviceInfo()?.serviceManagementId, ...this.updateForm.value }
    );
  }

  // ==================== MÉTODOS DE CRUD - EXCLUSÃO ====================

  /**
   * Exclui um serviço
   * @param serviceManagementId - ID do serviço
   */
  deleteServiceManagement(serviceManagementId: string) {
    if(serviceManagementId === '') return;
    this.serviceState.deleteService(serviceManagementId);
  }

  // ==================== MÉTODOS DE CONTROLE DE MODAIS ====================

  /**
   * Abre modal de registro de serviço
   * Carrega nomes de departamentos e inicializa formulário
   */
  openModalRegister() {
    this.initializeRegisterForm();
    this.deparmentState.loadNamesOfDepartments();
    this.modalRegister.set(true);
  }

  /**
   * Fecha modal de registro de serviço
   * Reseta lista de nomes de departamentos
   */
  closeModalRegister() {
    this.modalRegister.set(false);
    this.deparmentState.resetDepartmentNames();
  }

  /**
   * Abre modal de atualização de serviço
   * Busca dados do serviço, carrega departamentos e inicializa formulário
   * @param serviceManagementId - ID do serviço
   */
  openModalUpdate(serviceManagementId: string) {
    this.initializeUpdateForm();
    this.serviceState.getInfoService(serviceManagementId);
    this.modalUpdate = true;
    this.deparmentState.loadNamesOfDepartments();
  }

  /**
   * Fecha modal de atualização de serviço
   * Reseta informações do serviço
   */
  closeModalUpdate() {
    this.modalUpdate = false;
    this.serviceState.resetInfoService();
  }

  /**
   * Abre modal de exclusão de serviço
   * Busca dados do serviço e fecha dropdown
   * @param serviceManagementId - ID do serviço
   */
  openModalDelete(serviceManagementId: string) {
    this.serviceState.getInfoService(serviceManagementId);
    this.dropDown = null;
    this.modalDelete = true;
  }

  /**
   * Fecha modal de exclusão de serviço
   * Reseta informações do serviço
   */
  closeModalDelete() {
    this.modalDelete = false;
    this.serviceState.resetInfoService();
  }

  /**
   * Abre modal de visualização de serviço
   * Busca dados do serviço e garante que modal de descrição esteja fechado
   * @param serviceManagementId - ID do serviço
   */
  openModalView(serviceManagementId: string) {
    this.serviceState.getInfoService(serviceManagementId);
    this.modalView = true;
    this.modalDescription = false;
  }

  /**
   * Fecha modal de visualização de serviço
   * Reseta informações do serviço
   */
  closeModalView() {
    this.modalView = false;
    this.serviceState.resetInfoService();
  }

  /**
   * Abre modal de visualização de descrição
   * Busca dados do serviço
   * @param serviceManagementId - ID do serviço
   */
  openModalViewDescription(serviceManagementId: string) {
    this.modalDescription = true;
    this.serviceState.getInfoService(serviceManagementId);
  }

  /**
   * Fecha modal de visualização de descrição
   */
  closeModalViewDescription() {
    this.modalDescription = false;
  }

  // ==================== MÉTODOS DE BUSCA ====================

  /**
   * Executa busca ao digitar no campo de pesquisa
   * Atualiza o termo de busca no service
   * @param event - Evento do input
   */
  onSearch(event: any) {
    this.serviceState.setSearch(event.target.value);
  }

  // ==================== MÉTODOS DE PAGINAÇÃO ====================

  /**
   * Avança para a próxima página
   */
  nextPage() {
    this.serviceState.nextPage();
  }

  /**
   * Volta para a página anterior
   */
  previousPage() {
    this.serviceState.previousPage();
  }

  /**
   * Navega para uma página específica
   * @param page - Número da página (base 0)
   */
  goToPage(page: number) {
    this.serviceState.goToPage(page);
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
    return Math.min(
      (this.page() + 1) * this.itemsPerPage,
      this.totalElements()
    );
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

    // Ajusta início para não ultrapassar limite
    if (start < 0) {
      start = 0;
      end = Math.min(maxVisible, total);
    }

    // Ajusta final para não ultrapassar limite
    if (end > total) {
      end = total;
      start = Math.max(0, total - maxVisible);
    }

    return Array.from(
      { length: end - start },
      (_, i) => start + i
    );
  }
}
