package br.com.queue.service.schedule;

import br.com.queue.dtos.schedule.allSchedules.ResponseAllSchedulesDto;
import br.com.queue.dtos.schedule.create.CreateScheduleDto;
import br.com.queue.dtos.schedule.create.ResponseScheduleDto;
import br.com.queue.dtos.schedule.statistics.*;
import br.com.queue.dtos.schedule.update.UpdateScheduleDto;
import br.com.queue.entities.customer.Customer;
import br.com.queue.entities.schedule.Schedule;
import br.com.queue.entities.serviceManagement.ServiceManagement;
import br.com.queue.entities.ticket.Ticket;
import br.com.queue.entities.unit.Unit;
import br.com.queue.enums.PriorityLevel;
import br.com.queue.enums.ScheduleStatus;
import br.com.queue.infra.customer.CustomerNotFoundException;
import br.com.queue.infra.schedule.ScheduleDeleteException;
import br.com.queue.infra.schedule.ScheduleNotFoundException;
import br.com.queue.infra.serviceManagement.ServiceManagementNotFoundException;
import br.com.queue.infra.ticket.TicketNotFoundException;
import br.com.queue.repositories.customer.CustomerRepository;
import br.com.queue.repositories.schedule.ScheduleRepository;
import br.com.queue.repositories.serviceManagement.ServiceManagementRepository;
import br.com.queue.repositories.ticket.TicketRepository;
import br.com.queue.service.unit.UnitContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchedulingServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ServiceManagementRepository serviceManagementRepository;

    @Mock
    private UnitContext unitContext;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private JwtAuthenticationToken token;

    @InjectMocks
    private SchedulingService schedulingService;

    private Unit unit;
    private Customer customer;
    private ServiceManagement serviceManagement;
    private Schedule schedule;
    private Ticket ticket;
    private CreateScheduleDto createDto;
    private UpdateScheduleDto updateDto;

    @BeforeEach
    void setUp() {
        unit = new Unit();
        unit.setUnitId("unit-123");
        unit.setName("Unidade Teste");
        unit.setActive(true);

        customer = new Customer();
        customer.setCustomerId("customer-123");
        customer.setName("João Silva");
        customer.setCpf("123.456.789-00");
        customer.setPhone("(11) 99999-9999");
        customer.setEmail("joao@email.com");

        serviceManagement = new ServiceManagement();
        serviceManagement.setServiceManagementId("service-123");
        serviceManagement.setName("Suporte Técnico");

        ticket = new Ticket();
        ticket.setTicketId("ticket-123");
        ticket.setCode("ST-001");

        schedule = new Schedule();
        schedule.setScheduleId("schedule-123");
        schedule.setCustomer(customer);
        schedule.setServiceManagement(serviceManagement);
        schedule.setPriority(PriorityLevel.NORMAL);
        schedule.setScheduledDate(LocalDateTime.of(2026, 9, 15, 14, 30));
        schedule.setStatus(ScheduleStatus.SCHEDULED);
        schedule.setCreatedAt(LocalDateTime.of(2026, 9, 1, 10, 0));
        schedule.setUnit(unit);
        schedule.setTicket(ticket);

        createDto = new CreateScheduleDto(
                "customer-123",
                "service-123",
                "NORMAL",
                LocalDateTime.of(2026, 9, 15, 14, 30)
        );

        updateDto = new UpdateScheduleDto(
                "schedule-123",
                "customer-123",
                "service-123",
                PriorityLevel.NORMAL.name(),
                LocalDateTime.of(2026, 9, 16, 15, 0),
                "SCHEDULED"
        );
    }

    // =========================================================
    // CREATE SCHEDULE
    // =========================================================

    @Nested
    @DisplayName("Testes de criação de agendamento")
    class CreateScheduleTests {

        @Test
        @DisplayName("Deve criar agendamento com sucesso")
        void shouldCreateScheduleSuccessfully() {
            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(customerRepository.findByCustomerId("customer-123")).thenReturn(Optional.of(customer));
            when(serviceManagementRepository.findByServiceManagementId("service-123"))
                    .thenReturn(Optional.of(serviceManagement));
            when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> {
                Schedule saved = invocation.getArgument(0);
                saved.setScheduleId("schedule-123");
                return saved;
            });

            var response = schedulingService.createSchedule(token, createDto);

            assertNotNull(response);
            assertEquals("schedule-123", response.scheduleId());
            assertEquals("customer-123", response.customerId());
            assertEquals("João Silva", response.customerName());
            assertEquals("service-123", response.serviceManagementId());
            assertEquals("Suporte Técnico", response.serviceManagementName());
            assertEquals("NORMAL", response.priority());
            assertEquals("SCHEDULED", response.status());
            assertEquals(LocalDateTime.of(2026, 9, 15, 14, 30), response.scheduledDate());

            verify(unitContext).getCurrentUnit(token);
            verify(customerRepository).findByCustomerId("customer-123");
            verify(serviceManagementRepository).findByServiceManagementId("service-123");
            verify(scheduleRepository).save(any(Schedule.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não existe")
        void shouldThrowWhenCustomerDoesNotExist() {
            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(customerRepository.findByCustomerId("customer-123")).thenReturn(Optional.empty());

            var exception = assertThrows(
                    CustomerNotFoundException.class,
                    () -> schedulingService.createSchedule(token, createDto)
            );

            assertEquals("Cliente não encontrado com ID: customer-123", exception.getMessage());

            verify(customerRepository).findByCustomerId("customer-123");
            verify(serviceManagementRepository, never()).findByServiceManagementId(anyString());
            verify(scheduleRepository, never()).save(any(Schedule.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando serviço não existe")
        void shouldThrowWhenServiceDoesNotExist() {
            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(customerRepository.findByCustomerId("customer-123")).thenReturn(Optional.of(customer));
            when(serviceManagementRepository.findByServiceManagementId("service-123")).thenReturn(Optional.empty());

            var exception = assertThrows(
                    ServiceManagementNotFoundException.class,
                    () -> schedulingService.createSchedule(token, createDto)
            );

            assertEquals("Serviço não encontrado com ID: service-123", exception.getMessage());

            verify(customerRepository).findByCustomerId("customer-123");
            verify(serviceManagementRepository).findByServiceManagementId("service-123");
            verify(scheduleRepository, never()).save(any(Schedule.class));
        }
    }

    // =========================================================
    // UPDATE SCHEDULE
    // =========================================================

    @Nested
    @DisplayName("Testes de atualização de agendamento")
    class UpdateScheduleTests {

        @Test
        @DisplayName("Deve atualizar agendamento com sucesso")
        void shouldUpdateScheduleSuccessfully() {
            when(scheduleRepository.findByScheduleId("schedule-123")).thenReturn(Optional.of(schedule));
            when(customerRepository.findByCustomerId("customer-123")).thenReturn(Optional.of(customer));
            when(serviceManagementRepository.findByServiceManagementId("service-123"))
                    .thenReturn(Optional.of(serviceManagement));
            when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

            var response = schedulingService.updateSchedule(updateDto);

            assertNotNull(response);
            assertEquals("schedule-123", response.scheduleId());
            assertEquals("NORMAL", response.priority());
            assertEquals(LocalDateTime.of(2026, 9, 16, 15, 0), response.scheduledDate());

            verify(scheduleRepository).findByScheduleId("schedule-123");
            verify(customerRepository).findByCustomerId("customer-123");
            verify(serviceManagementRepository).findByServiceManagementId("service-123");
            verify(scheduleRepository).save(any(Schedule.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando agendamento não existe")
        void shouldThrowWhenScheduleDoesNotExist() {
            when(scheduleRepository.findByScheduleId("schedule-123")).thenReturn(Optional.empty());

            var exception = assertThrows(
                    ScheduleNotFoundException.class,
                    () -> schedulingService.updateSchedule(updateDto)
            );

            assertEquals("Agendamento não encontrado com ID: schedule-123", exception.getMessage());

            verify(scheduleRepository).findByScheduleId("schedule-123");
            verify(scheduleRepository, never()).save(any(Schedule.class));
        }

        @Test
        @DisplayName("Não deve atualizar quando nenhum campo é alterado")
        void shouldNotUpdateWhenNoChanges() {
            var dto = new UpdateScheduleDto(
                    "schedule-123",
                    null,
                    null,
                    null,
                    null,
                    null
            );

            when(scheduleRepository.findByScheduleId("schedule-123")).thenReturn(Optional.of(schedule));

            var response = schedulingService.updateSchedule(dto);

            assertNotNull(response);
            assertNull(schedule.getUpdatedAt());

            verify(scheduleRepository).findByScheduleId("schedule-123");
            // CORRIGIDO: NÃO deve verificar save() porque não houve mudanças
            verify(scheduleRepository, never()).save(any(Schedule.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não existe no update")
        void shouldThrowWhenCustomerDoesNotExistInUpdate() {
            var dto = new UpdateScheduleDto(
                    "schedule-123",
                    "customer-456",
                    null,
                    null,
                    null,
                    null
            );

            when(scheduleRepository.findByScheduleId("schedule-123")).thenReturn(Optional.of(schedule));
            when(customerRepository.findByCustomerId("customer-456")).thenReturn(Optional.empty());

            var exception = assertThrows(
                    CustomerNotFoundException.class,
                    () -> schedulingService.updateSchedule(dto)
            );

            assertEquals("Cliente não encontrado com ID: customer-456", exception.getMessage());

            verify(customerRepository).findByCustomerId("customer-456");
            verify(scheduleRepository, never()).save(any(Schedule.class));
        }
    }

    // =========================================================
    // GET ALL SCHEDULES
    // =========================================================

    @Nested
    @DisplayName("Testes de busca de todos os agendamentos")
    class GetAllSchedulesTests {

        @Test
        @DisplayName("Deve buscar todos os agendamentos com paginação")
        void shouldGetAllSchedulesWithPagination() {
            var pageable = PageRequest.of(0, 10);

            var responseDto = new ResponseAllSchedulesDto(
                    "schedule-123",
                    "customer-123",
                    "João Silva",
                    "123.456.789-00",
                    "1234567",
                    "(11) 99999-9999",
                    "joao@email.com",
                    "service-123",
                    "Suporte Técnico",
                    LocalDateTime.of(2026, 9, 15, 14, 30),
                    "SCHEDULED"
            );

            Page<ResponseAllSchedulesDto> page = new PageImpl<>(List.of(responseDto));

            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(scheduleRepository.findAllWithSearch("unit-123", null, null, pageable))
                    .thenReturn(page);

            var result = schedulingService.getAllSchedules(token, 0, 10, null, null);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals("schedule-123", result.getContent().get(0).scheduleId());
            assertEquals("João Silva", result.getContent().get(0).customerName());

            verify(unitContext).getCurrentUnit(token);
            verify(scheduleRepository).findAllWithSearch("unit-123", null, null, pageable);
        }

        @Test
        @DisplayName("Deve buscar agendamentos com filtro de busca")
        void shouldGetSchedulesWithSearchFilter() {
            var pageable = PageRequest.of(0, 10);

            var responseDto = new ResponseAllSchedulesDto(
                    "schedule-123",
                    "customer-123",
                    "João Silva",
                    "123.456.789-00",
                    "1234567",
                    "(11) 99999-9999",
                    "joao@email.com",
                    "service-123",
                    "Suporte Técnico",
                    LocalDateTime.of(2026, 9, 15, 14, 30),
                    "SCHEDULED"
            );

            Page<ResponseAllSchedulesDto> page = new PageImpl<>(List.of(responseDto));

            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(scheduleRepository.findAllWithSearch("unit-123", "João", null, pageable))
                    .thenReturn(page);

            var result = schedulingService.getAllSchedules(token, 0, 10, "João", null);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());

            verify(unitContext).getCurrentUnit(token);
            verify(scheduleRepository).findAllWithSearch("unit-123", "João", null, pageable);
        }

        @Test
        @DisplayName("Deve buscar agendamentos com filtro de data")
        void shouldGetSchedulesWithDateFilter() {
            var pageable = PageRequest.of(0, 10);
            var date = LocalDate.of(2026, 9, 15);

            var responseDto = new ResponseAllSchedulesDto(
                    "schedule-123",
                    "customer-123",
                    "João Silva",
                    "123.456.789-00",
                    "1234567",
                    "(11) 99999-9999",
                    "joao@email.com",
                    "service-123",
                    "Suporte Técnico",
                    LocalDateTime.of(2026, 9, 15, 14, 30),
                    "SCHEDULED"
            );

            Page<ResponseAllSchedulesDto> page = new PageImpl<>(List.of(responseDto));

            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(scheduleRepository.findAllWithSearch("unit-123", null, date, pageable))
                    .thenReturn(page);

            var result = schedulingService.getAllSchedules(token, 0, 10, null, date);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());

            verify(unitContext).getCurrentUnit(token);
            verify(scheduleRepository).findAllWithSearch("unit-123", null, date, pageable);
        }

        @Test
        @DisplayName("Deve retornar página vazia quando não há agendamentos")
        void shouldReturnEmptyPage() {
            var pageable = PageRequest.of(0, 10);
            Page<ResponseAllSchedulesDto> page = new PageImpl<>(List.of());

            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(scheduleRepository.findAllWithSearch("unit-123", null, null, pageable))
                    .thenReturn(page);

            var result = schedulingService.getAllSchedules(token, 0, 10, null, null);

            assertNotNull(result);
            assertEquals(0, result.getTotalElements());
            assertTrue(result.getContent().isEmpty());

            verify(unitContext).getCurrentUnit(token);
            verify(scheduleRepository).findAllWithSearch("unit-123", null, null, pageable);
        }
    }

    // =========================================================
    // GET SCHEDULE BY ID
    // =========================================================

    @Nested
    @DisplayName("Testes de busca de agendamento por ID")
    class GetScheduleByIdTests {

        @Test
        @DisplayName("Deve buscar agendamento por ID com sucesso")
        void shouldGetScheduleByIdSuccessfully() {
            when(scheduleRepository.findByScheduleId("schedule-123")).thenReturn(Optional.of(schedule));
            when(ticketRepository.findByTicketId("ticket-123")).thenReturn(Optional.of(ticket));

            var response = schedulingService.getScheduleById("schedule-123");

            assertNotNull(response);
            assertEquals("schedule-123", response.scheduleId());
            assertEquals("customer-123", response.customerId());
            assertEquals("João Silva", response.customerName());
            assertEquals("service-123", response.serviceManagementId());
            assertEquals("Suporte Técnico", response.serviceManagementName());
            assertEquals("ticket-123", response.ticketId());
            assertEquals("ST-001", response.ticketCode());
            assertEquals("NORMAL", response.priority());
            assertEquals("SCHEDULED", response.status());

            verify(scheduleRepository).findByScheduleId("schedule-123");
            verify(ticketRepository).findByTicketId("ticket-123");
        }

        @Test
        @DisplayName("Deve lançar exceção quando agendamento não existe")
        void shouldThrowWhenScheduleDoesNotExist() {
            when(scheduleRepository.findByScheduleId("schedule-123")).thenReturn(Optional.empty());

            var exception = assertThrows(
                    ScheduleNotFoundException.class,
                    () -> schedulingService.getScheduleById("schedule-123")
            );

            assertEquals("Agendamento não encontrado com ID: schedule-123", exception.getMessage());

            verify(scheduleRepository).findByScheduleId("schedule-123");
        }

        @Test
        @DisplayName("Deve retornar ticketId e ticketCode como null quando ticket não existe")
        void shouldReturnNullTicketWhenTicketDoesNotExist() {
            schedule.setTicket(null);

            when(scheduleRepository.findByScheduleId("schedule-123")).thenReturn(Optional.of(schedule));

            var response = schedulingService.getScheduleById("schedule-123");

            assertNotNull(response);
            assertNull(response.ticketId());
            assertNull(response.ticketCode());

            verify(scheduleRepository).findByScheduleId("schedule-123");
            verify(ticketRepository, never()).findByTicketId(anyString());
        }
    }

    // =========================================================
    // DELETE SCHEDULE
    // =========================================================

    @Nested
    @DisplayName("Testes de deleção de agendamento")
    class DeleteScheduleTests {

        @Test
        @DisplayName("Deve deletar agendamento com sucesso")
        void shouldDeleteScheduleSuccessfully() {
            // Remove o ticket para permitir deleção
            schedule.setTicket(null);

            when(scheduleRepository.findByScheduleId("schedule-123")).thenReturn(Optional.of(schedule));
            doNothing().when(scheduleRepository).delete(schedule);

            var response = schedulingService.deleteSchedule("schedule-123");

            assertNotNull(response);
            assertEquals("schedule-123", response.scheduleId());

            verify(scheduleRepository).findByScheduleId("schedule-123");
            verify(scheduleRepository).delete(schedule);
        }

        @Test
        @DisplayName("Deve lançar exceção quando agendamento não existe")
        void shouldThrowWhenScheduleDoesNotExist() {
            when(scheduleRepository.findByScheduleId("schedule-123")).thenReturn(Optional.empty());

            var exception = assertThrows(
                    ScheduleNotFoundException.class,
                    () -> schedulingService.deleteSchedule("schedule-123")
            );

            assertEquals("Agendamento não encontrado com ID: schedule-123", exception.getMessage());

            verify(scheduleRepository).findByScheduleId("schedule-123");
            verify(scheduleRepository, never()).delete(any(Schedule.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando agendamento tem atendimento iniciado")
        void shouldThrowWhenScheduleHasAttendanceStarted() {
            var attendance = mock(br.com.queue.entities.attendance.Attendance.class);

            var ticketWithAttendance = new Ticket();
            ticketWithAttendance.setAttendance(attendance);
            schedule.setTicket(ticketWithAttendance);

            when(scheduleRepository.findByScheduleId("schedule-123")).thenReturn(Optional.of(schedule));

            var exception = assertThrows(
                    ScheduleDeleteException.class,
                    () -> schedulingService.deleteSchedule("schedule-123")
            );

            assertEquals(
                    "Não é possível excluir um agendamento quando o atendimento já foi iniciado",
                    exception.getMessage()
            );

            verify(scheduleRepository).findByScheduleId("schedule-123");
            verify(scheduleRepository, never()).delete(any(Schedule.class));
        }

        @Test
        @DisplayName("Deve permitir deletar agendamento com ticket sem atendimento")
        void shouldAllowDeleteScheduleWithTicketWithoutAttendance() {
            // Ticket existe mas sem atendimento
            var ticketWithoutAttendance = new Ticket();
            ticketWithoutAttendance.setAttendance(null);
            schedule.setTicket(ticketWithoutAttendance);

            when(scheduleRepository.findByScheduleId("schedule-123")).thenReturn(Optional.of(schedule));
            doNothing().when(scheduleRepository).delete(schedule);

            var response = schedulingService.deleteSchedule("schedule-123");

            assertNotNull(response);
            assertEquals("schedule-123", response.scheduleId());

            verify(scheduleRepository).findByScheduleId("schedule-123");
            verify(scheduleRepository).delete(schedule);
        }
    }

    // =========================================================
    // GET STATISTICS
    // =========================================================

    @Nested
    @DisplayName("Testes de estatísticas do dashboard")
    class GetStatisticsTests {

        @Test
        @DisplayName("Deve retornar estatísticas com sucesso")
        void shouldReturnStatisticsSuccessfully() {
            var totalDto = mock(ResponseCountTotalSchedulesStatisticsDto.class);
            var percentageDto = mock(ResponseSchedulePercentagesStatisticsDto.class);
            var createdByMonth = List.of(mock(ResponseSchedulesCreatedByMonthStatisticsDto.class));
            var createdByWeek = List.of(mock(ResponseSchedulesCreatedByWeekStatisticsDto.class));
            var createdByDay = mock(ResponseSchedulesCreatedByDayStatisticsDto.class);
            var byDepartment = List.of(mock(ResponseSchedulesByDepartmentStatisticsDto.class));
            var byService = List.of(mock(ResponseSchedulesByServiceStatisticsDto.class));
            var byPriority = List.of(mock(ResponseSchedulesByPriorityStatisticsDto.class));
            var byHour = List.of(mock(ResponseSchedulesByHourStatisticsDto.class));

            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(scheduleRepository.countTotalSchedulesStatisticsDto("unit-123")).thenReturn(totalDto);
            when(scheduleRepository.getSchedulePercentagesStatisticsDto("unit-123")).thenReturn(percentageDto);
            when(scheduleRepository.countSchedulesCreatedByMonth("unit-123")).thenReturn(createdByMonth);
            when(scheduleRepository.countSchedulesCreatedByWeek("unit-123")).thenReturn(createdByWeek);
            when(scheduleRepository.countSchedulesCreatedByDay("unit-123")).thenReturn(createdByDay);
            when(scheduleRepository.countSchedulesByDepartment("unit-123")).thenReturn(byDepartment);
            when(scheduleRepository.countSchedulesByService("unit-123")).thenReturn(byService);
            when(scheduleRepository.countSchedulesByPriority("unit-123")).thenReturn(byPriority);
            when(scheduleRepository.countSchedulesByHour("unit-123")).thenReturn(byHour);

            var response = schedulingService.getScheduleStatistics(token);

            assertNotNull(response);
            assertEquals(totalDto, response.countTotalScheduleStatistics());
            assertEquals(percentageDto, response.schedulePercentagesStatistics());
            assertEquals(createdByMonth, response.schedulesCreatedByMonth());
            assertEquals(createdByWeek, response.schedulesCreatedByWeek());
            assertEquals(createdByDay, response.scheduleCreatedByDay());
            assertEquals(byDepartment, response.schedulesByDepartment());
            assertEquals(byService, response.schedulesByService());
            assertEquals(byPriority, response.schedulesByPriority());
            assertEquals(byHour, response.schedulesByHour());

            verify(unitContext).getCurrentUnit(token);
            verify(scheduleRepository).countTotalSchedulesStatisticsDto("unit-123");
            verify(scheduleRepository).getSchedulePercentagesStatisticsDto("unit-123");
            verify(scheduleRepository).countSchedulesCreatedByMonth("unit-123");
            verify(scheduleRepository).countSchedulesCreatedByWeek("unit-123");
            verify(scheduleRepository).countSchedulesCreatedByDay("unit-123");
            verify(scheduleRepository).countSchedulesByDepartment("unit-123");
            verify(scheduleRepository).countSchedulesByService("unit-123");
            verify(scheduleRepository).countSchedulesByPriority("unit-123");
            verify(scheduleRepository).countSchedulesByHour("unit-123");
        }

        @Test
        @DisplayName("Deve retornar estatísticas mesmo com dados vazios")
        void shouldReturnStatisticsEvenWithEmptyData() {
            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(scheduleRepository.countTotalSchedulesStatisticsDto("unit-123")).thenReturn(null);
            when(scheduleRepository.getSchedulePercentagesStatisticsDto("unit-123")).thenReturn(null);
            when(scheduleRepository.countSchedulesCreatedByMonth("unit-123")).thenReturn(List.of());
            when(scheduleRepository.countSchedulesCreatedByWeek("unit-123")).thenReturn(List.of());
            when(scheduleRepository.countSchedulesCreatedByDay("unit-123")).thenReturn(null);
            when(scheduleRepository.countSchedulesByDepartment("unit-123")).thenReturn(List.of());
            when(scheduleRepository.countSchedulesByService("unit-123")).thenReturn(List.of());
            when(scheduleRepository.countSchedulesByPriority("unit-123")).thenReturn(List.of());
            when(scheduleRepository.countSchedulesByHour("unit-123")).thenReturn(List.of());

            var response = schedulingService.getScheduleStatistics(token);

            assertNotNull(response);
            assertNull(response.countTotalScheduleStatistics());
            assertNull(response.schedulePercentagesStatistics());
            assertTrue(response.schedulesCreatedByMonth().isEmpty());
            assertTrue(response.schedulesCreatedByWeek().isEmpty());
            assertNull(response.scheduleCreatedByDay());
            assertTrue(response.schedulesByDepartment().isEmpty());
            assertTrue(response.schedulesByService().isEmpty());
            assertTrue(response.schedulesByPriority().isEmpty());
            assertTrue(response.schedulesByHour().isEmpty());

            verify(unitContext).getCurrentUnit(token);
            verify(scheduleRepository).countTotalSchedulesStatisticsDto("unit-123");
            verify(scheduleRepository).getSchedulePercentagesStatisticsDto("unit-123");
            verify(scheduleRepository).countSchedulesCreatedByMonth("unit-123");
            verify(scheduleRepository).countSchedulesCreatedByWeek("unit-123");
            verify(scheduleRepository).countSchedulesCreatedByDay("unit-123");
            verify(scheduleRepository).countSchedulesByDepartment("unit-123");
            verify(scheduleRepository).countSchedulesByService("unit-123");
            verify(scheduleRepository).countSchedulesByPriority("unit-123");
            verify(scheduleRepository).countSchedulesByHour("unit-123");
        }
    }
}