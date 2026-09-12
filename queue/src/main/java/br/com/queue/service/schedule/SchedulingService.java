package br.com.queue.service.schedule;

import br.com.queue.dtos.schedule.allSchedules.ResponseAllSchedulesDto;
import br.com.queue.dtos.schedule.create.CreateScheduleDto;
import br.com.queue.dtos.schedule.create.ResponseScheduleDto;
import br.com.queue.dtos.schedule.statistics.ResponseScheduleDashBoardDto;
import br.com.queue.dtos.schedule.update.UpdateScheduleDto;
import br.com.queue.entities.customer.Customer;
import br.com.queue.entities.schedule.Schedule;
import br.com.queue.entities.serviceManagement.ServiceManagement;
import br.com.queue.entities.ticket.Ticket;
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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulingService {

    private final ScheduleRepository scheduleRepository;
    private final CustomerRepository customerRepository;
    private final ServiceManagementRepository serviceManagementRepository;
    private final UnitContext unitContext;
    private final TicketRepository ticketRepository;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // =========================================== CREATE ===========================================

    @Transactional
    public ResponseScheduleDto createSchedule(JwtAuthenticationToken token, CreateScheduleDto dto) {
        log.info("Criando agendamento para customerId: {}, serviceId: {}, data: {}",
                dto.customerId(), dto.serviceManagementId(), dto.scheduledDate());

        var unit = this.unitContext.getCurrentUnit(token);
        var customer = this.findCustomerById(dto.customerId());
        var service = this.findServiceManagementById(dto.serviceManagementId());

        var entity = this.buildScheduleEntity(unit, customer, service, dto);

        log.info("Agendamento criado com sucesso: {}", entity.getScheduleId());
        return this.buildResponseScheduleDto(this.scheduleRepository.save(entity));
    }

    // =========================================== UPDATE ===========================================

    @Transactional
    public ResponseScheduleDto updateSchedule(UpdateScheduleDto dto) {
        log.info("Atualizando agendamento: {}", dto.scheduleId());

        var schedule = this.findScheduleById(dto.scheduleId());
        var hasChanges = this.updateScheduleFields(schedule, dto);

        if (hasChanges) {
            schedule.setUpdatedAt(LocalDateTime.now());
            this.scheduleRepository.save(schedule);
            log.info("Agendamento atualizado com sucesso: {}", dto.scheduleId());
        } else {
            log.debug("Nenhuma alteração detectada para o agendamento: {}", dto.scheduleId());
        }

        return this.buildResponseScheduleDto(schedule);
    }

    // ============================================ GET ALL ==========================================

    public Page<ResponseAllSchedulesDto> getAllSchedules(
            JwtAuthenticationToken token,
            int page,
            int size,
            String search,
            LocalDate scheduleDate
    ) {
        var unit = this.unitContext.getCurrentUnit(token);
        String normalizedSearch = this.normalizeSearch(search);

        log.debug("Buscando agendamentos - unidade: {}, página: {}, tamanho: {}, busca: {}, data: {}",
                unit.getUnitId(), page, size, normalizedSearch, scheduleDate);

        return this.scheduleRepository.findAllWithSearch(
                unit.getUnitId(),
                normalizedSearch,
                scheduleDate,
                PageRequest.of(page, size)
        );
    }

    // ============================================ GET BY ID ========================================

    public ResponseScheduleDto getScheduleById(String scheduleId) {
        log.debug("Buscando agendamento por ID: {}", scheduleId);
        var entity = this.findScheduleById(scheduleId);
        return this.buildResponseScheduleDto(entity);
    }

    // =========================================== DELETE ===========================================

    @Transactional
    public ResponseScheduleDto deleteSchedule(String scheduleId) {
        log.info("Deletando agendamento: {}", scheduleId);

        var entity = this.findScheduleById(scheduleId);

        this.validateScheduleDeletion(entity);

        var response = this.buildResponseScheduleDto(entity);
        this.scheduleRepository.delete(entity);

        log.info("Agendamento deletado com sucesso: {}", scheduleId);
        return response;
    }

    // =========================================== STATISTICS ========================================

    public ResponseScheduleDashBoardDto getScheduleStatistics(JwtAuthenticationToken token) {
        var unit = this.unitContext.getCurrentUnit(token);
        log.debug("Buscando estatísticas de agendamentos para unidade: {}", unit.getUnitId());

        return new ResponseScheduleDashBoardDto(
                this.scheduleRepository.countTotalSchedulesStatisticsDto(unit.getUnitId()),
                this.scheduleRepository.getSchedulePercentagesStatisticsDto(unit.getUnitId()),
                this.scheduleRepository.countSchedulesCreatedByMonth(unit.getUnitId()),
                this.scheduleRepository.countSchedulesCreatedByWeek(unit.getUnitId()),
                this.scheduleRepository.countSchedulesCreatedByDay(unit.getUnitId()),
                this.scheduleRepository.countSchedulesByDepartment(unit.getUnitId()),
                this.scheduleRepository.countSchedulesByService(unit.getUnitId()),
                this.scheduleRepository.countSchedulesByPriority(unit.getUnitId()),
                this.scheduleRepository.countSchedulesByHour(unit.getUnitId())
        );
    }

    // ========================================== AUXILIARES =========================================

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

    private Schedule findScheduleById(String scheduleId) {
        return this.scheduleRepository.findByScheduleId(scheduleId)
                .orElseThrow(() -> {
                    log.warn("Agendamento não encontrado com ID: {}", scheduleId);
                    return new ScheduleNotFoundException("Agendamento não encontrado com ID: " + scheduleId);
                });
    }

    private Ticket findTicketById(String ticketId) {
        return this.ticketRepository.findByTicketId(ticketId)
                .orElseThrow(() -> {
                    log.warn("Ticket não encontrado com ID: {}", ticketId);
                    return new TicketNotFoundException("Ticket não encontrado com ID: "+ticketId);
                });
    }

    private String normalizeSearch(String search) {
        return (search == null || search.isBlank()) ? null : search.trim();
    }

    private Schedule buildScheduleEntity(
            br.com.queue.entities.unit.Unit unit,
            Customer customer,
            ServiceManagement service,
            CreateScheduleDto dto
    ) {
        var entity = new Schedule();
        entity.setCustomer(customer);
        entity.setServiceManagement(service);
        entity.setPriority(PriorityLevel.valueOf(dto.priority()));
        entity.setScheduledDate(dto.scheduledDate());
        entity.setStatus(ScheduleStatus.SCHEDULED);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUnit(unit);
        return entity;
    }

    private boolean updateScheduleFields(Schedule schedule, UpdateScheduleDto dto) {
        boolean hasChanges = false;

        if (dto.customerId() != null && !dto.customerId().isBlank()) {
            var customer = this.findCustomerById(dto.customerId());
            schedule.setCustomer(customer);
            hasChanges = true;
        }

        if (dto.serviceManagementId() != null && !dto.serviceManagementId().isBlank()) {
            var serviceManagement = this.findServiceManagementById(dto.serviceManagementId());
            schedule.setServiceManagement(serviceManagement);
            hasChanges = true;
        }

        if (dto.priority() != null) {
            schedule.setPriority(PriorityLevel.valueOf(dto.priority()));
            hasChanges = true;
        }

        if (dto.scheduledDate() != null) {
            schedule.setScheduledDate(dto.scheduledDate());
            hasChanges = true;
        }

        if (dto.status() != null && !dto.status().isBlank()) {
            schedule.setStatus(ScheduleStatus.valueOf(dto.status()));
            hasChanges = true;
        }

        if (hasChanges) {
            log.debug("Campos do agendamento {} foram atualizados", schedule.getScheduleId());
        }

        return hasChanges;
    }

    private void validateScheduleDeletion(Schedule schedule) {
        var ticket = schedule.getTicket();
        if (ticket != null && ticket.getAttendance() != null) {
            log.warn("Tentativa de deletar agendamento com atendimento iniciado: {}", schedule.getScheduleId());
            throw new ScheduleDeleteException(
                    "Não é possível excluir um agendamento quando o atendimento já foi iniciado"
            );
        }
    }

    private ResponseScheduleDto buildResponseScheduleDto(Schedule entity) {
        var updateAt = entity.getUpdatedAt() != null
                ? entity.getUpdatedAt().format(DATE_TIME_FORMATTER)
                : null;

        var ticketInfo = this.getTicketInfo(entity);

        return new ResponseScheduleDto(
                entity.getScheduleId(),
                entity.getCustomer().getCustomerId(),
                entity.getCustomer().getName(),
                entity.getServiceManagement().getServiceManagementId(),
                entity.getServiceManagement().getName(),
                ticketInfo.ticketId,
                ticketInfo.ticketCode,
                entity.getPriority().name(),
                entity.getScheduledDate(),
                entity.getStatus().name(),
                entity.getCreatedAt().format(DATE_TIME_FORMATTER),
                updateAt
        );
    }

    private TicketInfo getTicketInfo(Schedule entity) {
        if (entity.getTicket() != null) {
            var ticketId = entity.getTicket().getTicketId();
            try {
                var ticket = this.findTicketById(ticketId);
                return new TicketInfo(ticketId, ticket.getCode());
            } catch (TicketNotFoundException e) {
                log.warn("Ticket associado ao agendamento não encontrado: {}", ticketId);
                return new TicketInfo(null, null);
            }
        }
        return new TicketInfo(null, null);
    }

    // Record auxiliar para informações do ticket
    private record TicketInfo(String ticketId, String ticketCode) {}
}