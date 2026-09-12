import { Component, computed, effect, inject, OnInit, signal } from '@angular/core';
import { AttendentStateService } from '../../services/states/attendent/attendent-state.service';
import { CommonModule } from '@angular/common';
import { TicketStateService } from '../../services/states/ticket/ticket-state.service';
import { UserStateService } from '../../services/states/user/user-state.service';
import { ResponseTicketsForAttendanceDto } from '../../dtos/ticket/ResponseTicketsForAttendanceDto';
import { interval, Subscription } from 'rxjs';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { VoiceService } from '../../services/voice/voice.service';

@Component({
  selector: 'app-service-counter',
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './service-counter.component.html',
  styleUrl: './service-counter.component.css'
})
export class ServiceCounterComponent implements OnInit {

  // ==================== INJEÇÕES DE DEPENDÊNCIA ====================

  /** Service para gerenciar estado do atendente */
  private attendentState = inject(AttendentStateService);

  /** Service para gerenciar estado do ticket */
  private ticketState = inject(TicketStateService);

  /** Service para gerenciar estado do usuário */
  private userState = inject(UserStateService);

  /** Construtor de formulários reativos */
  private fb = inject(FormBuilder);

  /** Service para exibir notificações toast */
  private snackBar = inject(MatSnackBar);

  // ==================== ESTADOS - ATENDENTE ====================

  /** Total de atendimentos realizados */
  public countTotalAttendances = this.attendentState.countTotalAttendances;

  /** Tempo médio de serviço */
  public averageServiceTime = this.attendentState.averageServiceTime;

  /** Tempo médio de espera */
  public avarageWaitingTime = this.attendentState.averageWaitingTime;

  // ==================== ESTADOS - TICKET ====================

  /** Lista de tickets aguardando atendimento */
  public ticketsForAttendance = this.ticketState.ticketsForAttendance;

  /** Histórico de tickets do atendente */
  public historyTickets = this.ticketState.historyTickets;

  /** Total de tickets */
  public totalTickets = this.ticketState.totalTickets;

  // ==================== ESTADOS - ATENDENTE ====================

  /** Timer atual do atendimento em andamento */
  public currentTimer = this.attendentState.currentTimer;

  // ==================== ESTADOS LOCAIS ====================

  /** Índice do ticket atual na fila */
  public cont = signal(-1);

  // ==================== ESTADOS - USUÁRIO ====================

  /** Usuário atualmente logado */
  public userLogged = this.userState.userLogged;

  // ==================== FORMULÁRIOS ====================

  /** Formulário para finalizar atendimento com resolução */
  public finishForm!: FormGroup;

  // ==================== VARIÁVEIS DE CONTROLE ====================

  /** Horário de início do atendimento atual */
  startTime = new Date();

  /** Tempo decorrido formatado (HH:MM:SS) */
  date: string = '00:00:00';

  // ==================== CONTROLE DE MODAIS ====================

  /** Modal de cancelamento de atendimento */
  public modalCancelAttendance = false;

  /** Modal de finalização de atendimento */
  public modalFinishAttendance = false;

  // ==================== PAGINAÇÃO - TICKETS ====================

  /** Página atual da lista de tickets */
  public pageTickets = this.ticketState.pageTickets;

  /** Total de páginas de tickets */
  public totalPagesTickets = this.ticketState.totalPagesTickets;

  /** Total de elementos de tickets */
  public totalElementsTickets = this.ticketState.totalElementsTickets;

  // ==================== PAGINAÇÃO - HISTÓRICO ====================

  /** Página atual do histórico */
  public pageHistory = this.ticketState.pageHistory;

  /** Total de páginas do histórico */
  public totalPagesHistory = this.ticketState.totalPagesHistory;

  /** Total de elementos do histórico */
  public totalElementsHistory = this.ticketState.totalElementsHistory;

  /** Quantidade de itens por página */
  public itemsPerPage = 6;

  /** Subscription para o polling de atualizações */
  private pollingSubscription?: Subscription;

  /** ID do ticket atualmente selecionado */
  public ticketSelectedId = signal<string | null>(null);

  /**
   * Computed property que retorna o ticket selecionado
   * Busca na lista de tickets pelo ID armazenado
   */
  public ticketSelected = computed(() => {
    const id = this.ticketSelectedId();

    if (!id) {
      return null;
    }

    return (
      this.ticketsForAttendance().find(
        ticket => ticket.ticketId === id
      ) ?? null
    );
  });

  constructor() {

    // ==================== INICIALIZAÇÃO DO FORMULÁRIO ====================

    /** Formulário para finalizar atendimento com campo de resolução */
    this.finishForm = this.fb.group({
      resolution: ['']
    });

    // ==================== EFEITOS DE REATIVIDADE ====================

    /**
     * Efeito: Monitora status de início de atendimento
     * - Sucesso: exibe mensagem positiva
     * - Erro: exibe mensagem de erro
     */
    effect(() => {

      if (this.attendentState.startAttendanceStatus() === 'success') {

        this.snackBar.open(this.attendentState.startAttendanceMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-success']
        });

        this.attendentState.resetStatus();

      }


      if (this.attendentState.startAttendanceStatus() === 'error') {

        this.snackBar.open(this.attendentState.startAttendanceMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-error']
        });

        this.attendentState.resetStatus();
      }
    });

    /**
     * Efeito: Monitora status de finalização de atendimento
     * - Sucesso: exibe mensagem, limpa seleção e fecha modal
     * - Erro: exibe mensagem de erro
     */
    effect(() => {

      if (this.attendentState.finishAttendanceStatus() === 'success') {

        this.ticketState.getTicketsForAttendence();
        this.ticketState.getHistoryTicketsByAttendant();

        this.snackBar.open(this.attendentState.finishAttendanceMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-success']
        });

        this.ticketSelectedId.set(null);
        this.modalFinishAttendance = false;
        this.attendentState.resetStatus();

      }

      if (this.attendentState.finishAttendanceStatus() === 'error') {

        this.snackBar.open(this.attendentState.finishAttendanceMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-error']
        });

        this.attendentState.resetStatus();
      }
    });

    /**
     * Efeito: Monitora status de cancelamento de atendimento
     * - Sucesso: exibe mensagem, limpa seleção e fecha modal
     * - Erro: exibe mensagem de erro
     */
    effect(() => {

      if (this.attendentState.cancelAttendanceStatus() === 'success') {

        this.ticketState.getTicketsForAttendence();
        this.ticketState.getHistoryTicketsByAttendant();

        this.snackBar.open(this.attendentState.cancelAttendanceMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-success']
        });

        this.ticketSelectedId.set(null);
        this.modalCancelAttendance = false;
        this.attendentState.resetStatus();

      }

      if (this.attendentState.cancelAttendanceStatus() === 'error') {

        this.snackBar.open(this.attendentState.cancelAttendanceMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-error']
        });

        this.attendentState.resetStatus();
      }
    });

    /**
     * Efeito: Armazena ticket selecionado no localStorage para exibição no painel
     * Quando um ticket é selecionado, salva no localStorage para ser consumido pelo QueueDisplayComponent
     */
    effect(() => {

      const ticket = this.ticketSelected();

      if (ticket) {
        localStorage.setItem(
          'ticketForPanel',
          JSON.stringify(ticket)
        );
      }
    });

  }

  // ==================== CICLO DE VIDA ====================

  /**
   * Inicializa o componente
   * - Carrega estatísticas do atendente
   * - Busca dados do usuário logado
   * - Remove ticket do painel anterior
   * - Inicia polling para atualização automática
   */
  ngOnInit(): void {
    this.ticketState.getTicketsForAttendence();
    this.ticketState.getHistoryTicketsByAttendant();
    this.attendentState.loadStatistics();
    localStorage.removeItem('ticketForPanel');
  }

  /**
   * Destroi o componente e cancela subscription do polling
   */
  ngOnDestroy(): void {
    this.pollingSubscription?.unsubscribe();
  }

  // ==================== MÉTODOS AUXILIARES ====================

  /**
   * Retorna o nome da prioridade em português
   * @param priority - Prioridade em inglês (NORMAL, PRIORITY)
   * @returns Nome da prioridade em português
   */
  getNamePriority(priority: string):string {
    if (priority === 'NORMAL') return 'Normal';
    else if (priority === 'PRIORITY') return 'Prioridade';
    return '';
  }

  /**
   * Retorna o nome do status em português
   * @param status - Status em inglês (FINISHED, CANCELED)
   * @returns Nome do status em português
   */
  getNameStatus(status: string):string {
    if(status === 'FINISHED') return 'Finalizado';
    else if (status === 'CANCELED') return 'Cancelado';
    return ''
  }

  // ==================== MÉTODOS DE ATENDIMENTO ====================

  /**
   * Inicia o atendimento de um ticket
   * @param ticketId - ID do ticket a ser atendido
   */
  startAttendance(ticketId: string) {
    this.attendentState.startAttendance(ticketId);
  }

  /**
   * Avança para o próximo ticket na fila
   * @param tickets - Lista de tickets disponíveis
   */
  callNextTicket(tickets: ResponseTicketsForAttendanceDto[]) {

    if (tickets.length === 0) {
      return;
    }

    const nextIndex = this.cont() + 1;
    if (nextIndex >= tickets.length) {
      return;
    }

    const ticket = tickets[nextIndex];

    this.cont.set(nextIndex);
    this.ticketSelectedId.set(ticket.ticketId);
    this.ticketState.callTicket(ticket.ticketId);
  }

  /**
   * Volta para o ticket anterior na fila
   * @param tickets - Lista de tickets disponíveis
   */
  callBeforeTicket(tickets: ResponseTicketsForAttendanceDto[]) {

    if (tickets.length === 0) {
      return;
    }

    const previousIndex = this.cont() - 1;

    if (previousIndex < 0) {
      return;
    }

    const ticket = tickets[previousIndex];

    this.cont.set(previousIndex);
    this.ticketSelectedId.set(tickets[previousIndex].ticketId);
    this.ticketState.callTicket(ticket.ticketId);
  }

  // ==================== MÉTODOS DE PAGINAÇÃO - TICKETS ====================

  /**
   * Avança para próxima página de tickets
   */
  nextPageTickets(): void {
    this.ticketState.nextPageTickets();
  }

  /**
   * Volta para página anterior de tickets
   */
  previousPageTickets(): void {
    this.ticketState.previousPageTickets();
  }

  /**
   * Navega para página específica de tickets
   * @param page - Número da página (base 0)
   */
  goToPageTickets(page: number): void {
    this.ticketState.goToPageTickets(page);
  }

  /**
   * Calcula índice inicial dos tickets exibidos
   * @returns Índice inicial (base 1)
   */
  getStartIndexTickets(): number {
    return this.pageTickets() * this.itemsPerPage + 1;
  }

  /**
   * Calcula índice final dos tickets exibidos
   * @returns Índice final
   */
  getEndIndexTickets(): number {
    return Math.min((this.pageTickets() + 1) * this.itemsPerPage, this.totalElementsTickets());
  }

  /**
   * Gera array de números de páginas visíveis para tickets (máximo 4)
   * @returns Array com números das páginas
   */
  getPagesArrayTickets(): number[] {
    const total = this.totalPagesTickets();
    const current = this.pageTickets();
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

  // ==================== MÉTODOS DE PAGINAÇÃO - HISTÓRICO ====================

  /**
   * Avança para próxima página do histórico
   */
  nextPageHistory(): void {
    this.ticketState.nextPageHistory();
  }

  /**
   * Volta para página anterior do histórico
   */
  previousPageHistory(): void {
    this.ticketState.previousPageHistory();
  }

  /**
   * Navega para página específica do histórico
   * @param page - Número da página (base 0)
   */
  goToPageHistory(page: number): void {
    this.ticketState.goToPageHistory(page);
  }

  /**
   * Calcula índice inicial do histórico exibido
   * @returns Índice inicial (base 1)
   */
  getStartIndexHistory(): number {
    return this.pageHistory() * this.itemsPerPage + 1;
  }

  /**
   * Calcula índice final do histórico exibido
   * @returns Índice final
   */
  getEndIndexHistory(): number {
    return Math.min((this.pageHistory() + 1) * this.itemsPerPage, this.totalElementsHistory());
  }

  /**
   * Gera array de números de páginas visíveis para histórico (máximo 4)
   * @returns Array com números das páginas
   */
  getPagesArrayHistory(): number[] {
    const total = this.totalPagesHistory();
    const current = this.pageHistory();
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

  // ==================== MÉTODOS DE MODAL - CANCELAMENTO ====================

  /**
   * Abre modal de cancelamento de atendimento
   * @param ticketId - ID do ticket a ser cancelado
   */
  openModalCanceled(ticketId: string) {
    this.ticketSelectedId.set(ticketId);
    this.modalCancelAttendance = true;
  }

  /**
   * Fecha modal de cancelamento de atendimento
   */
  closeModalCanceled() {
    this.modalCancelAttendance = false;
  }

  /**
   * Cancela o atendimento de um ticket
   * @param ticketId - ID do ticket
   */
  cancelAttendance(ticketId: string) {

    if (ticketId === '') return;
    this.attendentState.cancelAttendance(ticketId);
  }

  // ==================== MÉTODOS DE MODAL - FINALIZAÇÃO ====================

  /**
   * Abre modal de finalização de atendimento
   * @param ticketId - ID do ticket a ser finalizado
   */
  openModalFinish(ticketId: string) {
    this.ticketSelectedId.set(ticketId);
    this.modalFinishAttendance = true;
  }

  /**
   * Fecha modal de finalização de atendimento
   */
  closeModalFinish() {
    this.modalFinishAttendance = false;
  }

  /**
   * Finaliza o atendimento de um ticket com resolução
   * @param ticketId - ID do ticket
   */
  finishAttendance(ticketId: string) {

    if (ticketId === '') return;
    this.attendentState.finishAttendance(ticketId, this.finishForm.value.resolution);
  }

  // ==================== MÉTODOS DE INTERAÇÃO COM PAINEL ====================

  /**
   * Define flag no localStorage para chamar o cliente por voz
   * A flag é consumida pelo QueueDisplayComponent
   */
  callCustomer() {

    const ticket = this.ticketSelected();
    if (!ticket) return;
    this.ticketState.callCustomer(ticket.ticketId);
  }

  /**
   * Abre o painel de fila em uma nova janela
   */
  redirectToQueueDisplay() {
    window.open('/queue-display', '_blank');
  }
}
