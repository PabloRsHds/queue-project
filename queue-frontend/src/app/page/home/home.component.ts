import { ServiceManagementService } from './../../services/states/serviceManagement/service-management.service';
import { Component, effect, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GlobalStatesService } from '../../services/states/global-states.service';
import { TableServicesComponent } from "../../components/table-services/table-services.component";
import { TableDepartmentsComponent } from "../../components/table-departments/table-departments.component";
import { TableUsersComponent } from "../../components/table-users/table-users.component";
import { SchedulingComponent } from "../../components/scheduling/scheduling.component";
import { GraphicComponent } from "../../components/graphic/graphic.component";
import { ConfigComponent } from "../../components/config/config.component";
import { DepartmentStateService } from '../../services/states/department/department-state.service';
import { UserStateService } from '../../services/states/user/user-state.service';
import { ServiceCounterComponent } from "../../components/service-counter/service-counter.component";
import { ScheduleStateService } from '../../services/states/scheduling/scheduling-state.service';
import { AttendentStateService } from '../../services/states/attendent/attendent-state.service';
import { timer } from 'rxjs';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApexAxisChartSeries, ApexChart, ApexXAxis, ApexDataLabels, ChartComponent } from "ng-apexcharts";
import { CustomerStateService } from '../../services/states/customer/customer-state.service';
import { LoginStateService } from '../../services/states/login/login-state.service';
import { TableUnitComponent } from "../../components/table-unit/table-unit.component";

export type ChartOptions = {
  series: ApexAxisChartSeries;
  chart: ApexChart;
  xaxis: ApexXAxis;
  dataLabels: ApexDataLabels;
};

export type DonutChartOptions = {
  series: ApexNonAxisChartSeries;
  chart: ApexChart;
  labels: string[];
  colors: string[];
  legend: ApexLegend;
  dataLabels: ApexDataLabels;
  responsive: ApexResponsive[];
};

@Component({
  selector: 'app-home',
  imports: [CommonModule, ChartComponent, TableServicesComponent, TableDepartmentsComponent, TableUsersComponent, SchedulingComponent, GraphicComponent, ConfigComponent, ServiceCounterComponent, TableUnitComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent {

  // ==================== CONFIGURAÇÕES DOS GRÁFICOS ====================

  /** Configuração do gráfico de departamentos criados por mês */
  public chartOptions!: ChartOptions;

  /** Configuração do gráfico de atendimentos por semana */
  public chartAttendanceWeekOptions!: ChartOptions;

  /** Configuração do gráfico de agendamentos por prioridade (donut) */
  public chartSchedulingPriorityOptions!: DonutChartOptions;

  // ==================== INJEÇÕES DE DEPENDÊNCIA ====================

  /* Service para gerenciar estado de login */
  public loginState = inject(LoginStateService);

  /** Service para gerenciar estado global da aplicação */
  public globalState = inject(GlobalStatesService);

  /** Service para gerenciar estado do departamento */
  public departmentState = inject(DepartmentStateService);

  /** Service para gerenciar estado do usuário */
  public userState = inject(UserStateService);
  public loadUserByToken = this.userState.loadUserByToken;

  /** Service para gerenciar estado do agendamento */
  public scheduleState = inject(ScheduleStateService);

  /** Service para gerenciar estado do atendente */
  public attendentState = inject(AttendentStateService);

  /** Service para gerenciar estado do serviço */
  public serviceState = inject(ServiceManagementService);

  /** Service para gerenciar estado do cliente */
  public customerState = inject(CustomerStateService);

  /** Service para gerenciar estado do agendamento (alias) */
  public schedulingState = inject(ScheduleStateService);

  /** Service para navegação entre rotas */
  public router = inject(Router);

  /** Service para exibir notificações toast */
  public snackBar = inject(MatSnackBar);

  // ==================== ESTATÍSTICAS DE ATENDENTE ====================

  /** Tempo médio de espera */
  public avarageWaitingTime = this.attendentState.averageWaitingTime;

  /** Tempo médio de serviço */
  public averageServiceTime = this.attendentState.averageServiceTime;

  /** Média de atendimentos por usuário */
  public averageAttendanceByUsers = this.attendentState.averageAttendanceByUser;

  /** Atendimentos criados por mês */
  public attendancesCreatedByMonth = this.attendentState.attendancesCreatedByMonth;

  /** Atendimentos por semana */
  public attendancesByWeek = this.attendentState.attendancesByWeek;

  /** Atendimentos por serviço */
  public attendancesByService = this.attendentState.attendancesByService;

  /** Atendimentos por hora */
  public attendancesByHour = this.attendentState.attendancesByHour;

  /** Atendimentos por departamento */
  public attendancesByDepartment = this.attendentState.attendancesByDepartment;

  /** Atendimentos por cliente */
  public attendancesByCustomer = this.attendentState.attendancesByCustomer;

  // ==================== ESTATÍSTICAS DE DEPARTAMENTO ====================

  /** Departamentos criados por mês */
  public departmentsCreatedByMonth = this.departmentState.departmentsCreatedByMonth;

  // ==================== ESTATÍSTICAS DE SERVIÇO ====================

  /** Serviços criados por mês */
  public servicesCreatedByMonth = this.serviceState.servicesCreatedByMonth;

  /** Usuários por serviço */
  public usersByService = this.serviceState.usersByService;

  /** Agendamentos por serviço */
  public schedulesByService = this.serviceState.schedulesByService;

  // ==================== ESTATÍSTICAS DE USUÁRIO ====================

  /** Usuários criados por mês */
  public usersCreatedByMonth = this.userState.usersCreatedByMonthStatistics;

  // ==================== ESTATÍSTICAS DE AGENDAMENTO ====================

  /** Total de agendamentos */
  public totalScheduling = this.schedulingState.countTotalScheduleStatistics;

  /** Total de agendamentos por mês */
  public totalSchedulingByMonth = this.schedulingState.schedulesCreatedByMonth;

  /** Total de agendamentos por semana */
  public totalSchedulingByWeek = this.schedulingState.schedulesCreatedByWeek;

  /** Agendamentos criados por dia */
  public scheduleCreatedByDay = this.schedulingState.scheduleCreatedByDay;

  /** Agendamentos por hora */
  public schedulesByHour = this.schedulingState.schedulesByHour;

  /** Agendamentos por prioridade */
  public schedulesByPriority = this.schedulingState.schedulesByPriority;

  // ==================== ESTATÍSTICAS DE CLIENTE ====================

  /** Total de clientes por mês */
  public totalCustomersByMonth = this.customerState.totalCustomersByMonth;

  // ==================== ESTADOS GERAIS ====================

  /** Usuário atualmente logado */
  public userLogged = this.userState.userLogged;

  /** Seção ativa do menu lateral */
  public activeSection = this.globalState.activeSection;

  /** Total de departamentos cadastrados */
  public totalDepartments = this.departmentState.countTotalDepartment;

  /** Total de serviços cadastrados */
  public totalServices = this.serviceState.countTotalServicesStatistics;

  /** Total de usuários cadastrados */
  public totalUsers = this.userState.countTotalUsersStatistics;

  /** Estatísticas de agendamentos por dia */
  public scheduleStatistics = this.scheduleState.scheduleCreatedByDay;

  /** Total de atendimentos realizados */
  public countTotalAttendances = this.attendentState.countTotalAttendances;

  // ==================== VARIÁVEIS DE CONTROLE ====================

  /** Controla abertura do menu lateral em dispositivos móveis */
  public openAside = false;

  /** Controla abertura do diálogo de logout */
  public dialog: boolean = false;

  // ==================== CICLO DE VIDA ====================

  /**
   * Inicializa o componente
   * - Busca dados do usuário pelo token
   * - Carrega estatísticas de departamentos, serviços e usuários
   */
  ngOnInit() {
    this.userState.getUserByToken();
  }

  // ==================== EFEITOS DE REATIVIDADE ====================

  constructor() {

    effect(() => {

      if (this.loginState.logoutStatus() === 'success') {

        this.snackBar.open(this.loginState.logoutMessage(), 'Fechar',
          { duration: 3000, panelClass: ['snackbar-success'] },
        );

        localStorage.removeItem('accessToken');
        localStorage.removeItem('activeSection');
        this.router.navigate(['/login']);
        this.activeSection.set('inicio');
        localStorage.setItem('activeSection', 'inicio');
      }

    });

    /**
     * Efeito: Carrega estatísticas baseadas na role do usuário logado
     * Quando o usuário é carregado, determina quais estatísticas carregar
     */
    effect(() => {
      const user = this.userLogged();

      if (!user) return;

      const role = user.role;

      // Carrega estatísticas comuns a todos os usuários
      this.userState.loadStatistics();

      // Carrega estatísticas específicas baseadas na role
      switch (role) {
        case 'MANAGER':
        case 'ADMIN':
          // Gerentes/Admins veem todas as estatísticas
          this.departmentState.loadStatistics();
          this.serviceState.loadStatistics();
          this.scheduleState.loadStatistics();
          break;

        case 'ATTENDANT':
          // Atendentes só veem estatísticas de atendimento
          this.attendentState.loadStatistics();
          this.scheduleState.loadStatistics();
          break;

        case 'RECEPTION':
          // Recepcionistas só veem estatísticas de agendamentos
          this.scheduleState.loadStatistics();
          this.customerState.loadStatistics();
          break;

        default:
          // Fallback: carrega estatísticas básicas
          this.departmentState.loadStatistics();
          this.serviceState.loadStatistics();
          this.scheduleState.loadStatistics();
          this.customerState.loadStatistics();
          this.attendentState.loadStatistics();
          break;
      }
    });

    /**
     * Efeito: Configura gráfico de atendimentos por semana
     * Quando dados de atendimentos semanais são carregados, cria gráfico de barras
     */
    effect(() => {
      const data = this.attendancesByWeek();
      if (!data || data.length === 0) return;
      this.chartAttendanceWeekOptions = {
        series: [{
          name: 'Atendimentos',
          data: data.map(x => x.totalAttendances)
        }],
        chart: {
          type: 'bar',
          height: 350,
          toolbar: { show: false }
        },
        xaxis: {
          categories: data.map(x => x.dayName)
        },
        dataLabels: {
          enabled: true
        }
      };
    });

    /**
     * Efeito: Configura gráfico de departamentos criados por mês
     * Quando dados de departamentos mensais são carregados, cria gráfico de barras
     */
    effect(() => {
      const data = this.departmentsCreatedByMonth();
      if (!data || data.length === 0) return;
      this.chartOptions = {
        series: [{
          name: 'Departamentos',
          data: data.map(x => x.totalDepartments)
        }],
        chart: {
          type: 'bar',
          height: 350,
          toolbar: { show: false }
        },
        xaxis: {
          categories: data.map(x => x.monthName)
        },
        dataLabels: {
          enabled: true
        }
      };
    });

    /**
     * Efeito: Configura gráfico de agendamentos por prioridade (donut)
     * Quando dados de prioridades são carregados, cria gráfico donut
     * Cores: Azul, Vermelho, Verde, Amarelo
     */
    effect(() => {
      const data = this.schedulesByPriority();
      if (!data || data.length === 0) return;
      this.chartSchedulingPriorityOptions = {
        series: data.map(x => x.totalSchedules),
        chart: {
          type: 'donut',
          height: 300
        },
        labels: data.map(x => x.priority),
        colors: ['#3b82f6', 'tomato', '#ef4444', '#f59e0b'],
        legend: {
          position: 'bottom'
        },
        dataLabels: {
          enabled: true
        },
        responsive: [{
          breakpoint: 768,
          options: {
            chart: { width: 300 },
            legend: { position: 'bottom' }
          }
        }]
      };
    });
  }

  // ==================== MÉTODOS DE NAVEGAÇÃO ====================

  /**
   * Define a seção ativa do menu lateral
   * Salva no localStorage para persistência e fecha menu em mobile
   * @param section - Nome da seção (inicio, department, service, user, scheduling, attendance, config)
   */
  setActive(section: string) {
    localStorage.setItem('activeSection', section);
    this.activeSection.set(section);
    // Fecha menu lateral em dispositivos móveis após navegação
    if (window.innerWidth <= 768) {
      this.openAside = false;
    }
  }

  /**
   * Navega para uma seção específica e abre o modal correspondente
   * Utilizado para ações rápidas de criação via botões
   * @param section - Nome da seção alvo
   */
  functionActive(section: string) {
    this.activeSection.set(section);

    // Aguarda 500ms para troca de seção antes de abrir modal
    if (section === 'department') {
      timer(500).subscribe(() => {
        this.departmentState.modalRegister.set(true);
      });
    }

    if (section === 'service') {
      timer(500).subscribe(() => {
        this.serviceState.modalRegister.set(true);
      });
    }

    if (section === 'user') {
      timer(500).subscribe(() => {
        this.userState.modalRegister.set(true);
      });
    }

    if (section === 'scheduling') {
      timer(500).subscribe(() => {
        this.scheduleState.modalSchedulingRegister.set(true);
        this.schedulingState.table.set('Scheduling');
      });
    }

    if (section === 'scheduling-customer') {
      this.activeSection.set('scheduling');
      timer(500).subscribe(() => {
        this.scheduleState.modalCustomerRegister.set(true);
        this.schedulingState.table.set('Customers');
      });
    }

    if (section === 'attendant') {
      // Aguarda 200ms para navegar para atendimento
      timer(200).subscribe(() => {
        this.activeSection.set('attendance');
      });
    }
  }

  // ==================== MÉTODOS AUXILIARES ====================

  /**
   * Retorna o nome do cargo em português
   * @param role - Cargo em inglês (MANAGER, ATTENDANT, RECEPTION)
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
   * Abre ou fecha o diálogo de confirmação de logout
   */
  openOrCloseDialog() {
    this.dialog = !this.dialog;
  }

  // ==================== MÉTODOS DE AÇÃO ====================

  /**
   * Realiza o logout do usuário
   * - Remove token de acesso do localStorage
   * - Exibe mensagem de sucesso
   * - Redireciona para página de login
   * - Reseta seção ativa para 'inicio'
   */
  logout() {
    this.loginState.logout();
  }

  /**
   * Abre o painel de fila em uma nova janela
   * Utilizado para exibir a fila de atendimento em tela separada
   */
  redirectToQueueDisplay() {
    window.open('/queue-display', '_blank');
  }

  /**
   * Abre o painel de fila em uma nova janela
   * Utilizado para exibir a fila de atendimento em tela separada
   */
  redirectToStart() {
    this.setActive('inicio');
  }
}
