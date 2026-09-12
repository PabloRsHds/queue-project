package br.com.queue.service.ticket;

import br.com.queue.dtos.ticket.ResponseTicketDto;
import br.com.queue.dtos.ticket.allTickets.ResponseAllTicketsDto;
import br.com.queue.dtos.ticket.attendance.ResponseTicketsForAttendance;
import br.com.queue.dtos.ticket.create.CreateTicketDto;
import br.com.queue.dtos.ticket.finishTicket.FinishTicketDto;
import br.com.queue.entities.attendance.Attendance;
import br.com.queue.entities.customer.Customer;
import br.com.queue.entities.department.Department;
import br.com.queue.entities.schedule.Schedule;
import br.com.queue.entities.serviceManagement.ServiceManagement;
import br.com.queue.entities.ticket.Ticket;
import br.com.queue.entities.unit.Unit;
import br.com.queue.enums.PriorityLevel;
import br.com.queue.enums.TicketStatus;
import br.com.queue.infra.customer.CustomerNotFoundException;
import br.com.queue.infra.schedule.ScheduleNotFoundException;
import br.com.queue.infra.serviceManagement.ServiceManagementNotFoundException;
import br.com.queue.infra.ticket.TicketNotFoundException;
import br.com.queue.repositories.attendance.AttendanceRepository;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ServiceManagementRepository serviceManagementRepository;

    @Mock
    private UnitContext unitContext;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private JwtAuthenticationToken token;

    @InjectMocks
    private TicketService ticketService;

    private Ticket ticket;
    private Customer customer;
    private Schedule schedule;
    private ServiceManagement serviceManagement;
    private Unit unit;
    private Attendance attendance;
    private Department department;

    private CreateTicketDto createTicketDto;
    private FinishTicketDto finishTicketDto;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setName("Departamento Teste");

        unit = new Unit();
        unit.setUnitId("unit-123");
        unit.setName("Unidade Teste");
        unit.setAddress("Rua Teste, 123");
        unit.setActive(true);

        customer = new Customer();
        customer.setCustomerId("customer-123");
        customer.setName("Rodrigo");

        schedule = new Schedule();
        schedule.setScheduleId("schedule-123");

        serviceManagement = new ServiceManagement();
        serviceManagement.setServiceManagementId("service-123");
        serviceManagement.setName("Atendimento");
        serviceManagement.setCode("AT");
        serviceManagement.setLastTicketNumber(10L);
        serviceManagement.setDepartment(department);

        ticket = new Ticket();
        ticket.setTicketId("ticket-123");
        ticket.setCallNumber(11);
        ticket.setCode("AT-011");
        ticket.setCustomer(customer);
        ticket.setServiceManagement(serviceManagement);
        ticket.setSchedule(schedule);
        ticket.setUnit(unit);
        ticket.setPriority(PriorityLevel.NORMAL);
        ticket.setStatus(TicketStatus.WAITING);
        ticket.setCreatedAt(LocalDateTime.of(2026, 8, 28, 10, 30));

        attendance = new Attendance();
        attendance.setStartedAt(LocalDateTime.of(2026, 8, 28, 10, 30));
        attendance.setFinishedAt(LocalDateTime.of(2026, 8, 28, 10, 35));

        createTicketDto = new CreateTicketDto(
                "customer-123",
                "service-123",
                "NORMAL",
                "schedule-123"
        );

        finishTicketDto = new FinishTicketDto(
                "ticket-123",
                "FINISHED"
        );
    }

    // =========================================================
    // CREATE TICKET
    // =========================================================

    @Nested
    @DisplayName("Testes de criação de ticket")
    class CreateTicketTests {

        @Test
        @DisplayName("Deve criar ticket com sucesso")
        void shouldCreateTicketSuccessfully() {
            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(scheduleRepository.findById("schedule-123")).thenReturn(Optional.of(schedule));
            when(customerRepository.findByCustomerId("customer-123")).thenReturn(Optional.of(customer));
            when(serviceManagementRepository.findByServiceManagementId("service-123"))
                    .thenReturn(Optional.of(serviceManagement));
            when(ticketRepository.findTicketByScheduleScheduleId("schedule-123")).thenReturn(Optional.empty());
            when(serviceManagementRepository.save(serviceManagement)).thenReturn(serviceManagement);
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation ->
                    invocation.getArgument(0, Ticket.class)
            );

            var response = ticketService.createTicket(token, createTicketDto);

            assertNotNull(response);
            assertEquals("AT-011", response.code());
            assertEquals("customer-123", response.customerId());
            assertEquals("Rodrigo", response.customerName());
            assertEquals("service-123", response.serviceManagementId());
            assertEquals("Atendimento", response.serviceManagementName());
            assertEquals("NORMAL", response.priority());
            assertEquals("WAITING", response.status());

            verify(messagingTemplate).convertAndSend(
                    eq("/topic/tickets"),
                    any(ResponseTicketsForAttendance.class)
            );
        }

        @Test
        @DisplayName("Deve retornar ticket existente quando já existe para o agendamento")
        void shouldUpdateExistingTicket() {
            ticket.setCreatedAt(LocalDateTime.of(2026, 8, 27, 10, 0));

            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(scheduleRepository.findById("schedule-123")).thenReturn(Optional.of(schedule));
            when(customerRepository.findByCustomerId("customer-123")).thenReturn(Optional.of(customer));
            when(serviceManagementRepository.findByServiceManagementId("service-123"))
                    .thenReturn(Optional.of(serviceManagement));
            when(ticketRepository.findTicketByScheduleScheduleId("schedule-123")).thenReturn(Optional.of(ticket));
            when(ticketRepository.save(ticket)).thenReturn(ticket);

            var response = ticketService.createTicket(token, createTicketDto);

            assertNotNull(response);
            assertEquals("ticket-123", response.ticketId());

            verify(ticketRepository).save(ticket);
            verify(messagingTemplate, never()).convertAndSend(
                    eq("/topic/tickets"),
                    any(ResponseTicketsForAttendance.class)
            );
        }

        @Test
        @DisplayName("Deve lançar exceção quando schedule não existe")
        void shouldThrowWhenScheduleDoesNotExist() {
            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(scheduleRepository.findById("schedule-123")).thenReturn(Optional.empty());

            var exception = assertThrows(
                    ScheduleNotFoundException.class,
                    () -> ticketService.createTicket(token, createTicketDto)
            );

            assertEquals("Agendamento não encontrado com ID: schedule-123", exception.getMessage());

            verify(customerRepository, never()).findByCustomerId(anyString());
            verify(ticketRepository, never()).save(any(Ticket.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando customer não existe")
        void shouldThrowWhenCustomerDoesNotExist() {
            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(scheduleRepository.findById("schedule-123")).thenReturn(Optional.of(schedule));
            when(customerRepository.findByCustomerId("customer-123")).thenReturn(Optional.empty());

            var exception = assertThrows(
                    CustomerNotFoundException.class,
                    () -> ticketService.createTicket(token, createTicketDto)
            );

            assertEquals("Cliente não encontrado com ID: customer-123", exception.getMessage());

            verify(serviceManagementRepository, never()).findByServiceManagementId(anyString());
            verify(ticketRepository, never()).save(any(Ticket.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando service management não existe")
        void shouldThrowWhenServiceManagementDoesNotExist() {
            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(scheduleRepository.findById("schedule-123")).thenReturn(Optional.of(schedule));
            when(customerRepository.findByCustomerId("customer-123")).thenReturn(Optional.of(customer));
            when(serviceManagementRepository.findByServiceManagementId("service-123")).thenReturn(Optional.empty());

            var exception = assertThrows(
                    ServiceManagementNotFoundException.class,
                    () -> ticketService.createTicket(token, createTicketDto)
            );

            assertEquals("Serviço não encontrado com ID: service-123", exception.getMessage());

            verify(ticketRepository, never()).save(any(Ticket.class));
        }
    }

    // =========================================================
    // CALL TICKET
    // =========================================================

    @Nested
    @DisplayName("Testes de chamada de ticket")
    class CallTicketTests {

        @Test
        @DisplayName("Deve chamar ticket com sucesso")
        void shouldCallTicketSuccessfully() {
            when(ticketRepository.findById("ticket-123")).thenReturn(Optional.of(ticket));
            when(ticketRepository.save(ticket)).thenReturn(ticket);

            var response = ticketService.callTicket("ticket-123");

            assertNotNull(response);
            assertEquals("ticket-123", response.ticketId());
            assertEquals("CALLED", response.status());
            assertNotNull(ticket.getCalledAt());

            verify(ticketRepository).findById("ticket-123");
            verify(ticketRepository).save(ticket);
            verify(messagingTemplate).convertAndSend(
                    eq("/topic/queue-display"),
                    any(ResponseTicketDto.class)
            );
        }

        @Test
        @DisplayName("Deve lançar exceção quando ticket não existe")
        void shouldThrowWhenTicketDoesNotExist() {
            when(ticketRepository.findById("ticket-123")).thenReturn(Optional.empty());

            var exception = assertThrows(
                    TicketNotFoundException.class,
                    () -> ticketService.callTicket("ticket-123")
            );

            assertEquals("Ticket não encontrado com ID: ticket-123", exception.getMessage());

            verify(ticketRepository, never()).save(any(Ticket.class));
            verify(messagingTemplate, never()).convertAndSend(anyString(), any(ResponseTicketDto.class));
        }
    }

    // =========================================================
    // FINISH TICKET
    // =========================================================

    @Nested
    @DisplayName("Testes de finalização de ticket")
    class FinishTicketTests {

        @Test
        @DisplayName("Deve finalizar ticket com sucesso")
        void shouldFinishTicketSuccessfully() {
            when(ticketRepository.findById("ticket-123")).thenReturn(Optional.of(ticket));
            when(ticketRepository.save(ticket)).thenReturn(ticket);

            var response = ticketService.finishTicket(finishTicketDto);

            assertNotNull(response);
            assertEquals("FINISHED", response.status());
            assertEquals(TicketStatus.FINISHED, ticket.getStatus());

            verify(ticketRepository).findById("ticket-123");
            verify(ticketRepository).save(ticket);
        }

        @Test
        @DisplayName("Deve lançar exceção ao finalizar ticket inexistente")
        void shouldThrowWhenFinishingNonExistingTicket() {
            when(ticketRepository.findById("ticket-123")).thenReturn(Optional.empty());

            var exception = assertThrows(
                    TicketNotFoundException.class,
                    () -> ticketService.finishTicket(finishTicketDto)
            );

            assertEquals("Ticket não encontrado com ID: ticket-123", exception.getMessage());

            verify(ticketRepository, never()).save(any(Ticket.class));
        }
    }

    // =========================================================
    // CANCEL TICKET
    // =========================================================

    @Nested
    @DisplayName("Testes de cancelamento de ticket")
    class CancelTicketTests {

        @Test
        @DisplayName("Deve cancelar ticket com sucesso")
        void shouldCancelTicketSuccessfully() {
            when(ticketRepository.findById("ticket-123")).thenReturn(Optional.of(ticket));
            when(ticketRepository.save(ticket)).thenReturn(ticket);

            var response = ticketService.cancelTicket("ticket-123");

            assertNotNull(response);
            assertEquals("CANCELED", response.status());
            assertEquals(TicketStatus.CANCELED, ticket.getStatus());

            verify(attendanceRepository, never()).save(any(Attendance.class));
        }

        @Test
        @DisplayName("Deve finalizar atendimento ao cancelar ticket")
        void shouldFinishAttendanceWhenCancelingTicket() {
            ticket.setAttendance(attendance);

            when(ticketRepository.findById("ticket-123")).thenReturn(Optional.of(ticket));
            when(ticketRepository.save(ticket)).thenReturn(ticket);
            when(attendanceRepository.save(attendance)).thenReturn(attendance);

            var response = ticketService.cancelTicket("ticket-123");

            assertNotNull(response);
            assertEquals("CANCELED", response.status());
            assertNotNull(attendance.getFinishedAt());

            verify(attendanceRepository).save(attendance);
            verify(ticketRepository).save(ticket);
        }

        @Test
        @DisplayName("Não deve finalizar atendimento quando não existe atendimento")
        void shouldNotFinishAttendanceWhenAttendanceDoesNotExist() {
            ticket.setAttendance(null);

            when(ticketRepository.findById("ticket-123")).thenReturn(Optional.of(ticket));
            when(ticketRepository.save(ticket)).thenReturn(ticket);

            ticketService.cancelTicket("ticket-123");

            verify(attendanceRepository, never()).save(any(Attendance.class));
            verify(ticketRepository).save(ticket);
        }
    }

    // =========================================================
    // DELETE TICKET
    // =========================================================

    @Nested
    @DisplayName("Testes de exclusão de ticket")
    class DeleteTicketTests {

        @Test
        @DisplayName("Deve deletar ticket com sucesso")
        void shouldDeleteTicketSuccessfully() {
            when(ticketRepository.findById("ticket-123")).thenReturn(Optional.of(ticket));
            doNothing().when(ticketRepository).delete(ticket);

            var response = ticketService.deleteTicket("ticket-123");

            assertNotNull(response);
            assertEquals("ticket-123", response.ticketId());
            assertEquals("AT-011", response.code());

            verify(ticketRepository).findById("ticket-123");
            verify(ticketRepository).delete(ticket);
            verify(attendanceRepository, never()).delete(any(Attendance.class));
        }

        @Test
        @DisplayName("Deve deletar atendimento junto com ticket")
        void shouldDeleteAttendanceWhenDeletingTicket() {
            ticket.setAttendance(attendance);

            when(ticketRepository.findById("ticket-123")).thenReturn(Optional.of(ticket));
            doNothing().when(attendanceRepository).delete(attendance);
            doNothing().when(ticketRepository).delete(ticket);

            var response = ticketService.deleteTicket("ticket-123");

            assertNotNull(response);

            verify(attendanceRepository).delete(attendance);
            verify(ticketRepository).delete(ticket);
        }

        @Test
        @DisplayName("Deve lançar exceção quando ticket não existe")
        void shouldThrowWhenDeletingNonExistingTicket() {
            when(ticketRepository.findById("ticket-123")).thenReturn(Optional.empty());

            var exception = assertThrows(
                    TicketNotFoundException.class,
                    () -> ticketService.deleteTicket("ticket-123")
            );

            assertEquals("Ticket não encontrado com ID: ticket-123", exception.getMessage());

            verify(ticketRepository, never()).delete(any(Ticket.class));
            verify(attendanceRepository, never()).delete(any(Attendance.class));
        }
    }

    // =========================================================
    // GET TICKET BY ID
    // =========================================================

    @Nested
    @DisplayName("Testes de busca por ID")
    class GetTicketByIdTests {

        @Test
        @DisplayName("Deve buscar ticket por ID")
        void shouldGetTicketById() {
            when(ticketRepository.findById("ticket-123")).thenReturn(Optional.of(ticket));

            var response = ticketService.getTicketById("ticket-123");

            assertNotNull(response);
            assertEquals("ticket-123", response.ticketId());
            assertEquals("AT-011", response.code());
            assertEquals("Rodrigo", response.customerName());
            assertEquals("Atendimento", response.serviceManagementName());
            assertEquals("NORMAL", response.priority());
            assertEquals("WAITING", response.status());

            verify(ticketRepository).findById("ticket-123");
        }

        @Test
        @DisplayName("Deve lançar exceção quando ticket não existe")
        void shouldThrowWhenTicketNotFound() {
            when(ticketRepository.findById("ticket-123")).thenReturn(Optional.empty());

            assertThrows(
                    TicketNotFoundException.class,
                    () -> ticketService.getTicketById("ticket-123")
            );

            verify(ticketRepository).findById("ticket-123");
        }
    }

    // =========================================================
    // GET ALL TICKETS
    // =========================================================

    @Nested
    @DisplayName("Testes de busca de todos os tickets")
    class GetAllTicketsTests {

        @Test
        @DisplayName("Deve buscar todos os tickets com paginação")
        void shouldGetAllTicketsWithPagination() {
            var pageable = PageRequest.of(0, 10);
            Page<Ticket> page = new PageImpl<>(List.of(ticket));

            when(ticketRepository.findAll(pageable)).thenReturn(page);

            var result = ticketService.getAllTickets(0, 10);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals("ticket-123", result.getContent().get(0).ticketId());

            verify(ticketRepository).findAll(pageable);
        }

        @Test
        @DisplayName("Deve retornar página vazia")
        void shouldReturnEmptyPage() {
            var pageable = PageRequest.of(0, 10);
            Page<Ticket> page = new PageImpl<>(List.of());

            when(ticketRepository.findAll(pageable)).thenReturn(page);

            var result = ticketService.getAllTickets(0, 10);

            assertNotNull(result);
            assertEquals(0, result.getTotalElements());
            assertTrue(result.getContent().isEmpty());

            verify(ticketRepository).findAll(pageable);
        }
    }

    // =========================================================
    // GET TICKETS BY ATTENDANT
    // =========================================================

    @Nested
    @DisplayName("Testes de tickets do atendente")
    class GetTicketsByAttendantTests {

        @Test
        @DisplayName("Deve buscar tickets do atendente")
        void shouldGetTicketsByAttendant() {
            var pageable = PageRequest.of(0, 10);
            Page<Ticket> page = new PageImpl<>(List.of(ticket));

            when(token.getName()).thenReturn("user-123");
            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(ticketRepository.getTicketsByAttendant("unit-123", "user-123", pageable)).thenReturn(page);

            var result = ticketService.getTicketsByAttendant(token, 0, 10);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals("ticket-123", result.getContent().get(0).ticketId());

            verify(unitContext).getCurrentUnit(token);
            verify(token, times(2)).getName(); // <-- CORRIGIDO para 2 vezes
            verify(ticketRepository).getTicketsByAttendant("unit-123", "user-123", pageable);
        }
    }

    // =========================================================
    // GET HISTORY BY ATTENDANT
    // =========================================================

    @Nested
    @DisplayName("Testes de histórico do atendente")
    class GetHistoryTicketsByAttendantTests {

        @Test
        @DisplayName("Deve buscar histórico de tickets do atendente")
        void shouldGetHistoryTicketsByAttendant() {
            var pageable = PageRequest.of(0, 10);
            Page<Ticket> page = new PageImpl<>(List.of(ticket));

            when(token.getName()).thenReturn("user-123");
            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(ticketRepository.getHistoryTicketsByAttendant("unit-123", "user-123", pageable)).thenReturn(page);

            var result = ticketService.getHistoryTicketsByAttendant(token, 0, 10);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals("ticket-123", result.getContent().get(0).ticketId());

            verify(unitContext).getCurrentUnit(token);
            verify(ticketRepository).getHistoryTicketsByAttendant("unit-123", "user-123", pageable);
        }
    }

    // =========================================================
    // CALL CUSTOMER
    // =========================================================

    @Nested
    @DisplayName("Testes de chamada do cliente")
    class CallCustomerTests {

        @Test
        @DisplayName("Deve chamar cliente com sucesso")
        void shouldCallCustomerSuccessfully() {
            when(ticketRepository.findById("ticket-123")).thenReturn(Optional.of(ticket));

            var response = ticketService.callCustomer("ticket-123");

            assertNotNull(response);
            assertEquals("ticket-123", response.ticketId());
            assertEquals("AT-011", response.code());

            verify(ticketRepository).findById("ticket-123");
            verify(messagingTemplate).convertAndSend(
                    eq("/topic/queue-display/call"),
                    any(ResponseTicketDto.class)
            );
        }

        @Test
        @DisplayName("Deve lançar exceção quando ticket não existe")
        void shouldThrowWhenCallingCustomerForNonExistingTicket() {
            when(ticketRepository.findById("ticket-123")).thenReturn(Optional.empty());

            assertThrows(
                    TicketNotFoundException.class,
                    () -> ticketService.callCustomer("ticket-123")
            );

            verify(messagingTemplate, never()).convertAndSend(anyString(), any(ResponseTicketDto.class));
        }
    }

    // =========================================================
    // RESET CODE
    // =========================================================

    @Nested
    @DisplayName("Testes de reset do código")
    class ResetCodeTests {

        @Test
        @DisplayName("Deve resetar código do ticket")
        void shouldResetCode() {
            when(ticketRepository.findById("ticket-123")).thenReturn(Optional.of(ticket));
            when(ticketRepository.save(ticket)).thenReturn(ticket);

            ticketService.resetCode("ticket-123");

            assertEquals(0, ticket.getCallNumber());

            verify(ticketRepository).findById("ticket-123");
            verify(ticketRepository).save(ticket);
        }

        @Test
        @DisplayName("Deve lançar exceção ao resetar ticket inexistente")
        void shouldThrowWhenResettingNonExistingTicket() {
            when(ticketRepository.findById("ticket-123")).thenReturn(Optional.empty());

            assertThrows(
                    TicketNotFoundException.class,
                    () -> ticketService.resetCode("ticket-123")
            );

            verify(ticketRepository).findById("ticket-123");
            verify(ticketRepository, never()).save(any(Ticket.class));
        }
    }
}