import { inject, Injectable, signal } from '@angular/core';
import { HttpService } from '../../backend/http.service';
import { CreateTicketDto } from '../../../dtos/ticket/CreateTicketDto';
import { ResponseTicketDto } from '../../../dtos/ticket/ResponseTicketDto';
import { ResponseTicketsForAttendanceDto } from '../../../dtos/ticket/ResponseTicketsForAttendanceDto';
import { HttpErrorResponse } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class TicketStateService {

  // INJECTIONS
  private http = inject(HttpService);

  // STATES
  public ticketInfo = signal<ResponseTicketDto | null>(null);
  public ticketForPanel = signal<ResponseTicketDto | null>(null);

  public createStatus = signal<'success' | 'error' | 'default'>('default');
  public createMessage = signal('');

  public deleteStatus = signal<'success' | 'error' | 'default'>('default');
  public deleteMessage = signal('');

  public callTicketStatus = signal<'success' | 'error' | 'default'>('default');
  public callTicketMessage = signal('');

  public callCustomerStatus = signal<'success' | 'error' | 'default'>('default');
  public callCustomerMessage = signal('');

  // Variables
  public ticketsForAttendance = signal<ResponseTicketsForAttendanceDto[]>([]);
  public historyTickets = signal<ResponseTicketsForAttendanceDto[]>([]);
  public totalTickets = signal(0);

  // PAGINATION TICKETS
  public pageTickets = signal<number>(0);
  public readonly size = 6;
  public totalElementsTickets = signal(0);
  public totalPagesTickets = signal(0);

  // PAGINATION HISTORY
  public pageHistory = signal<number>(0);
  public totalElementsHistory = signal(0);
  public totalPagesHistory = signal(0);


  createTicket(request: CreateTicketDto) {

    this.http.createTicket(request).subscribe({
      next: (response) => {
        this.createStatus.set('success');
        this.createMessage.set('Ticket criado com sucesso!');
        this.ticketInfo.set(response);
        this.getTicketsForAttendence();
      },
      error: (error: HttpErrorResponse) => {
        this.createStatus.set(error.error?.message || 'Erro ao criar ticket');
        this.createMessage.set('Erro ao criar ticket');
      }
    })
  }

  deleteTicket(request: string) {

    this.http.deleteTicket(request).subscribe({

      next: (response) => {

        this.deleteStatus.set('success');
        this.deleteMessage.set('Ticket deletado com sucesso!');
      },
      error: (error: HttpErrorResponse) => {
        this.deleteStatus.set(error.error?.message || 'Erro ao deletar ticket');
        this.deleteMessage.set('Erro ao deletar ticket');
      }
    })
  }

  callTicket(ticketId: string) {

    this.http.callTicket(ticketId).subscribe({
      next: () => {
      },
      error: (error: HttpErrorResponse) => {
        this.callTicketMessage.set(error.error?.message || 'Erro ao chamar ticket');
        this.callTicketStatus.set('error');
      }
    });
  }

  callCustomer(ticketId: string) {

    this.http.callCustomer(ticketId).subscribe({
      next: (response) => {
      },
      error: (error: HttpErrorResponse) => {
        this.callCustomerMessage.set(error.error?.message || 'Erro ao chamar cliente');
        this.callCustomerStatus.set('error');
      }
    });
  }

  getTicketsForAttendence() {
    this.http.getTicketsForAttendance(this.pageTickets(), this.size).subscribe({
      next: (response) => {
        this.ticketsForAttendance.set(response.content);
        this.totalTickets.set(response.totalElements);
        this.totalElementsTickets.set(response.totalElements);
        this.totalPagesTickets.set(response.totalPages);
      }
    });
  }

  getHistoryTicketsByAttendant() {
    return this.http.getHistoryTicketsByAttendant(this.pageTickets(), this.size).subscribe({
      next: (response) => {
        this.historyTickets.set(response.content);
        this.totalTickets.set(response.totalElements);
      }
    });
  }

  // PAGINATION TICKETS
  nextPageTickets() {

    if (this.pageTickets() + 1 >= this.totalPagesTickets()) return;

    this.pageTickets.update(p => p + 1);
    this.getTicketsForAttendence();
  }

  previousPageTickets() {

    if (this.pageTickets() === 0) return;
    this.pageTickets.update(p => p - 1);

    this.getTicketsForAttendence();
  }

  goToPageTickets(page: number) {

    if (page < 0 || page >= this.totalPagesTickets()) return;
    this.pageTickets.set(page);
    this.getTicketsForAttendence();
  }

  // PAGINATION HISTORY
  nextPageHistory() {

    if (this.pageHistory() + 1 >= this.totalPagesHistory()) return;

    this.pageHistory.update(p => p + 1);
    this.getHistoryTicketsByAttendant();
  }

  previousPageHistory() {

    if (this.pageHistory() === 0) return;
    this.pageHistory.update(p => p - 1);

    this.getHistoryTicketsByAttendant();
  }

  goToPageHistory(page: number) {

    if (page < 0 || page >= this.totalPagesHistory()) return;
    this.pageHistory.set(page);
    this.getHistoryTicketsByAttendant();
  }

  // RESETS
  resetStatus() {
    this.createStatus.set('default');
    this.deleteStatus.set('default');
  }

  // WEBSOCKET
  addTicketFromWebSocket(ticket: ResponseTicketsForAttendanceDto) {

    this.ticketsForAttendance.update(tickets => [
      ticket,
      ...tickets
    ]);

    this.totalTickets.update(total => total + 1);
  }
}
