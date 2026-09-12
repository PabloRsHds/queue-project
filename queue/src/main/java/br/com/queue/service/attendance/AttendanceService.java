package br.com.queue.service.attendance;

import br.com.queue.dtos.attendance.allAttendances.ResponseAllAttendances;
import br.com.queue.dtos.attendance.start.StartAttendanceDto;
import br.com.queue.dtos.attendance.start.FinishAttendanceDto;
import br.com.queue.dtos.attendance.finish.ResponseAttendanceDto;
import br.com.queue.dtos.attendance.finish.ResponseFinishAttendanceDto;
import br.com.queue.dtos.attendance.statistics.ResponseAttendanceDashboardDto;
import br.com.queue.dtos.ticket.attendance.ResponseTicketsForAttendance;
import br.com.queue.entities.attendance.Attendance;
import br.com.queue.entities.ticket.Ticket;
import br.com.queue.entities.user.User;
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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final UnitContext unitContext;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String ATTENDANCE_TIME_DEFAULT = "00:00:00";

    // =========================================== START ============================================

    @Transactional
    public ResponseAttendanceDto startAttendance(JwtAuthenticationToken token, StartAttendanceDto dto) {
        log.info("Iniciando atendimento para ticket: {}", dto.ticketId());

        var unit = this.unitContext.getCurrentUnit(token);
        var ticket = this.findTicketByTicketIdAndUnitId(dto.ticketId(), unit.getUnitId());
        var user = this.findUserById(token.getName());

        this.validateStartAttendance(ticket, user);

        ticket.setStatus(TicketStatus.IN_PROGRESS);

        var attendance = this.buildAttendanceEntity(ticket, user, unit);

        this.ticketRepository.save(ticket);
        this.attendanceRepository.save(attendance);

        log.info("Atendimento iniciado com sucesso para ticket: {}, atendente: {}",
                ticket.getTicketId(), user.getUserId());

        return this.toStartResponse(attendance);
    }

    private void validateStartAttendance(Ticket ticket, User user) {
        log.debug("Validando permissões para iniciar atendimento do ticket: {}", ticket.getTicketId());

        if (user.getRole() != Role.ATTENDANT && user.getRole() != Role.ADMIN) {
            log.warn("Usuário não autorizado a iniciar atendimento: {}, role: {}",
                    user.getUserId(), user.getRole());
            throw new AttendanceInvalidStateException(
                    "Usuário não tem permissão para iniciar atendimentos"
            );
        }

        if (ticket.getStatus() != TicketStatus.CALLED) {
            log.warn("Tentativa de iniciar atendimento para ticket com status inválido: {}, status: {}",
                    ticket.getTicketId(), ticket.getStatus());
            throw new AttendanceInvalidStateException(
                    "Apenas tickets com status chamado - CALLED podem iniciar atendimento"
            );
        }

        log.debug("Validação concluída para iniciar atendimento do ticket: {}", ticket.getTicketId());
    }

    private Attendance buildAttendanceEntity(Ticket ticket, User user, br.com.queue.entities.unit.Unit unit) {
        log.debug("Construindo entidade Attendance para ticket: {}", ticket.getTicketId());

        var attendance = new Attendance();
        attendance.setTicket(ticket);
        attendance.setUser(user);
        attendance.setStartedAt(LocalDateTime.now());
        attendance.setUnit(unit);

        return attendance;
    }

    // ================================================================================================

    // =========================================== FINISH ============================================

    @Transactional
    public ResponseFinishAttendanceDto finishAttendance(FinishAttendanceDto dto) {
        log.info("Finalizando atendimento para ticket: {}", dto.ticketId());

        var ticket = this.findTicketById(dto.ticketId());
        var attendance = this.findAttendanceByTicket(ticket);

        this.finishAttendanceEntity(attendance, dto);
        ticket.setStatus(TicketStatus.FINISHED);

        this.attendanceRepository.save(attendance);
        this.ticketRepository.save(ticket);

        // Envia notificação WebSocket
        this.sendAttendanceFinishedNotification(ticket, attendance);

        log.info("Atendimento finalizado com sucesso para ticket: {}, resolução: {}",
                ticket.getTicketId(), dto.resolution());

        return this.toFinishResponse(attendance);
    }

    private void finishAttendanceEntity(Attendance attendance, FinishAttendanceDto dto) {
        log.debug("Finalizando entidade Attendance para ticket: {}", attendance.getTicket().getTicketId());

        attendance.setResolution(dto.resolution());
        attendance.setFinishedAt(LocalDateTime.now());
    }

    // ================================================================================================

    // ============================================ GET ALL ==========================================

    public Page<ResponseAllAttendances> getAllAttendances(int page, int size) {
        log.debug("Buscando todos os atendimentos - página: {}, tamanho: {}", page, size);

        return this.attendanceRepository.findAll(PageRequest.of(page, size))
                .map(this::toAllAttendancesResponse);
    }

    // ================================================================================================

    // ============================================ DELETE ===========================================

    @Transactional
    public void deleteAttendance(String attendanceId) {
        log.info("Deletando atendimento: {}", attendanceId);

        var attendance = this.findAttendanceById(attendanceId);
        this.attendanceRepository.delete(attendance);

        log.info("Atendimento deletado com sucesso: {}", attendanceId);
    }

    // ================================================================================================

    // =========================================== STATISTICS ========================================

    public ResponseAttendanceDashboardDto getAttendanceStatistics(JwtAuthenticationToken token) {
        var unit = this.unitContext.getCurrentUnit(token);
        log.debug("Buscando estatísticas de atendimentos para unidade: {}", unit.getUnitId());

        var response = new ResponseAttendanceDashboardDto(
                this.attendanceRepository.countTotalAttendances(unit.getUnitId()),
                this.attendanceRepository.getAverageWaitingTime(unit.getUnitId()),
                this.attendanceRepository.getAverageServiceTime(unit.getUnitId()),
                this.attendanceRepository.averageAttendanceByUser(unit.getUnitId()),
                this.attendanceRepository.countAttendancesCreatedByMonth(unit.getUnitId()),
                this.attendanceRepository.countAttendancesByWeek(unit.getUnitId()),
                this.attendanceRepository.countAttendancesByService(unit.getUnitId()),
                this.attendanceRepository.countAttendancesByHour(unit.getUnitId()),
                this.attendanceRepository.countAttendancesByDepartment(unit.getUnitId()),
                this.attendanceRepository.countAttendancesByCustomer(unit.getUnitId())
        );

        log.debug("Estatísticas de atendimentos coletadas para unidade: {}", unit.getUnitId());
        return response;
    }

    // ================================================================================================

    // ======================================== AUXILIARES - FIND =====================================

    private Ticket findTicketByTicketIdAndUnitId(String ticketId, String unitId) {
        return this.ticketRepository.findByTicketIdAndUnitId(ticketId, unitId)
                .orElseThrow(() -> {
                    log.warn("Ticket não encontrado com ID: {} na unidade: {}", ticketId, unitId);
                    return new TicketNotFoundException(
                            "Ticket não encontrado com ID: " + ticketId + " na unidade: " + unitId
                    );
                });
    }

    private Ticket findTicketById(String ticketId) {
        return this.ticketRepository.findByTicketId(ticketId)
                .orElseThrow(() -> {
                    log.warn("Ticket não encontrado com ID: {}", ticketId);
                    return new TicketNotFoundException("Ticket não encontrado com ID: " + ticketId);
                });
    }

    private User findUserById(String userId) {
        return this.userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Usuário não encontrado com ID: {}", userId);
                    return new UserNotFoundException("Usuário não encontrado com ID: " + userId);
                });
    }

    private Attendance findAttendanceByTicket(Ticket ticket) {
        return this.attendanceRepository.findByTicket(ticket)
                .orElseThrow(() -> {
                    log.warn("Atendimento não encontrado para o ticket: {}", ticket.getTicketId());
                    return new AttendanceNotFoundException(
                            "Atendimento não encontrado para o ticket: " + ticket.getTicketId()
                    );
                });
    }

    private Attendance findAttendanceById(String attendanceId) {
        return this.attendanceRepository.findByAttendanceId(attendanceId)
                .orElseThrow(() -> {
                    log.warn("Atendimento não encontrado com ID: {}", attendanceId);
                    return new AttendanceNotFoundException("Atendimento não encontrado com ID: " + attendanceId);
                });
    }

    // ================================================================================================

    // ======================================== AUXILIARES - WEBSOCKET ===============================

    private void sendAttendanceFinishedNotification(Ticket ticket, Attendance attendance) {
        log.debug("Enviando notificação de atendimento finalizado via WebSocket para o ticket: {}",
                ticket.getTicketId());

        var notification = this.buildResponseTicketsForAttendance(ticket, attendance);
        this.messagingTemplate.convertAndSend("/topic/tickets/history", notification);
    }

    // ================================================================================================

    // ======================================== AUXILIARES - UTILS ====================================

    private String calculateAttendanceTime(Attendance attendance) {
        if (attendance == null || attendance.getFinishedAt() == null) {
            return ATTENDANCE_TIME_DEFAULT;
        }

        Duration duration = Duration.between(attendance.getStartedAt(), attendance.getFinishedAt());
        long seconds = duration.getSeconds();

        return String.format(
                "%02d:%02d:%02d",
                seconds / 3600,
                (seconds % 3600) / 60,
                seconds % 60
        );
    }

    // ================================================================================================

    // ======================================== AUXILIARES - DTO BUILDER =============================

    private ResponseAttendanceDto toStartResponse(Attendance attendance) {
        return new ResponseAttendanceDto(
                attendance.getTicket().getTicketId(),
                attendance.getTicket().getCode(),
                attendance.getStartedAt()
        );
    }

    private ResponseFinishAttendanceDto toFinishResponse(Attendance attendance) {
        return new ResponseFinishAttendanceDto(
                attendance.getResolution(),
                attendance.getFinishedAt()
        );
    }

    private ResponseAllAttendances toAllAttendancesResponse(Attendance attendance) {
        return new ResponseAllAttendances(
                attendance.getTicket().getTicketId(),
                attendance.getTicket().getCode(),
                attendance.getResolution(),
                attendance.getStartedAt(),
                attendance.getFinishedAt()
        );
    }

    private ResponseTicketsForAttendance buildResponseTicketsForAttendance(Ticket ticket, Attendance attendance) {
        var attendanceTime = this.calculateAttendanceTime(attendance);
        var startedAt = attendance.getStartedAt();
        var finishedAt = attendance.getFinishedAt();

        return new ResponseTicketsForAttendance(
                ticket.getTicketId(),
                ticket.getCode(),
                ticket.getStatus().name(),
                ticket.getPriority().name(),
                ticket.getCustomer().getName(),
                ticket.getServiceManagement().getName(),
                ticket.getCreatedAt(),
                startedAt,
                finishedAt,
                attendanceTime
        );
    }

    // ================================================================================================
}