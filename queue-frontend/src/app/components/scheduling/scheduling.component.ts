import { Component, effect, HostListener, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CustomerStateService } from '../../services/states/customer/customer-state.service';
import { ScheduleStateService } from '../../services/states/scheduling/scheduling-state.service';
import { ServiceManagementService } from '../../services/states/serviceManagement/service-management.service';
import { TicketStateService } from '../../services/states/ticket/ticket-state.service';
import { ResponseAllCustomersDto } from '../../dtos/customer/ResponseAllCustomersDto';
import { NgxMaskDirective } from 'ngx-mask';

@Component({
  selector: 'app-scheduling',
  imports: [CommonModule, FormsModule, ReactiveFormsModule, NgxMaskDirective],
  templateUrl: './scheduling.component.html',
  styleUrl: './scheduling.component.css'
})
export class SchedulingComponent implements OnInit {

  /**
   * Inicializa o componente carregando dados iniciais
   * - Carrega lista de clientes
   * - Carrega lista de agendamentos
   */
  ngOnInit() {
    this.customerState.loadCustomers();
    this.schedulingState.loadSchedules();
  }

  // ==================== INJEÇÕES DE DEPENDÊNCIA ====================

  /** Service para gerenciar estado do cliente */
  private customerState = inject(CustomerStateService);

  /** Service para gerenciar estado do agendamento */
  private schedulingState = inject(ScheduleStateService);

  /** Service para gerenciar estado do serviço */
  private serviceState = inject(ServiceManagementService);

  /** Service para gerenciar estado do ticket */
  private ticketState = inject(TicketStateService);

  /** Construtor de formulários reativos */
  private fb = inject(FormBuilder);

  /** Service para exibir notificações toast */
  private snackBar = inject(MatSnackBar);

  // ==================== VARIÁVEIS DE CONTROLE ====================

  /** Índice do dropdown ativo (null quando nenhum está aberto) */
  dropDown: number | null = null;

  /** Quantidade de itens por página na tabela */
  itemsPerPage = 4;

  /** Tabela atualmente exibida (Scheduling ou Customer) */
  table = this.schedulingState.table;

  /** Data atual do sistema */
  currentDate = new Date();

  /** Input de busca por cliente */
  searchCustomerInput = '';

  /** Input de busca para atualização de cliente */
  updateCustomerSearch = '';

  /** Data selecionada para filtro (formato ISO) */
  selectedDate = new Date().toLocaleDateString('en-CA');

  // ==================== CONTROLE DE MODAIS - AGENDAMENTO ====================

  /** Modal de registro de agendamento */
  modalSchedulingRegister = this.schedulingState.modalSchedulingRegister;

  /** Modal de atualização de agendamento */
  modalSchedulingUpdate = false;

  /** Modal de exclusão de agendamento */
  modalSchedulingDelete = false;

  /** Modal de visualização de agendamento */
  modalSchedulingView = false;

  // ==================== CONTROLE DE MODAIS - CLIENTE ====================

  /** Modal de registro de cliente */
  modalCustomerRegister = this.schedulingState.modalCustomerRegister;

  /** Modal de atualização de cliente */
  modalCustomerUpdate = false;

  /** Modal de exclusão de cliente */
  modalCustomerDelete = false;

  /** Modal de visualização de cliente */
  modalCustomerView = false;

  // ==================== CONTROLE DE MODAIS - TICKET ====================

  /** Modal de criação de ticket */
  modalTicket = false;

  /** Modal de impressão de ticket */
  modalTicketPrinting = false;

  // ==================== FORMULÁRIOS ====================

  /** Formulário de registro de cliente */
  registerCustomerForm!: FormGroup;

  /** Formulário de atualização de cliente */
  updateCustomerForm!: FormGroup;

  /** Formulário de registro de agendamento */
  registerScheduleForm!: FormGroup;

  /** Formulário de atualização de agendamento */
  updateScheduleForm!: FormGroup;

  // ==================== ESTADOS - SERVIÇOS ====================

  /** Lista de nomes de serviços e departamentos */
  public serviceNamesAndDepartments = this.serviceState.serviceNamesAndDepartments;

  // ==================== ESTADOS - CLIENTES ====================

  /** Lista de clientes */
  public customers = this.customerState.customers;

  /** Informações detalhadas de um cliente */
  public customerInfo = this.customerState.customerInfo;

  /** Página atual da listagem de clientes */
  public customerPage = this.customerState.customerPage;

  /** Total de páginas de clientes */
  public customerTotalPages = this.customerState.customerTotalPages;

  /** Total de elementos de clientes */
  public customerTotalElements = this.customerState.customerTotalElements;

  /** Termo de busca de clientes */
  public customerSearch = this.customerState.customerSearch;

  /** Lista de IDs e nomes de clientes */
  public customerIdsAndNames = this.customerState.customerIdsAndNames;

  /** Sugestões de clientes para autocomplete */
  public customerSuggestions = this.customerState.customerSuggestions;

  // ==================== ESTADOS - AGENDAMENTOS ====================

  /** Lista de agendamentos */
  public schedules = this.schedulingState.schedules;

  /** Informações detalhadas de um agendamento */
  public scheduleInfo = this.schedulingState.scheduleInfo;

  /** Página atual da listagem de agendamentos */
  public schedulePage = this.schedulingState.schedulePage;

  /** Total de páginas de agendamentos */
  public scheduleTotalPages = this.schedulingState.scheduleTotalPages;

  /** Total de elementos de agendamentos */
  public scheduleTotalElements = this.schedulingState.scheduleTotalElements;

  /** Termo de busca de agendamentos */
  public scheduleSearch = this.schedulingState.scheduleSearch;

  /** Agendamentos criados por dia */
  public scheduleCreatedByDay = this.schedulingState.scheduleCreatedByDay;

  // ==================== ESTADOS - TICKETS ====================

  /** Informações detalhadas de um ticket */
  public ticketInfo = this.ticketState.ticketInfo;

  // ==================== ESTADOS LOCAIS ====================

  /** ID do cliente selecionado */
  public customerId = signal<string>('');

  /** ID do serviço selecionado */
  public serviceManagementId = signal<string>('');

  constructor() {

    /**
     * Efeito: Recarrega agendamentos quando a tabela é alterada para Scheduling
     */
    effect(() => {
      if (this.table() === 'Scheduling') {
        this.schedulingState.loadSchedules();
      }
    })

    // ==================== INICIALIZAÇÃO DOS FORMULÁRIOS ====================

    /**
     * Formulário de registro de agendamento
     */
    this.registerScheduleForm = this.fb.group({
      customerId: [''],           // ID do cliente
      serviceManagementId: [''],  // ID do serviço
      priority: [''],             // Prioridade (NORMAL, PRIORITY)
      scheduledDate: [''],        // Data agendada
    });

    /**
     * Formulário de atualização de agendamento
     */
    this.updateScheduleForm = this.fb.group({
      scheduleId: [''],           // ID do agendamento
      customerId: [''],           // ID do cliente
      serviceManagementId: [''],  // ID do serviço
      priority: [''],             // Prioridade
      scheduledDate: [''],        // Data agendada
      status: ['']                // Status (SCHEDULED, PRESENT, CANCELED, ABSENT)
    });

    /**
     * Formulário de registro de cliente
     */
    this.registerCustomerForm = this.fb.group({
      name: [''],      // Nome do cliente
      cpf: [''],       // CPF
      rg: [''],        // RG
      email: [''],     // E-mail
      phone: [''],     // Telefone
    });

    /**
     * Formulário de atualização de cliente
     */
    this.updateCustomerForm = this.fb.group({
      customerId: [''], // ID do cliente
      name: [''],       // Nome
      cpf: [''],        // CPF
      rg: [''],         // RG
      email: [''],      // E-mail
      phone: [''],      // Telefone
    });

    // ==================== EFEITOS DE REATIVIDADE ====================

    /**
     * Efeito: Preenche formulário de atualização quando agendamento é selecionado
     * Também busca o nome do cliente para exibição
     */
    effect(() => {

      const schedule = this.scheduleInfo();
      const customers = this.customerIdsAndNames();

      if (!schedule) return;

      this.updateScheduleForm.patchValue({
        scheduleId: schedule.scheduleId,
        customerId: schedule.customerId,
        serviceManagementId: schedule.serviceManagementId,
        priority: schedule.priority,
        scheduledDate: schedule.scheduledDate,
        status: schedule.status
      });

      const customer = customers.find(
        c => c.customerId === schedule.customerId
      );

      this.updateCustomerSearch = customer?.name ?? '';

    });

    /**
     * Efeito: Preenche formulário de atualização quando cliente é selecionado
     */
    effect(() => {
      if (this.customerInfo() !== null) {
        this.updateCustomerForm.patchValue({
          customerId: this.customerInfo()?.customerId,
          name: this.customerInfo()?.name,
          cpf: this.customerInfo()?.cpf,
          rg: this.customerInfo()?.rg,
          email: this.customerInfo()?.email,
          phone: this.customerInfo()?.phone,
        });
      }
    })

    /**
     * Efeito: Monitora status de registro de agendamento
     * - Sucesso: exibe mensagem, fecha modal e reseta formulário
     * - Erro: exibe mensagem de erro
     */
    effect(() => {

      if (this.schedulingState.registerStatus() === 'success') {
        this.snackBar.open(this.schedulingState.registerMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-success']
        });
        this.modalSchedulingRegister.set(false);
        this.registerScheduleForm.reset();
        this.schedulingState.resetStatus();
      }

      if (this.schedulingState.registerStatus() === 'error') {
        this.snackBar.open(this.schedulingState.registerMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-error']
        });
        this.schedulingState.resetStatus();
      }
    })

    /**
     * Efeito: Monitora status de atualização de agendamento
     * - Sucesso: exibe mensagem, fecha modal, reseta formulários e limpa dados
     * - Erro: exibe mensagem de erro
     */
    effect(() => {

      if (this.schedulingState.updateStatus() === 'success') {
        this.snackBar.open(this.schedulingState.updateMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-success']
        });
        this.schedulingState.resetStatus();
        this.updateScheduleForm.reset();
        this.serviceNamesAndDepartments.set([]);
        this.customerIdsAndNames.set([]);
        this.modalSchedulingUpdate = false;
        this.schedulingState.resetScheduleInfo();
      }

      if (this.schedulingState.updateStatus() === 'error') {
        this.snackBar.open(this.schedulingState.updateMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-error']
        });
        this.schedulingState.resetStatus();
      }
    })

    /**
     * Efeito: Monitora status de registro de cliente
     * - Sucesso: exibe mensagem, fecha modal e reseta formulário
     * - Erro: exibe mensagem de erro
     */
    effect(() => {
      if (this.customerState.registerCustomerStatus() === 'success') {
        this.snackBar.open(this.customerState.registerCustomerMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-success']
        });
        this.modalCustomerRegister.set(false);
        this.registerCustomerForm.reset();
        this.customerState.resetStatus();
      }

      if (this.customerState.registerCustomerStatus() === 'error') {
        this.snackBar.open(this.customerState.registerCustomerMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-error']
        });
        this.customerState.resetStatus();
      }
    });

    /**
     * Efeito: Monitora status de atualização de cliente
     * - Sucesso: exibe mensagem, fecha modal e reseta dados
     * - Erro: exibe mensagem de erro
     */
    effect(() => {
      if (this.customerState.updateCustomerStatus() === 'success') {
        this.snackBar.open(this.customerState.updateCustomerMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-success']
        });
        this.modalCustomerUpdate = false;
        this.customerState.resetCustomerInfo();
        this.customerState.resetStatus();
      }

      if (this.customerState.updateCustomerStatus() === 'error') {
        this.snackBar.open(this.customerState.updateCustomerMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-error']
        });
        this.customerState.resetStatus();
      }
    });

    /**
     * Efeito: Monitora status de exclusão de agendamento
     * - Sucesso: exibe mensagem, fecha modal e reseta dados
     * - Erro: exibe mensagem de erro
     */
    effect(() => {
      if (this.schedulingState.deleteStatus() === 'success') {
        this.snackBar.open(this.schedulingState.deleteMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-success']
        });
        this.modalSchedulingDelete = false;
        this.schedulingState.resetStatus();
        this.schedulingState.resetScheduleInfo();
      }

      if (this.schedulingState.deleteStatus() === 'error') {
        this.snackBar.open(this.schedulingState.deleteMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-error']
        });
        this.schedulingState.resetStatus();
      }
    });

    /**
     * Efeito: Monitora status de exclusão de cliente
     * - Sucesso: exibe mensagem, fecha modal e reseta dados
     * - Erro: exibe mensagem de erro
     */
    effect(() => {
      if (this.customerState.deleteCustomerStatus() === 'success') {
        this.snackBar.open(this.customerState.deleteCustomerMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-success']
        });
        this.modalCustomerDelete = false;
        this.customerState.resetStatus();
        this.customerState.resetCustomerInfo();
      }

      if (this.customerState.deleteCustomerStatus() === 'error') {
        this.snackBar.open(this.customerState.deleteCustomerMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-error']
        });
        this.customerState.resetStatus();
      }
    });

    /**
     * Efeito: Monitora status de criação de ticket
     * - Sucesso: exibe mensagem, fecha modal e abre impressão
     * - Erro: exibe mensagem de erro
     */
    effect(() => {

      if (this.ticketState.createStatus() === 'success') {
        this.snackBar.open(this.ticketState.createMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-success']
        });
        this.ticketState.resetStatus();
        this.modalTicket = false;
        this.modalTicketPrinting = true;
        this.printTicket();
      }

      if (this.ticketState.createStatus() === 'error') {
        this.snackBar.open(this.ticketState.createMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-error']
        });
        this.ticketState.resetStatus();
      }
    });

    /**
     * Efeito: Monitora status de exclusão de ticket
     * - Sucesso: exibe mensagem e fecha modal
     * - Erro: exibe mensagem de erro
     */
    effect(() => {

      if (this.ticketState.deleteStatus() === 'success') {
        this.snackBar.open(this.ticketState.deleteMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-success']
        });
        this.ticketState.resetStatus();
        this.modalTicket = false;
      }

      if (this.ticketState.deleteStatus() === 'error') {
        this.snackBar.open(this.ticketState.deleteMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-error']
        });
        this.ticketState.resetStatus();
      }
    });
  }

  // ==================== MÉTODOS DE CONTROLE DE MODAIS - AGENDAMENTO ====================

  /**
   * Abre modal de registro de agendamento
   * Carrega listas de clientes e serviços para seleção
   */
  public openScheduleModalRegister() {
    this.modalSchedulingRegister.set(true);
    this.customerState.loadCustomerIdsAndNames();
    this.serviceState.loadServiceNamesAndDepartments();
  }

  /**
   * Fecha modal de registro de agendamento
   */
  public closeScheduleModalRegister() {
    this.modalSchedulingRegister.set(false);
  }

  /**
   * Abre modal de atualização de agendamento
   * Busca dados do agendamento e carrega listas auxiliares
   * @param scheduleId - ID do agendamento a ser atualizado
   */
  public openScheduleModalUpdate(scheduleId: string) {
    this.schedulingState.getScheduleById(scheduleId);
    this.customerState.loadCustomerIdsAndNames();
    this.serviceState.loadServiceNamesAndDepartments();
    this.modalSchedulingUpdate = true;
  }

  /**
   * Fecha modal de atualização de agendamento
   * Reseta formulário e limpa dados temporários
   */
  public closeScheduleModalUpdate() {
    this.modalSchedulingUpdate = false;
    this.updateScheduleForm.reset();
    this.customerIdsAndNames.set([]);
    this.scheduleInfo.set(null);
    this.updateCustomerSearch = '';
  }

  /**
   * Abre modal de exclusão de agendamento
   * Busca dados do agendamento e fecha dropdown
   * @param scheduleId - ID do agendamento a ser excluído
   */
  public openScheduleModalDelete(scheduleId: string) {
    this.schedulingState.getScheduleById(scheduleId);
    this.modalSchedulingDelete = true;
    this.closeDropDown();
  }

  /**
   * Fecha modal de exclusão de agendamento
   */
  public closeScheduleModalDelete() {
    this.modalSchedulingDelete = false;
    this.scheduleInfo.set(null);
  }

  /**
   * Abre modal de visualização de agendamento
   * Busca dados do agendamento e do cliente
   * @param scheduleId - ID do agendamento
   * @param customerId - ID do cliente
   */
  public openScheduleModalView(scheduleId: string, customerId: string) {
    this.customerState.getInfoCustomer(customerId);
    this.schedulingState.getScheduleById(scheduleId);
    this.modalSchedulingView = true;
  }

  /**
   * Fecha modal de visualização de agendamento
   */
  public closeScheduleModalView() {
    this.modalSchedulingView = false;
    this.scheduleInfo.set(null);
  }

  // ==================== MÉTODOS DE CONTROLE DE MODAIS - CLIENTE ====================

  /**
   * Abre modal de registro de cliente
   */
  public openCustomerModalRegister() {
    this.modalCustomerRegister.set(true);
  }

  /**
   * Fecha modal de registro de cliente
   */
  public closeCustomerModalRegister() {
    this.modalCustomerRegister.set(false);
  }

  /**
   * Abre modal de atualização de cliente
   * @param customerId - ID do cliente a ser atualizado
   */
  public openCustomerModalUpdate(customerId: string) {
    this.customerState.getInfoCustomer(customerId);
    this.modalCustomerUpdate = true;
  }

  /**
   * Fecha modal de atualização de cliente
   */
  public closeCustomerModalUpdate() {
    this.modalCustomerUpdate = false;
    this.customerState.resetCustomerInfo();
  }

  /**
   * Abre modal de exclusão de cliente
   * Fecha dropdown após abertura
   * @param customerId - ID do cliente a ser excluído
   */
  public openCustomerModalDelete(customerId: string) {
    this.modalCustomerDelete = true;
    this.customerState.getInfoCustomer(customerId);
    this.closeDropDown();
  }

  /**
   * Fecha modal de exclusão de cliente
   */
  public closeCustomerModalDelete() {
    this.modalCustomerDelete = false;
    this.customerState.resetCustomerInfo();
  }

  /**
   * Abre modal de visualização de cliente
   * @param customerId - ID do cliente
   */
  public openCustomerModalView(customerId: string) {
    this.customerState.getInfoCustomer(customerId);
    this.modalCustomerView = true;
  }

  /**
   * Fecha modal de visualização de cliente
   */
  public closeCustomerModalView() {
    this.modalCustomerView = false;
    this.customerState.resetCustomerInfo();
  }

  // ==================== MÉTODOS DE CONTROLE DE MODAIS - TICKET ====================

  /**
   * Abre modal de criação de ticket para um agendamento
   * Busca dados do agendamento e fecha dropdown
   * @param scheduleId - ID do agendamento
   */
  public openTicketModal(scheduleId: string) {
    this.modalTicket = true;
    this.schedulingState.getScheduleById(scheduleId);
    this.closeDropDown();
  }

  /**
   * Fecha modal de criação de ticket
   */
  public closeTicketModal() {
    this.modalTicket = false;
  }

  /**
   * Fecha modal de impressão de ticket
   */
  public closeTicketPrintingModal() {
    this.modalTicketPrinting = false;
  }

  // ==================== MÉTODOS DE BUSCA E FILTRO ====================

  /**
   * Executa busca ao digitar no campo de pesquisa
   * Atualiza o termo de busca no service correspondente
   * @param event - Evento do input
   */
  onSearch(event: any) {
    if (this.table() === 'Scheduling') {
      this.schedulingState.setSearch(event.target.value);
    } else {
      this.customerState.setSearch(event.target.value);
    }
  }

  /**
   * Aplica filtro por data na tabela de agendamentos
   * @param event - Evento do input date
   */
  onDateFilter(event: any) {
    if (this.table() === 'Scheduling') {
      this.schedulingState.setSearchDate(event.target.value);
    }
  }

  // ==================== MÉTODOS DE PAGINAÇÃO ====================

  /**
   * Avança para a próxima página
   */
  nextPage() {
    if (this.table() === 'Scheduling') {
      this.schedulingState.nextPage();
    } else {
      this.customerState.nextPage();
    }
  }

  /**
   * Volta para a página anterior
   */
  previousPage() {
    if (this.table() === 'Scheduling') {
      this.schedulingState.previousPage();
    } else {
      this.customerState.previousPage();
    }
  }

  /**
   * Navega para uma página específica
   * @param page - Número da página (base 0)
   */
  goToPage(page: number) {
    if (this.table() === 'Scheduling') {
      this.schedulingState.goToPage(page);
    } else {
      this.customerState.goToPage(page);
    }
  }

  /**
   * Calcula o índice inicial dos itens exibidos
   * @returns Índice inicial (base 1)
   */
  getStartIndex(): number {
    if (this.table() === 'Scheduling') {
      return this.schedulePage() * this.itemsPerPage + 1;
    } else {
      return this.customerPage() * this.itemsPerPage + 1;
    }
  }

  /**
   * Calcula o índice final dos itens exibidos
   * @returns Índice final
   */
  getEndIndex(): number {
    if (this.table() === 'Scheduling') {
      return Math.min(
        (this.schedulePage() + 1) * this.itemsPerPage,
        this.scheduleTotalElements()
      );
    } else {
      return Math.min(
        (this.customerPage() + 1) * this.itemsPerPage,
        this.customerTotalElements()
      );
    }
  }

  /**
   * Gera array de números de páginas visíveis (máximo 4)
   * @returns Array com números das páginas
   */
  getPagesArray(): number[] {
    let total: number;
    let current: number;

    if (this.table() === 'Scheduling') {
      total = this.scheduleTotalPages();
      current = this.schedulePage();
    } else {
      total = this.customerTotalPages();
      current = this.customerPage();
    }

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

  // ==================== MÉTODOS DE CRUD - AGENDAMENTO ====================

  /**
   * Registra um novo agendamento
   * Verifica validade do formulário antes de enviar
   */
  registerSchedule() {
    if (this.registerScheduleForm.invalid) return;
    this.schedulingState.createSchedule(this.registerScheduleForm.value);
  }

  /**
   * Atualiza um agendamento existente
   */
  updateSchedule() {
    this.schedulingState.updateSchedule(this.updateScheduleForm.value);
  }

  /**
   * Exclui um agendamento
   * @param scheduleId - ID do agendamento
   */
  deleteSchedule(scheduleId: string) {
    this.schedulingState.deleteSchedule(scheduleId);
  }

  // ==================== MÉTODOS DE CRUD - CLIENTE ====================

  /**
   * Registra um novo cliente
   * Verifica validade do formulário antes de enviar
   */
  registerCustomer() {
    if (this.registerCustomerForm.invalid) return;
    this.customerState.createCustomer(this.registerCustomerForm.value);
  }

  /**
   * Atualiza um cliente existente
   */
  updateCustomer() {
    if (this.updateCustomerForm.invalid) return;
    this.customerState.updateCustomer(this.updateCustomerForm.value);
  }

  /**
   * Exclui um cliente
   * @param customerId - ID do cliente
   */
  deleteCustomer(customerId: string) {
    this.customerState.deleteCustomer(customerId);
  }

  // ==================== MÉTODOS DE CRUD - TICKET ====================

  /**
   * Cria um ticket para o agendamento atual
   * Utiliza dados do scheduleInfo para preencher os campos
   */
  createTicket() {

    if (this.scheduleInfo() === null) return;

    this.ticketState.createTicket(
      {
        customerId: this.scheduleInfo()?.customerId ?? '',
        serviceManagementId: this.scheduleInfo()?.serviceManagementId ?? '',
        scheduleId: this.scheduleInfo()?.scheduleId ?? '',
        priority: this.scheduleInfo()?.priority ?? ''
      });
  }

  /**
   * Exclui um ticket
   * @param ticketId - ID do ticket
   */
  deleteTicket(ticketId: string) {

    if (ticketId === '') return;
    this.ticketState.deleteTicket(ticketId);
  }

  // ==================== MÉTODOS AUXILIARES ====================

  /**
   * Alterna a tabela ativa entre Scheduling e Customer
   * @param table - Nome da tabela
   */
  public handleTable(table: string) {
    this.table.set(table);
  }

  /**
   * Retorna o nome do status do agendamento em português
   * @param status - Status em inglês (SCHEDULED, PRESENT, CANCELED, ABSENT)
   * @returns Status em português
   */
  public getStatusSchedule(status: string) {
    if (status === 'SCHEDULED') return 'Agendado';
    if (status === 'PRESENT') return 'Presente';
    if (status === 'CANCELED') return 'Cancelado';
    if (status === 'ABSENT') return 'Ausente';
    return '';
  }

  /**
   * Retorna a classe CSS para o status do agendamento
   * @param status - Status em inglês
   * @returns Nome da classe CSS
   */
  public getStatusClass(status: string): string {
    if (status === 'SCHEDULED') return 'status-scheduled';
    if (status === 'PRESENT') return 'status-present';
    if (status === 'CANCELED') return 'status-canceled';
    if (status === 'ABSENT') return 'status-absent';
    return '';
  }

  /**
   * Inicia a impressão do ticket após delay
   * Utilizado para garantir que o modal seja renderizado
   */
  public printTicket(): void {
    setTimeout(() => {
      window.print();
    }, 100);
  }

  /**
   * Retorna o nome da prioridade em português
   * @param priority - Prioridade em inglês (NORMAL, PRIORITY)
   * @returns Nome da prioridade em português
   */
  public getNamePriority(priority: string) {
    if (priority === 'NORMAL') return 'Normal';
    if (priority === 'PRIORITY') return 'Prioridade';
    return '-';
  }

  /**
   * Retorna o primeiro documento disponível para exibição na tabela
   * Prioridade: CPF > RG > Telefone > E-mail
   * @param cpf - CPF do cliente
   * @param rg - RG do cliente
   * @param phone - Telefone do cliente
   * @param email - E-mail do cliente
   * @returns Documento disponível ou string vazia
   */
  public getDocumentForTableSchedule(cpf: string, rg: string, phone: string, email: string): string {

    if (cpf !== '') return cpf;
    if (rg !== '') return rg;
    if (phone !== '') return phone;
    if (email !== '') return email;
    return '';
  }

  // ==================== MÉTODOS DE AUTOCOMPLETE - CLIENTE ====================

  /**
   * Busca clientes para autocomplete no registro de agendamento
   * Atualiza sugestões quando o termo tem 2+ caracteres
   * @param event - Evento do input
   */
  onCustomerSearch(event: Event) {

    const value = (event.target as HTMLInputElement).value;

    this.searchCustomerInput = value;

    if (value.trim().length < 2) {
      this.customerSuggestions.set([]);
      return;
    }

    this.customerState.searchCustomers(value);
  }

  /**
   * Seleciona um cliente para o formulário de registro de agendamento
   * Preenche o input com o nome e o formulário com o ID
   * @param customer - Cliente selecionado
   */
  selectCustomer(customer: ResponseAllCustomersDto) {

    this.searchCustomerInput = customer.name;

    this.registerScheduleForm.patchValue({
      customerId: customer.customerId
    });

    this.updateScheduleForm.patchValue({
      customerId: customer.customerId
    });

    this.customerSuggestions.set([]);
  }

  /**
   * Busca clientes para autocomplete na atualização de agendamento
   * Atualiza sugestões quando o termo tem 2+ caracteres
   * @param event - Evento do input
   */
  onUpdateCustomerSearch(event: Event) {

    const value = (event.target as HTMLInputElement).value;

    this.updateCustomerSearch = value;

    if (value.trim().length < 2) {
      this.customerState.customerSuggestions.set([]);
      return;
    }

    this.customerState.searchCustomers(value);
  }

  /**
   * Seleciona um cliente para o formulário de atualização de agendamento
   * Preenche o input com o nome e o formulário com o ID
   * @param customer - Cliente selecionado
   */
  selectUpdateCustomer(customer: ResponseAllCustomersDto) {

    this.updateCustomerSearch = customer.name;

    this.updateScheduleForm.patchValue({
      customerId: customer.customerId
    });

    this.customerState.customerSuggestions.set([]);
  }
}
