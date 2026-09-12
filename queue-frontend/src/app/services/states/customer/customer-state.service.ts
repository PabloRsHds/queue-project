import { computed, inject, Injectable, signal } from "@angular/core";
import { HttpService } from "../../backend/http.service";
import { ResponseAllCustomersDto } from "../../../dtos/customer/ResponseAllCustomersDto";
import { ResponseCustomerInfoDto } from "../../../dtos/customer/ResponseCustomerInfoDto";
import { ResponseCustomerIdsAndNames } from "../../../dtos/customer/ResponseCustomerIdsAndNames";
import { CreateCustomerDto } from "../../../dtos/customer/CreateCustomerDto";
import { UpdateCustomerDto } from "../../../dtos/customer/UpdateCustomerDto";
import { ResponseCountTotalCustomersStatisticsDto } from "../../../dtos/customer/statistics/ResponseCountTotalCustomersStatisticsDto";
import { ResponseCustomersCreatedByMonthStatisticsDto } from "../../../dtos/customer/statistics/ResponseCustomersCreatedByMonthStatisticsDto";
import { HttpErrorResponse } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class CustomerStateService {

  private http = inject(HttpService);

  // ===== STATES =====

  public customers = signal<ResponseAllCustomersDto[]>([]);
  public customerInfo = signal<ResponseCustomerInfoDto | null>(null);
  public customerIdsAndNames = signal<ResponseCustomerIdsAndNames[]>([]);
  public customerSuggestions = signal<ResponseAllCustomersDto[]>([]);

  // ===== MESSAGES =====

  public registerCustomerMessage = signal('');
  public registerCustomerStatus = signal<'success' | 'error' | 'default'>('default');

  public updateCustomerMessage = signal('');
  public updateCustomerStatus = signal<'success' | 'error' | 'default'>('default');

  public deleteCustomerMessage = signal('');
  public deleteCustomerStatus = signal<'success' | 'error' | 'default'>('default');

  // ==== STATISTICS =====
  public totalCustomers = signal<ResponseCountTotalCustomersStatisticsDto | null>(null);
  public totalCustomersByMonth = signal<ResponseCustomersCreatedByMonthStatisticsDto[] | []>([]);

  // ===== PAGINATION =====

  public customerPage = signal(0);
  public customerSize = 4;
  public customerTotalElements = signal(0);
  public customerSearch = signal('');

  public customerTotalPages = computed(() =>
    Math.ceil(this.customerTotalElements() / this.customerSize)
  );

  // ===== METHODS =====

  loadCustomers() {
    this.http.getAllCustomers(
      this.customerPage(),
      this.customerSize,
      this.customerSearch()
    ).subscribe({
      next: response => {
        this.customers.set(response.content);
        this.customerTotalElements.set(response.totalElements);
      },
      error: (error: HttpErrorResponse) => {
        console.error('Erro ao carregar clientes:', error);
      }
    });
  }

  loadCustomerIdsAndNames() {
    this.http.getCustomerIdsAndNames().subscribe({
      next: response => {
        this.customerIdsAndNames.set(response);
      },
      error: (error: HttpErrorResponse) => {
        this.customerIdsAndNames.set([]);
        console.error('Erro ao carregar IDs e nomes dos clientes:', error);
      }
    });
  }

  searchCustomers(search: string) {
    this.http.getAllCustomers(0, 5, search).subscribe({
      next: response => {
        this.customerSuggestions.set(response.content);
      },
      error: (error: HttpErrorResponse) => {
        this.customerSuggestions.set([]);
        console.error('Erro ao buscar sugestões de clientes:', error);
      }
    });
  }

  getInfoCustomer(customerId: string) {
    this.http.getCustomerById(customerId).subscribe({
      next: response => {
        this.customerInfo.set(response);
      },
      error: (error: HttpErrorResponse) => {
        this.customerInfo.set(null);
        console.error('Erro ao buscar informações do cliente:', error);
      }
    });
  }

  // ===== LOAD STATISTICS =====
  loadStatistics() {
    this.http.getCustomerStatistics().subscribe({
      next: (response) => {
        this.totalCustomers.set(response.countTotalCustomersStatistics);
        this.totalCustomersByMonth.set(response.customersCreatedByMonthStatistics);
      },
      error: (error: HttpErrorResponse) => {
        console.error('Erro ao carregar estatísticas de clientes:', error);
      }
    })
  }

  createCustomer(request: CreateCustomerDto) {
    this.http.createCustomer(request).subscribe({
      next: () => {
        this.registerCustomerMessage.set('Cliente registrado com sucesso!');
        this.registerCustomerStatus.set('success');
        this.loadCustomers();
        this.loadStatistics();
      },
      error: (error: HttpErrorResponse) => {
        this.registerCustomerMessage.set(error.error?.message || 'Erro ao registrar cliente');
        this.registerCustomerStatus.set('error');
      }
    });
  }

  updateCustomer(request: UpdateCustomerDto) {
    this.http.updateCustomer(request).subscribe({
      next: () => {
        this.updateCustomerMessage.set('Cliente atualizado com sucesso!');
        this.updateCustomerStatus.set('success');
        this.loadCustomers();
        this.loadStatistics();

        // Atualiza o detalhe se estiver aberto
        if (this.customerInfo() && this.customerInfo()?.customerId === request.customerId) {
          this.getInfoCustomer(request.customerId);
        }
      },
      error: (error: HttpErrorResponse) => {
        this.updateCustomerMessage.set(error.error?.message || 'Erro ao atualizar cliente');
        this.updateCustomerStatus.set('error');
      }
    });
  }

  deleteCustomer(customerId: string) {
    this.http.deleteCustomer(customerId).subscribe({
      next: () => {
        // Se deletou o último item da página, volta uma página
        if (this.customers().length === 1 && this.customerPage() > 0) {
          this.customerPage.update(p => p - 1);
        }

        this.deleteCustomerMessage.set('Cliente deletado com sucesso!');
        this.deleteCustomerStatus.set('success');
        this.loadCustomers();
        this.loadStatistics();
        this.customerInfo.set(null);
      },
      error: (error: HttpErrorResponse) => {
        this.deleteCustomerMessage.set(error.error?.message || 'Erro ao deletar cliente');
        this.deleteCustomerStatus.set('error');
      }
    });
  }

  nextPage() {
    if (this.customerPage() + 1 >= this.customerTotalPages()) return;

    this.customerPage.update(p => p + 1);
    this.loadCustomers();
  }

  previousPage() {
    if (this.customerPage() === 0) return;

    this.customerPage.update(p => p - 1);
    this.loadCustomers();
  }

  goToPage(page: number) {
    if (page < 0 || page >= this.customerTotalPages()) return;

    this.customerPage.set(page);
    this.loadCustomers();
  }

  setSearch(value: string) {
    this.customerSearch.set(value);
    this.customerPage.set(0);
    this.loadCustomers();
  }

  resetCustomerInfo() {
    this.customerInfo.set(null);
  }

  resetStatus() {
    this.registerCustomerStatus.set('default');
    this.updateCustomerStatus.set('default');
    this.deleteCustomerStatus.set('default');
  }
}
