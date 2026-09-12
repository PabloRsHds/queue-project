package br.com.queue.service.attendance;

import br.com.queue.dtos.attendance.allAttendances.ResponseAllAttendances;
import br.com.queue.dtos.attendance.start.StartAttendanceDto;
import br.com.queue.dtos.attendance.start.FinishAttendanceDto;
import br.com.queue.dtos.attendance.finish.ResponseAttendanceDto;
import br.com.queue.dtos.attendance.finish.ResponseFinishAttendanceDto;
import br.com.queue.dtos.attendance.statistics.*;
import br.com.queue.dtos.ticket.attendance.ResponseTicketsForAttendance;
import br.com.queue.entities.attendance.Attendance;
import br.com.queue.entities.customer.Customer;
import br.com.queue.entities.serviceManagement.ServiceManagement;
import br.com.queue.entities.ticket.Ticket;
import br.com.queue.entities.unit.Unit;
import br.com.queue.entities.user.User;
import br.com.queue.enums.PriorityLevel;
import br.com.queue.enums.Role;
import br.com.queue.enums.TicketStatus;
import br.com.queue.infra.attendances.AttendanceInvalidStateException;
import br.com.queue.infra.attendances.AttendanceNotFoundException;
import br.com.queue.infra.ticket.TicketNotFoundException;
import br.com.queue.infra.user.UserNotFoundException;
import br.com.queue.repositories.attendance.AttendanceRepository;
import br.com.queue.repositories.ticket.TicketRepository;
import br.com.queue.repositories.user.UserRepository;
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
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UnitContext unitContext;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private JwtAuthenticationToken token;

    @InjectMocks
    private AttendanceService attendanceService;

    private Unit unit;
    private User user;
    private Customer customer;
    private ServiceManagement serviceManagement;
    private Ticket ticket;
    private Attendance attendance;
    private StartAttendanceDto startDto;
    private FinishAttendanceDto finishDto;

    @BeforeEach
    void setUp() {
        unit = new Unit();
        unit.setUnitId("unit-123");
        unit.setName("Unidade Teste");
        unit.setActive(true);

        user = new User();
        user.setUserId("user-123");
        user.setUsername("atendente");
        user.setName("Atendente");
        user.setRole(Role.ATTENDANT);
        user.setActive(true);
        user.setUnit(unit);

        customer = new Customer();
        customer.setCustomerId("customer-123");
        customer.setName("João Silva");

        serviceManagement = new ServiceManagement();
        serviceManagement.setServiceManagementId("service-123");
        serviceManagement.setName("Suporte Técnico");

        ticket = new Ticket();
        ticket.setTicketId("ticket-123");
        ticket.setCode("ST-001");
        ticket.setCustomer(customer);
        ticket.setServiceManagement(serviceManagement);
        ticket.setPriority(PriorityLevel.NORMAL);
        ticket.setStatus(TicketStatus.CALLED);
        ticket.setCreatedAt(LocalDateTime.of(2026, 8, 29, 10, 0));
        ticket.setUnit(unit);

        attendance = new Attendance();
        attendance.setAttendanceId("attendance-123");
        attendance.setTicket(ticket);
        attendance.setUser(user);
        attendance.setStartedAt(LocalDateTime.of(2026, 8, 29, 10, 5));
        attendance.setUnit(unit);

        startDto = new StartAttendanceDto("ticket-123");
        finishDto = new FinishAttendanceDto("ticket-123", "Atendimento realizado com sucesso");
    }

    // =========================================================
    // START ATTENDANCE
    // =========================================================

    @Nested
    @DisplayName("Testes de início de atendimento")
    class StartAttendanceTests {

        @Test
        @DisplayName("Deve iniciar atendimento com sucesso para ATTENDANT")
        void shouldStartAttendanceSuccessfullyForAttendant() {
            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(token.getName()).thenReturn("user-123");
            when(ticketRepository.findByTicketIdAndUnitId("ticket-123", "unit-123"))
                    .thenReturn(Optional.of(ticket));
            when(userRepository.findById("user-123")).thenReturn(Optional.of(user));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> {
                Attendance saved = invocation.getArgument(0);
                saved.setAttendanceId("attendance-456");
                return saved;
            });

            var response = attendanceService.startAttendance(token, startDto);

            assertNotNull(response);
            assertEquals("ticket-123", response.ticketId());
            assertEquals("ST-001", response.ticketCode());
            assertNotNull(response.startedAt());
            assertEquals(TicketStatus.IN_PROGRESS, ticket.getStatus());

            verify(unitContext).getCurrentUnit(token);
            verify(token).getName();
            verify(ticketRepository).findByTicketIdAndUnitId("ticket-123", "unit-123");
            verify(userRepository).findById("user-123");
            verify(ticketRepository).save(any(Ticket.class));
            verify(attendanceRepository).save(any(Attendance.class));
        }

        @Test
        @DisplayName("Deve iniciar atendimento com sucesso para ADMIN")
        void shouldStartAttendanceSuccessfullyForAdmin() {
            user.setRole(Role.ADMIN);

            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(token.getName()).thenReturn("user-123");
            when(ticketRepository.findByTicketIdAndUnitId("ticket-123", "unit-123"))
                    .thenReturn(Optional.of(ticket));
            when(userRepository.findById("user-123")).thenReturn(Optional.of(user));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> {
                Attendance saved = invocation.getArgument(0);
                saved.setAttendanceId("attendance-456");
                return saved;
            });

            var response = attendanceService.startAttendance(token, startDto);

            assertNotNull(response);
            assertEquals("ticket-123", response.ticketId());
            assertEquals(TicketStatus.IN_PROGRESS, ticket.getStatus());

            verify(unitContext).getCurrentUnit(token);
            verify(ticketRepository).findByTicketIdAndUnitId("ticket-123", "unit-123");
            verify(userRepository).findById("user-123");
            verify(ticketRepository).save(any(Ticket.class));
            verify(attendanceRepository).save(any(Attendance.class));
        }


        @Test
        @DisplayName("Deve lançar exceção quando usuário não existe")
        void shouldThrowWhenUserDoesNotExist() {
            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(token.getName()).thenReturn("user-123");
            when(ticketRepository.findByTicketIdAndUnitId("ticket-123", "unit-123"))
                    .thenReturn(Optional.of(ticket));
            when(userRepository.findById("user-123")).thenReturn(Optional.empty());

            var exception = assertThrows(
                    UserNotFoundException.class,
                    () -> attendanceService.startAttendance(token, startDto)
            );

            assertEquals("Usuário não encontrado com ID: user-123", exception.getMessage());

            verify(userRepository).findById("user-123");
            verify(ticketRepository, never()).save(any(Ticket.class));
            verify(attendanceRepository, never()).save(any(Attendance.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando ticket não está com status CALLED")
        void shouldThrowWhenTicketStatusIsNotCalled() {
            ticket.setStatus(TicketStatus.WAITING);

            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(token.getName()).thenReturn("user-123");
            when(ticketRepository.findByTicketIdAndUnitId("ticket-123", "unit-123"))
                    .thenReturn(Optional.of(ticket));
            when(userRepository.findById("user-123")).thenReturn(Optional.of(user));

            var exception = assertThrows(
                    AttendanceInvalidStateException.class,
                    () -> attendanceService.startAttendance(token, startDto)
            );

            assertEquals("Apenas tickets com status chamado - CALLED podem iniciar atendimento",
                    exception.getMessage());

            verify(ticketRepository).findByTicketIdAndUnitId("ticket-123", "unit-123");
            verify(userRepository).findById("user-123");
            verify(ticketRepository, never()).save(any(Ticket.class));
            verify(attendanceRepository, never()).save(any(Attendance.class));
        }
    }

    // =========================================================
    // FINISH ATTENDANCE
    // =========================================================

    @Nested
    @DisplayName("Testes de finalização de atendimento")
    class FinishAttendanceTests {

        @Test
        @DisplayName("Deve finalizar atendimento com sucesso")
        void shouldFinishAttendanceSuccessfully() {
            ticket.setStatus(TicketStatus.IN_PROGRESS);

            when(ticketRepository.findByTicketId("ticket-123")).thenReturn(Optional.of(ticket));
            when(attendanceRepository.findByTicket(ticket)).thenReturn(Optional.of(attendance));
            when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

            var response = attendanceService.finishAttendance(finishDto);

            assertNotNull(response);
            assertEquals("Atendimento realizado com sucesso", response.resolution());
            assertNotNull(response.finishedAt());
            assertEquals(TicketStatus.FINISHED, ticket.getStatus());

            verify(ticketRepository).findByTicketId("ticket-123");
            verify(attendanceRepository).findByTicket(ticket);
            verify(attendanceRepository).save(any(Attendance.class));
            verify(ticketRepository).save(any(Ticket.class));
            verify(messagingTemplate).convertAndSend(eq("/topic/tickets/history"),
                    any(ResponseTicketsForAttendance.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando ticket não existe")
        void shouldThrowWhenTicketDoesNotExistInFinish() {
            when(ticketRepository.findByTicketId("ticket-123")).thenReturn(Optional.empty());

            var exception = assertThrows(
                    TicketNotFoundException.class,
                    () -> attendanceService.finishAttendance(finishDto)
            );

            assertEquals("Ticket não encontrado com ID: ticket-123", exception.getMessage());

            verify(ticketRepository).findByTicketId("ticket-123");
            verify(attendanceRepository, never()).findByTicket(any(Ticket.class));
            verify(attendanceRepository, never()).save(any(Attendance.class));
            verify(ticketRepository, never()).save(any(Ticket.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando atendimento não existe para o ticket")
        void shouldThrowWhenAttendanceDoesNotExist() {
            when(ticketRepository.findByTicketId("ticket-123")).thenReturn(Optional.of(ticket));
            when(attendanceRepository.findByTicket(ticket)).thenReturn(Optional.empty());

            var exception = assertThrows(
                    AttendanceNotFoundException.class,
                    () -> attendanceService.finishAttendance(finishDto)
            );

            assertEquals("Atendimento não encontrado para o ticket: ticket-123",
                    exception.getMessage());

            verify(ticketRepository).findByTicketId("ticket-123");
            verify(attendanceRepository).findByTicket(ticket);
            verify(attendanceRepository, never()).save(any(Attendance.class));
            verify(ticketRepository, never()).save(any(Ticket.class));
        }
    }

    // =========================================================
    // GET ALL ATTENDANCES
    // =========================================================

    @Nested
    @DisplayName("Testes de busca de todos os atendimentos")
    class GetAllAttendancesTests {

        @Test
        @DisplayName("Deve buscar todos os atendimentos com paginação")
        void shouldGetAllAttendancesWithPagination() {
            var pageable = PageRequest.of(0, 10);
            Page<Attendance> page = new PageImpl<>(List.of(attendance));

            when(attendanceRepository.findAll(pageable)).thenReturn(page);

            var result = attendanceService.getAllAttendances(0, 10);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals("ticket-123", result.getContent().get(0).ticketId());
            assertEquals("ST-001", result.getContent().get(0).ticketCode());
            assertNotNull(result.getContent().get(0).startedAt());

            verify(attendanceRepository).findAll(pageable);
        }

        @Test
        @DisplayName("Deve retornar página vazia quando não há atendimentos")
        void shouldReturnEmptyPage() {
            var pageable = PageRequest.of(0, 10);
            Page<Attendance> page = new PageImpl<>(List.of());

            when(attendanceRepository.findAll(pageable)).thenReturn(page);

            var result = attendanceService.getAllAttendances(0, 10);

            assertNotNull(result);
            assertEquals(0, result.getTotalElements());
            assertTrue(result.getContent().isEmpty());

            verify(attendanceRepository).findAll(pageable);
        }
    }

    // =========================================================
    // DELETE ATTENDANCE
    // =========================================================

    @Nested
    @DisplayName("Testes de deleção de atendimento")
    class DeleteAttendanceTests {

        @Test
        @DisplayName("Deve deletar atendimento com sucesso")
        void shouldDeleteAttendanceSuccessfully() {
            when(attendanceRepository.findByAttendanceId("attendance-123")).thenReturn(Optional.of(attendance));
            doNothing().when(attendanceRepository).delete(attendance);

            attendanceService.deleteAttendance("attendance-123");

            verify(attendanceRepository).findByAttendanceId("attendance-123");
            verify(attendanceRepository).delete(attendance);
        }

        @Test
        @DisplayName("Deve lançar exceção quando atendimento não existe")
        void shouldThrowWhenAttendanceDoesNotExist() {
            when(attendanceRepository.findByAttendanceId("attendance-123")).thenReturn(Optional.empty());

            var exception = assertThrows(
                    AttendanceNotFoundException.class,
                    () -> attendanceService.deleteAttendance("attendance-123")
            );

            assertEquals("Atendimento não encontrado com ID: attendance-123",
                    exception.getMessage());

            verify(attendanceRepository).findByAttendanceId("attendance-123");
            verify(attendanceRepository, never()).delete(any(Attendance.class));
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
            var totalDto = mock(ResponseCountTotalAttendancesStatisticsDto.class);
            var waitingTimeDto = mock(ResponseAverageWaitingTimeStatisticsDto.class);
            var serviceTimeDto = mock(ResponseAverageServiceTimeStatisticsDto.class);
            var byUser = List.of(mock(ResponseAverageAttendanceByUserStatisticsDto.class));
            var byMonth = List.of(mock(ResponseAttendancesCreatedByMonthStatisticsDto.class));
            var byWeek = List.of(mock(ResponseAttendancesByWeekStatisticsDto.class));
            var byService = List.of(mock(ResponseAttendancesByServiceStatisticsDto.class));
            var byHour = List.of(mock(ResponseAttendancesByHourStatisticsDto.class));
            var byDepartment = List.of(mock(ResponseAttendancesByDepartmentStatisticsDto.class));
            var byCustomer = List.of(mock(ResponseAttendancesByCustomerStatisticsDto.class));

            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(attendanceRepository.countTotalAttendances("unit-123")).thenReturn(totalDto);
            when(attendanceRepository.getAverageWaitingTime("unit-123")).thenReturn(waitingTimeDto);
            when(attendanceRepository.getAverageServiceTime("unit-123")).thenReturn(serviceTimeDto);
            when(attendanceRepository.averageAttendanceByUser("unit-123")).thenReturn(byUser);
            when(attendanceRepository.countAttendancesCreatedByMonth("unit-123")).thenReturn(byMonth);
            when(attendanceRepository.countAttendancesByWeek("unit-123")).thenReturn(byWeek);
            when(attendanceRepository.countAttendancesByService("unit-123")).thenReturn(byService);
            when(attendanceRepository.countAttendancesByHour("unit-123")).thenReturn(byHour);
            when(attendanceRepository.countAttendancesByDepartment("unit-123")).thenReturn(byDepartment);
            when(attendanceRepository.countAttendancesByCustomer("unit-123")).thenReturn(byCustomer);

            var response = attendanceService.getAttendanceStatistics(token);

            assertNotNull(response);
            assertEquals(totalDto, response.countTotalAttendances());
            assertEquals(waitingTimeDto, response.averageWaitingTime());
            assertEquals(serviceTimeDto, response.averageServiceTime());
            assertEquals(byUser, response.averageAttendanceByUser());
            assertEquals(byMonth, response.attendancesCreatedByMonth());
            assertEquals(byWeek, response.attendancesByWeek());
            assertEquals(byService, response.attendancesByService());
            assertEquals(byHour, response.attendancesByHour());
            assertEquals(byDepartment, response.attendancesByDepartment());
            assertEquals(byCustomer, response.attendancesByCustomer());

            verify(unitContext).getCurrentUnit(token);
            verify(attendanceRepository).countTotalAttendances("unit-123");
            verify(attendanceRepository).getAverageWaitingTime("unit-123");
            verify(attendanceRepository).getAverageServiceTime("unit-123");
            verify(attendanceRepository).averageAttendanceByUser("unit-123");
            verify(attendanceRepository).countAttendancesCreatedByMonth("unit-123");
            verify(attendanceRepository).countAttendancesByWeek("unit-123");
            verify(attendanceRepository).countAttendancesByService("unit-123");
            verify(attendanceRepository).countAttendancesByHour("unit-123");
            verify(attendanceRepository).countAttendancesByDepartment("unit-123");
            verify(attendanceRepository).countAttendancesByCustomer("unit-123");
        }

        @Test
        @DisplayName("Deve retornar estatísticas mesmo com dados vazios")
        void shouldReturnStatisticsEvenWithEmptyData() {
            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(attendanceRepository.countTotalAttendances("unit-123")).thenReturn(null);
            when(attendanceRepository.getAverageWaitingTime("unit-123")).thenReturn(null);
            when(attendanceRepository.getAverageServiceTime("unit-123")).thenReturn(null);
            when(attendanceRepository.averageAttendanceByUser("unit-123")).thenReturn(List.of());
            when(attendanceRepository.countAttendancesCreatedByMonth("unit-123")).thenReturn(List.of());
            when(attendanceRepository.countAttendancesByWeek("unit-123")).thenReturn(List.of());
            when(attendanceRepository.countAttendancesByService("unit-123")).thenReturn(List.of());
            when(attendanceRepository.countAttendancesByHour("unit-123")).thenReturn(List.of());
            when(attendanceRepository.countAttendancesByDepartment("unit-123")).thenReturn(List.of());
            when(attendanceRepository.countAttendancesByCustomer("unit-123")).thenReturn(List.of());

            var response = attendanceService.getAttendanceStatistics(token);

            assertNotNull(response);
            assertNull(response.countTotalAttendances());
            assertNull(response.averageWaitingTime());
            assertNull(response.averageServiceTime());
            assertTrue(response.averageAttendanceByUser().isEmpty());
            assertTrue(response.attendancesCreatedByMonth().isEmpty());
            assertTrue(response.attendancesByWeek().isEmpty());
            assertTrue(response.attendancesByService().isEmpty());
            assertTrue(response.attendancesByHour().isEmpty());
            assertTrue(response.attendancesByDepartment().isEmpty());
            assertTrue(response.attendancesByCustomer().isEmpty());

            verify(unitContext).getCurrentUnit(token);
            verify(attendanceRepository).countTotalAttendances("unit-123");
            verify(attendanceRepository).getAverageWaitingTime("unit-123");
            verify(attendanceRepository).getAverageServiceTime("unit-123");
            verify(attendanceRepository).averageAttendanceByUser("unit-123");
            verify(attendanceRepository).countAttendancesCreatedByMonth("unit-123");
            verify(attendanceRepository).countAttendancesByWeek("unit-123");
            verify(attendanceRepository).countAttendancesByService("unit-123");
            verify(attendanceRepository).countAttendancesByHour("unit-123");
            verify(attendanceRepository).countAttendancesByDepartment("unit-123");
            verify(attendanceRepository).countAttendancesByCustomer("unit-123");
        }
    }

    // =========================================================
    // FIND AUXILIAR METHODS
    // =========================================================

    @Nested
    @DisplayName("Testes dos métodos auxiliares find")
    class FindAuxiliarTests {

        @Test
        @DisplayName("Deve encontrar ticket por ID e unidade com sucesso")
        void shouldFindTicketByTicketIdAndUnitIdSuccessfully() {

            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(token.getName()).thenReturn("user-123");
            when(ticketRepository.findByTicketIdAndUnitId("ticket-123", "unit-123"))
                    .thenReturn(Optional.of(ticket));
            when(userRepository.findById("user-123")).thenReturn(Optional.of(user));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> {
                Attendance saved = invocation.getArgument(0);
                saved.setAttendanceId("attendance-456");
                return saved;
            });

            attendanceService.startAttendance(token, startDto);

            verify(ticketRepository).findByTicketIdAndUnitId("ticket-123", "unit-123");
        }

        @Test
        @DisplayName("Deve encontrar usuário por ID com sucesso")
        void shouldFindUserByIdSuccessfully() {
            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(token.getName()).thenReturn("user-123");
            when(ticketRepository.findByTicketIdAndUnitId("ticket-123", "unit-123"))
                    .thenReturn(Optional.of(ticket));
            when(userRepository.findById("user-123")).thenReturn(Optional.of(user));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> {
                Attendance saved = invocation.getArgument(0);
                saved.setAttendanceId("attendance-456");
                return saved;
            });

            attendanceService.startAttendance(token, startDto);

            verify(userRepository).findById("user-123");
        }

        @Test
        @DisplayName("Deve encontrar atendimento por ticket com sucesso")
        void shouldFindAttendanceByTicketSuccessfully() {
            ticket.setStatus(TicketStatus.IN_PROGRESS);

            when(ticketRepository.findByTicketId("ticket-123")).thenReturn(Optional.of(ticket));
            when(attendanceRepository.findByTicket(ticket)).thenReturn(Optional.of(attendance));
            when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

            attendanceService.finishAttendance(finishDto);

            verify(attendanceRepository).findByTicket(ticket);
        }

        @Test
        @DisplayName("Deve encontrar atendimento por ID com sucesso")
        void shouldFindAttendanceByIdSuccessfully() {
            when(attendanceRepository.findByAttendanceId("attendance-123")).thenReturn(Optional.of(attendance));
            doNothing().when(attendanceRepository).delete(attendance);

            attendanceService.deleteAttendance("attendance-123");

            verify(attendanceRepository).findByAttendanceId("attendance-123");
        }
    }

    // =========================================================
    // VERIFICAR MENSAGENS DE ERRO
    // =========================================================

    @Nested
    @DisplayName("Testes de verificação de mensagens de erro")
    class ErrorMessagesTests {

        @Test
        @DisplayName("Deve verificar mensagem de UserNotFoundException")
        void shouldCheckUserNotFoundExceptionMessage() {
            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(token.getName()).thenReturn("user-123");
            when(ticketRepository.findByTicketIdAndUnitId("ticket-123", "unit-123"))
                    .thenReturn(Optional.of(ticket));
            when(userRepository.findById("user-123")).thenReturn(Optional.empty());

            var exception = assertThrows(
                    UserNotFoundException.class,
                    () -> attendanceService.startAttendance(token, startDto)
            );

            assertEquals("Usuário não encontrado com ID: user-123", exception.getMessage());
        }

        @Test
        @DisplayName("Deve verificar mensagem de AttendanceInvalidStateException - status")
        void shouldCheckAttendanceInvalidStateExceptionStatus() {
            ticket.setStatus(TicketStatus.WAITING);

            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(token.getName()).thenReturn("user-123");
            when(ticketRepository.findByTicketIdAndUnitId("ticket-123", "unit-123"))
                    .thenReturn(Optional.of(ticket));
            when(userRepository.findById("user-123")).thenReturn(Optional.of(user));

            var exception = assertThrows(
                    AttendanceInvalidStateException.class,
                    () -> attendanceService.startAttendance(token, startDto)
            );

            assertEquals("Apenas tickets com status chamado - CALLED podem iniciar atendimento",
                    exception.getMessage());
        }

        @Test
        @DisplayName("Deve verificar mensagem de AttendanceNotFoundException")
        void shouldCheckAttendanceNotFoundExceptionMessage() {
            when(ticketRepository.findByTicketId("ticket-123")).thenReturn(Optional.of(ticket));
            when(attendanceRepository.findByTicket(ticket)).thenReturn(Optional.empty());

            var exception = assertThrows(
                    AttendanceNotFoundException.class,
                    () -> attendanceService.finishAttendance(finishDto)
            );

            assertEquals("Atendimento não encontrado para o ticket: ticket-123",
                    exception.getMessage());
        }
    }
}