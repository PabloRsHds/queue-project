package br.com.queue.service.ticket;

import br.com.queue.dtos.ticket.ResponseTicketDto;
import br.com.queue.dtos.ticket.allTickets.ResponseAllTicketsDto;
import br.com.queue.dtos.ticket.attendance.ResponseTicketsForAttendance;
import br.com.queue.dtos.ticket.create.CreateTicketDto;
import br.com.queue.dtos.ticket.finishTicket.FinishTicketDto;
import br.com.queue.entities.attendance.Attendance;
import br.com.queue.entities.customer.Customer;
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
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepository;
    private final CustomerRepository customerRepository;
    private final ServiceManagementRepository serviceManagementRepository;
    private final UnitContext unitContext;
    private final ScheduleRepository scheduleRepository;
    private final AttendanceRepository attendanceRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String ATTENDANCE_TIME_DEFAULT = "00:00:00";

    // =========================================== CREATE ===========================================

    @Transactional
    public ResponseTicketDto createTicket(JwtAuthenticationToken token, CreateTicketDto dto) {
        log.info("Criando ticket para scheduleId: {}, customerId: {}, serviceId: {}",
                dto.scheduleId(), dto.customerId(), dto.serviceManagementId());

        var unit = this.unitContext.getCurrentUnit(token);
        var schedule = this.findScheduleById(dto.scheduleId());
        var customer = this.findCustomerById(dto.customerId());
        var serviceManagement = this.findServiceManagementById(dto.serviceManagementId());

        // Verifica se já existe ticket para este agendamento
        var existingTicket = this.findExistingTicketBySchedule(dto.scheduleId());

        if (existingTicket.isPresent()) {
            var ticket = this.updateExistingTicket(existingTicket.get());
            log.info("Ticket existente atualizado: {}", ticket.getTicketId());
            return this.buildResponseTicketDto(ticket);
        }

        // Gera novo número de chamada e cria ticket
        var ticket = this.createNewTicket(unit, schedule, customer, serviceManagement, dto);
        this.ticketRepository.save(ticket);

        // Envia notificação WebSocket
        this.sendTicketCreatedNotification(ticket);

        log.info("Ticket criado com sucesso: {}, código: {}", ticket.getTicketId(), ticket.getCode());
        return this.buildResponseTicketDto(ticket);
    }

    private Ticket updateExistingTicket(Ticket ticket) {
        ticket.setCreatedAt(LocalDateTime.now());
        return this.ticketRepository.save(ticket);
    }

    private Ticket createNewTicket(
            Unit unit,
            Schedule schedule,
            Customer customer,
            ServiceManagement serviceManagement,
            CreateTicketDto dto
    ) {
        var nextCallNumber = this.generateNextCallNumber(serviceManagement);
        return this.buildTicketEntity(unit, schedule, customer, serviceManagement, nextCallNumber, dto);
    }

    private long generateNextCallNumber(ServiceManagement serviceManagement) {
        var nextCallNumber = serviceManagement.getLastTicketNumber() + 1;
        serviceManagement.setLastTicketNumber(nextCallNumber);
        this.serviceManagementRepository.save(serviceManagement);
        return nextCallNumber;
    }

    private Ticket buildTicketEntity(
            br.com.queue.entities.unit.Unit unit,
            Schedule schedule,
            Customer customer,
            ServiceManagement serviceManagement,
            long callNumber,
            CreateTicketDto dto
    ) {
        log.debug("Construindo entidade Ticket para: {}", dto.scheduleId());

        var ticket = new Ticket();
        ticket.setCallNumber(callNumber);
        ticket.setCode(this.generateCode(serviceManagement.getCode(), callNumber));
        ticket.setCustomer(customer);
        ticket.setServiceManagement(serviceManagement);
        ticket.setPriority(PriorityLevel.valueOf(dto.priority()));
        ticket.setStatus(TicketStatus.WAITING);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setSchedule(schedule);
        ticket.setUnit(unit);

        return ticket;
    }

    // ================================================================================================

    // ============================================ UPDATE ===========================================

    @Transactional
    public ResponseTicketDto callTicket(String ticketId) {
        log.info("Chamando ticket: {}", ticketId);

        var entity = this.findTicketById(ticketId);
        entity.setStatus(TicketStatus.CALLED);
        entity.setCalledAt(LocalDateTime.now());
        this.ticketRepository.save(entity);

        var response = this.buildResponseTicketDto(entity);
        this.sendQueueDisplayNotification(response);

        log.info("Ticket chamado com sucesso: {}", ticketId);
        return response;
    }

    @Transactional
    public ResponseTicketDto finishTicket(FinishTicketDto dto) {
        log.info("Finalizando ticket: {}, status: {}", dto.ticketId(), dto.status());

        var entity = this.findTicketById(dto.ticketId());
        entity.setStatus(TicketStatus.valueOf(dto.status()));

        log.info("Ticket finalizado com sucesso: {}", dto.ticketId());
        return this.buildResponseTicketDto(this.ticketRepository.save(entity));
    }

    @Transactional
    public ResponseTicketDto cancelTicket(String ticketId) {
        log.info("Cancelando ticket: {}", ticketId);

        var entity = this.findTicketById(ticketId);
        this.finishAttendanceIfExists(entity);

        entity.setStatus(TicketStatus.CANCELED);

        log.info("Ticket cancelado com sucesso: {}", ticketId);
        return this.buildResponseTicketDto(this.ticketRepository.save(entity));
    }

    // ================================================================================================

    // ============================================ DELETE ===========================================

    @Transactional
    public ResponseTicketDto deleteTicket(String ticketId) {
        log.info("Deletando ticket: {}", ticketId);

        var entity = this.findTicketById(ticketId);
        var response = this.buildResponseTicketDto(entity);

        this.deleteAttendanceIfExists(entity);
        this.ticketRepository.delete(entity);

        log.info("Ticket deletado com sucesso: {}", ticketId);
        return response;
    }

    // ================================================================================================

    // ============================================ GET BY ID =========================================

    public ResponseTicketDto getTicketById(String ticketId) {
        log.debug("Buscando ticket por ID: {}", ticketId);
        var entity = this.findTicketById(ticketId);
        return this.buildResponseTicketDto(entity);
    }

    // ================================================================================================

    // ============================================ GET ALL ===========================================

    public Page<ResponseAllTicketsDto> getAllTickets(int page, int size) {
        log.debug("Buscando todos os tickets - página: {}, tamanho: {}", page, size);

        return this.ticketRepository.findAll(PageRequest.of(page, size))
                .map(this::buildResponseAllTicketsDto);
    }

    // ================================================================================================

    // ====================================== GET BY ATTENDANT ========================================

    public Page<ResponseTicketsForAttendance> getTicketsByAttendant(
            JwtAuthenticationToken token,
            int page,
            int size
    ) {
        var unit = this.unitContext.getCurrentUnit(token);
        log.debug("Buscando tickets do atendente: {}, unidade: {}", token.getName(), unit.getUnitId());

        return this.ticketRepository
                .getTicketsByAttendant(unit.getUnitId(), token.getName(), PageRequest.of(page, size))
                .map(this::buildResponseTicketsForAttendance);
    }

    // ================================================================================================

    // ====================================== GET HISTORY BY ATTENDANT ================================

    public Page<ResponseTicketsForAttendance> getHistoryTicketsByAttendant(
            JwtAuthenticationToken token,
            int page,
            int size
    ) {
        var unit = this.unitContext.getCurrentUnit(token);
        log.debug("Buscando histórico de tickets do atendente: {}, unidade: {}", token.getName(), unit.getUnitId());

        return this.ticketRepository
                .getHistoryTicketsByAttendant(unit.getUnitId(), token.getName(), PageRequest.of(page, size))
                .map(this::buildResponseTicketsForAttendance);
    }

    // ================================================================================================

    // ============================================ CALL CUSTOMER =====================================

    public ResponseTicketDto callCustomer(String ticketId) {
        log.info("Chamando cliente do ticket: {}", ticketId);

        var entity = this.findTicketById(ticketId);
        var response = this.buildResponseTicketDto(entity);

        this.sendCustomerCallNotification(response);

        log.info("Cliente chamado com sucesso para o ticket: {}", ticketId);
        return response;
    }

    // ================================================================================================

    // ============================================ RESET CODE ========================================

    public void resetCode(String ticketId) {
        log.info("Resetando código do ticket: {}", ticketId);

        var entity = this.findTicketById(ticketId);
        entity.setCallNumber(0);
        this.ticketRepository.save(entity);

        log.info("Código resetado com sucesso para o ticket: {}", ticketId);
    }

    // ================================================================================================

    // ======================================== AUXILIARES - FIND =====================================

    private Schedule findScheduleById(String scheduleId) {
        return this.scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> {
                    log.warn("Agendamento não encontrado com ID: {}", scheduleId);
                    return new ScheduleNotFoundException("Agendamento não encontrado com ID: " + scheduleId);
                });
    }

    private Customer findCustomerById(String customerId) {
        return this.customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> {
                    log.warn("Cliente não encontrado com ID: {}", customerId);
                    return new CustomerNotFoundException("Cliente não encontrado com ID: " + customerId);
                });
    }

    private ServiceManagement findServiceManagementById(String serviceManagementId) {
        return this.serviceManagementRepository.findByServiceManagementId(serviceManagementId)
                .orElseThrow(() -> {
                    log.warn("Serviço não encontrado com ID: {}", serviceManagementId);
                    return new ServiceManagementNotFoundException("Serviço não encontrado com ID: " + serviceManagementId);
                });
    }

    private Ticket findTicketById(String ticketId) {
        return this.ticketRepository.findById(ticketId)
                .orElseThrow(() -> {
                    log.warn("Ticket não encontrado com ID: {}", ticketId);
                    return new TicketNotFoundException("Ticket não encontrado com ID: " + ticketId);
                });
    }

    private Optional<Ticket> findExistingTicketBySchedule(String scheduleId) {
        return this.ticketRepository.findTicketByScheduleScheduleId(scheduleId);
    }

    // ================================================================================================

    // ======================================== AUXILIARES - ATTENDANCE ===============================

    private void deleteAttendanceIfExists(Ticket ticket) {
        if (ticket.getAttendance() != null) {
            log.debug("Deletando atendimento do ticket: {}", ticket.getTicketId());
            this.attendanceRepository.delete(ticket.getAttendance());
        }
    }

    private void finishAttendanceIfExists(Ticket ticket) {
        var attendance = ticket.getAttendance();
        if (attendance != null && attendance.getStartedAt() != null) {
            log.debug("Finalizando atendimento do ticket: {}", ticket.getTicketId());
            attendance.setFinishedAt(LocalDateTime.now());
            this.attendanceRepository.save(attendance);
        }
    }

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

    // ======================================== AUXILIARES - WEBSOCKET ================================

    private void sendTicketCreatedNotification(Ticket ticket) {
        var notification = this.buildResponseTicketsForAttendance(ticket);
        this.messagingTemplate.convertAndSend("/topic/tickets", notification);
        log.debug("Notificação de criação enviada via WebSocket para o ticket: {}", ticket.getTicketId());
    }

    private void sendQueueDisplayNotification(ResponseTicketDto response) {
        this.messagingTemplate.convertAndSend("/topic/queue-display", response);
        log.debug("Notificação de fila enviada via WebSocket para o ticket: {}", response.ticketId());
    }

    private void sendCustomerCallNotification(ResponseTicketDto response) {
        this.messagingTemplate.convertAndSend("/topic/queue-display/call", response);
        log.debug("Notificação de chamada de cliente enviada via WebSocket para o ticket: {}", response.ticketId());
    }

    // ================================================================================================

    // ======================================== AUXILIARES - UTILS ====================================

    private String generateCode(String prefix, long callNumber) {
        if (callNumber < 10) {
            return String.format("%s-%02d", prefix, callNumber);
        } else if (callNumber < 100) {
            return String.format("%s-%03d", prefix, callNumber);
        }
        return String.format("%s-%03d", prefix, callNumber);
    }

    private String normalizeSearch(String search) {
        return (search == null || search.isBlank()) ? null : search.trim();
    }

    // ================================================================================================

    // ======================================== AUXILIARES - DTO BUILDER ==============================

    private ResponseTicketDto buildResponseTicketDto(Ticket entity) {
        var calledAt = entity.getCalledAt() != null
                ? entity.getCalledAt().format(DATE_TIME_FORMATTER)
                : "";

        return new ResponseTicketDto(
                entity.getTicketId(),
                entity.getCode(),
                entity.getCustomer().getCustomerId(),
                entity.getCustomer().getName(),
                entity.getServiceManagement().getServiceManagementId(),
                entity.getServiceManagement().getName(),
                entity.getServiceManagement().getDepartment().getName(),
                entity.getPriority().name(),
                entity.getStatus().name(),
                entity.getCreatedAt().format(DATE_TIME_FORMATTER),
                calledAt
        );
    }

    private ResponseAllTicketsDto buildResponseAllTicketsDto(Ticket ticket) {
        return new ResponseAllTicketsDto(
                ticket.getTicketId(),
                ticket.getCode(),
                ticket.getCustomer().getName(),
                ticket.getServiceManagement().getName(),
                ticket.getPriority().name(),
                ticket.getStatus().name(),
                ticket.getCreatedAt()
        );
    }

    private ResponseTicketsForAttendance buildResponseTicketsForAttendance(Ticket ticket) {
        var attendance = ticket.getAttendance();
        var attendanceTime = ATTENDANCE_TIME_DEFAULT;
        LocalDateTime startedAt = null;
        LocalDateTime finishedAt = null;

        if (attendance != null) {
            attendanceTime = this.calculateAttendanceTime(attendance);
            startedAt = attendance.getStartedAt();
            finishedAt = attendance.getFinishedAt();
        }

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