import { Component, effect, HostListener, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DepartmentStateService } from '../../services/states/department/department-state.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { FormBuilder, FormGroup } from '@angular/forms';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-table-departments',
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './table-departments.component.html',
  styleUrl: './table-departments.component.css'
})
export class TableDepartmentsComponent implements OnInit {

  // ==================== INJEÇÕES DE DEPENDÊNCIA ====================

  /** Service para gerenciar estado do departamento */
  private departmentState = inject(DepartmentStateService);

  /** Construtor de formulários reativos */
  private fb = inject(FormBuilder);

  /** Service para exibir notificações toast */
  private snackBar = inject(MatSnackBar);

  // ==================== ESTADOS - DEPARTAMENTO ====================

  /** Lista de departamentos */
  public departments = this.departmentState.departments;

  /** Informações detalhadas de um departamento */
  public departmentInfo = this.departmentState.departmentInfo;

  /** Total de departamentos cadastrados */
  public totalDepartments = this.departmentState.countTotalDepartment;

  /** Percentual de serviços por departamento */
  public percentageByDepartment = this.departmentState.getPercentagesByDepartment;

  /** Página atual da listagem */
  public page = this.departmentState.page;

  /** Total de páginas */
  public totalPages = this.departmentState.totalPages;

  /** Total de elementos */
  public totalElements = this.departmentState.totalElements;

  // Loads
  public allDepartmentsLoading = this.departmentState.allDepartmentsLoading;
  public registerLoading = this.departmentState.registerLoading;
  public updateLoading = this.departmentState.updateLoading;
  public deleteLoading = this.departmentState.deleteLoading;

  // ==================== VARIÁVEIS DE CONTROLE ====================

  /** Quantidade de itens por página */
  private itemsPerPage = 4;

  // ==================== CONTROLE DE MODAIS ====================

  /** Índice do dropdown ativo (null quando nenhum está aberto) */
  public dropDown: number | null = null;

  /** Modal de registro de departamento */
  public modalRegister = this.departmentState.modalRegister;

  /** Modal de atualização de departamento */
  public modalUpdate: boolean = false;

  /** Modal de exclusão de departamento */
  public modalDelete: boolean = false;

  /** Modal de visualização de departamento */
  public modalView: boolean = false;

  /** Modal de visualização de descrição */
  public modalDescription: boolean = false;

  // ==================== ESTADOS DE RESPONSIVIDADE ====================

  /** Indica se está em dispositivo móvel (largura < 768px) */
  isMobile = signal(window.innerWidth < 768);

  // ==================== FORMULÁRIO DE REGISTRO ====================

  /** Formulário de registro de departamento */
  public registerForm!: FormGroup;

  /**
   * Inicializa o formulário de registro
   * Campos: nome e descrição
   */
  initializeRegisterForm() {
    this.registerForm = this.fb.group({
      name: [''],
      description: ['']
    });
  }

  // ==================== FORMULÁRIO DE ATUALIZAÇÃO ====================

  /** Formulário de atualização de departamento */
  public updateForm!: FormGroup;

  /**
   * Inicializa o formulário de atualização
   * Campos: nome, descrição e status ativo
   */
  initializeUpdateForm() {
    this.updateForm = this.fb.group({
      name: [''],
      description: [''],
      active: [false]
    });
  }

  // ==================== CICLO DE VIDA ====================

  /**
   * Inicializa o componente
   * - Carrega lista de departamentos
   * - Carrega estatísticas
   */
  ngOnInit() {
    this.departmentState.loadDepartments();
    this.departmentState.loadStatistics();
  }

  // ==================== CONSTRUTOR ====================

  constructor() {

    /**
     * Efeito: Preenche formulário de atualização quando departamento é selecionado
     * Atualiza os campos do formulário com os dados do departamento
     */
    effect(() => {

      const department = this.departmentInfo();

      if (this.modalUpdate && this.departmentInfo() !== null) {

        this.updateForm.patchValue({
          name: department?.name,
          description: department?.description,
          active: department?.active
        });
      }
    })

    /**
     * Efeito: Monitora status de registro de departamento
     * - Sucesso: exibe mensagem positiva, fecha modal e reseta status
     * - Erro: exibe mensagem de erro
     */
    effect(() => {

      if (this.departmentState.registerStatus() === 'success') {

        this.departmentState.resetStatus();
        this.closeModalRegister();

        this.snackBar.open(this.departmentState.registerMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-success']
        });
      }

      if (this.departmentState.registerStatus() === 'error') {

        this.departmentState.resetStatus();

        this.snackBar.open(this.departmentState.registerMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-error']
        });
      }
    });

    /**
     * Efeito: Monitora status de atualização de departamento
     * - Sucesso: exibe mensagem positiva, fecha modal e reseta status
     * - Erro: exibe mensagem de erro
     */
    effect(() => {

      if (this.departmentState.updateStatus() === 'success') {

        this.departmentState.resetStatus();
        this.closeModalUpdate();

        this.snackBar.open(this.departmentState.updateMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-success']
        });
      }

      if (this.departmentState.updateStatus() === 'error') {

        this.departmentState.resetStatus();

        this.snackBar.open(this.departmentState.updateMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-error']
        });
      }
    });

    /**
     * Efeito: Monitora status de exclusão de departamento
     * - Sucesso: exibe mensagem positiva, fecha modal e reseta status
     * - Erro: exibe mensagem de erro
     */
    effect(() => {

      if (this.departmentState.deleteStatus() === 'success') {

        this.departmentState.resetStatus();
        this.closeModalDelete();

        this.snackBar.open(this.departmentState.deleteMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-success']
        });
      }

      if (this.departmentState.deleteStatus() === 'error') {

        this.departmentState.resetStatus();

        this.snackBar.open(this.departmentState.deleteMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-error']
        });
      }
    });
  }

  // ==================== MÉTODOS DE CONTROLE DE MODAIS ====================

  /**
   * Abre modal de registro de departamento
   * Inicializa o formulário de registro
   */
  openModalRegister() {
    this.initializeRegisterForm();
    this.modalRegister.set(true);
  }

  /**
   * Fecha modal de registro de departamento
   */
  closeModalRegister() {
    this.modalRegister.set(false);
  }

  /**
   * Abre modal de atualização de departamento
   * Busca dados do departamento e inicializa formulário
   * @param departmentId - ID do departamento
   */
  openModalUpdate(departmentId: string) {
    this.initializeUpdateForm();
    this.departmentState.getInfoDepartment(departmentId);
    this.modalUpdate = true;
  }

  /**
   * Fecha modal de atualização de departamento
   * Reseta informações do departamento
   */
  closeModalUpdate() {
    this.modalUpdate = false;
    this.departmentState.resetDepartmentInfo();
  }

  /**
   * Abre modal de exclusão de departamento
   * Busca dados do departamento e fecha dropdown
   * @param departmentId - ID do departamento
   */
  openModalDelete(departmentId: string) {
    this.departmentState.getInfoDepartment(departmentId);
    this.dropDown = null;
    this.modalDelete = true;
  }

  /**
   * Fecha modal de exclusão de departamento
   * Reseta informações do departamento
   */
  closeModalDelete() {
    this.modalDelete = false;
    this.departmentState.resetDepartmentInfo();
  }

  /**
   * Abre modal de visualização de departamento
   * Busca dados do departamento
   * @param departmentId - ID do departamento
   */
  openModalView(departmentId: string) {
    this.departmentState.getInfoDepartment(departmentId);
    this.modalView = true;
  }

  /**
   * Fecha modal de visualização de departamento
   * Reseta informações do departamento
   */
  closeModalView() {
    this.modalView = false;
    this.departmentState.resetDepartmentInfo();
  }

  /**
   * Abre modal de visualização de descrição
   * Busca dados do departamento
   * @param departmentId - ID do departamento
   */
  openModalViewDescription(departmentId: string) {
    this.modalDescription = true;
    this.departmentState.getInfoDepartment(departmentId);
  }

  /**
   * Fecha modal de visualização de descrição
   */
  closeModalViewDescription() {
    this.modalDescription = false;
  }

  // ==================== MÉTODOS DE CRUD ====================

  /**
   * Registra um novo departamento
   * Verifica validade do formulário antes de enviar
   */
  registerDepartment() {
    if (this.registerForm.invalid) return;
    this.departmentState.createDepartment(this.registerForm.value);
  }

  /**
   * Atualiza um departamento existente
   * Combina o ID do departamento com os dados do formulário
   */
  updateDepartment() {
    if (this.updateForm.invalid) return;
    this.departmentState.updateDepartment(
      {departmentId: this.departmentState.departmentInfo()?.departmentId, ...this.updateForm.value });
  }

  /**
   * Exclui um departamento
   * @param departmentId - ID do departamento
   */
  deleteDepartment(departmentId: string) {
    if (departmentId === '') return;
    this.departmentState.deleteDepartment(departmentId);
  }

  // ==================== MÉTODOS DE BUSCA ====================

  /**
   * Executa busca ao digitar no campo de pesquisa
   * Atualiza o termo de busca no service
   * @param event - Evento do input
   */
  onSearch(event: any) {
    this.departmentState.setSearch(event.target.value);
  }

  // ==================== MÉTODOS DE PAGINAÇÃO ====================

  /**
   * Avança para a próxima página
   */
  nextPage() {
    this.departmentState.nextPage();
  }

  /**
   * Volta para a página anterior
   */
  previousPage() {
    this.departmentState.previousPage();
  }

  /**
   * Navega para uma página específica
   * @param page - Número da página (base 0)
   */
  goToPage(page: number) {
    this.departmentState.goToPage(page);
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
