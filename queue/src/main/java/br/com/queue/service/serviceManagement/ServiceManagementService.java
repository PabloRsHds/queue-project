package br.com.queue.service.serviceManagement;

import br.com.queue.dtos.serviceManagement.ResponseServiceManagementDto;
import br.com.queue.dtos.serviceManagement.create.CreateServiceManagementDto;
import br.com.queue.dtos.serviceManagement.getServiceDto.ResponseGetServiceByIdDto;
import br.com.queue.dtos.serviceManagement.list_service.ResponseServicesForCreatedUser;
import br.com.queue.dtos.serviceManagement.statistics.ResponseServiceDashBoardDto;
import br.com.queue.dtos.serviceManagement.update.UpdateServiceManagementDto;
import br.com.queue.entities.department.Department;
import br.com.queue.entities.serviceManagement.ServiceManagement;
import br.com.queue.infra.department.DepartmentNotFoundException;
import br.com.queue.infra.serviceManagement.ServiceManagementAlreadyExistsException;
import br.com.queue.infra.serviceManagement.ServiceManagementNotFoundException;
import br.com.queue.repositories.department.DepartmentRepository;
import br.com.queue.repositories.serviceManagement.ServiceManagementRepository;
import br.com.queue.service.unit.UnitContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceManagementService {

    private final ServiceManagementRepository serviceRepository;
    private final DepartmentRepository departmentRepository;
    private final UnitContext unitContext;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // =========================================== CREATE ===========================================

    @Transactional
    public ResponseServiceManagementDto createServiceManagement(
            JwtAuthenticationToken token,
            CreateServiceManagementDto dto
    ) {
        log.info("Criando serviço: {}", dto.name());

        var unit = this.unitContext.getCurrentUnit(token);

        this.validateCreate(dto);

        var department = this.findDepartment(dto.departmentName());

        var entity = this.toEntity(dto, department);
        entity.setUnit(unit);

        log.info("Serviço criado com sucesso: {}, ID: {}", entity.getName(), entity.getServiceManagementId());
        return toResponse(this.serviceRepository.save(entity));
    }

    private void validateCreate(CreateServiceManagementDto dto) {
        if (this.serviceRepository.findByName(dto.name()).isPresent()) {
            log.warn("Tentativa de criar serviço com nome já existente: {}", dto.name());
            throw new ServiceManagementAlreadyExistsException(
                    "Já existe um serviço com o nome: " + dto.name()
            );
        }
    }

    private ServiceManagement toEntity(
            CreateServiceManagementDto dto,
            Department department
    ) {
        var entity = new ServiceManagement();
        entity.setName(dto.name());
        entity.setCode(dto.code());
        entity.setDescription(this.normalizeOptional(dto.description()));
        entity.setDepartment(department);
        entity.setActive(true);
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }

    // ==============================================================================================

    // =========================================== UPDATE ===========================================

    @Transactional
    public ResponseServiceManagementDto updateServiceManagement(
            UpdateServiceManagementDto dto
    ) {
        log.info("Atualizando serviço: {}", dto.serviceManagementId());

        var entity = this.findService(dto.serviceManagementId());

        this.validateUpdate(dto, entity);
        this.updateEntity(dto, entity);

        this.serviceRepository.save(entity);

        log.info("Serviço atualizado com sucesso: {}, ID: {}", entity.getName(), entity.getServiceManagementId());
        return this.toResponse(entity);
    }

    private void validateUpdate(
            UpdateServiceManagementDto dto,
            ServiceManagement entity
    ) {
        if (dto.name() != null
                && !dto.name().isBlank()
                && !dto.name().equals(entity.getName())
                && serviceRepository.findByName(dto.name()).isPresent()) {

            log.warn("Tentativa de atualizar serviço com nome já existente: {}", dto.name());
            throw new ServiceManagementAlreadyExistsException(
                    "Já existe um serviço com o nome: " + dto.name()
            );
        }
    }

    private void updateEntity(
            UpdateServiceManagementDto dto,
            ServiceManagement entity
    ) {
        boolean hasChanges = false;

        if (dto.name() != null && !dto.name().isBlank()) {
            entity.setName(dto.name());
            hasChanges = true;
        }

        if (dto.code() != null && !dto.code().isBlank()) {
            entity.setCode(dto.code());
            hasChanges = true;
        }

        if (dto.description() != null) {
            entity.setDescription(dto.description());
            hasChanges = true;
        }

        if (dto.departmentName() != null && !dto.departmentName().isBlank()) {
            entity.setDepartment(findDepartment(dto.departmentName()));
            hasChanges = true;
        }

        if (dto.active() != null) {
            entity.setActive(dto.active());
            hasChanges = true;
        }

        if (hasChanges) {
            entity.setUpdatedAt(LocalDateTime.now());
            log.debug("Campos do serviço {} foram atualizados", entity.getServiceManagementId());
        } else {
            log.debug("Nenhuma alteração detectada para o serviço {}", entity.getServiceManagementId());
        }
    }

    // ==============================================================================================

    // =========================================== DELETE ===========================================

    @Transactional
    public ResponseServiceManagementDto deleteServiceManagement(
            String serviceManagementId
    ) {
        log.info("Deletando serviço: {}", serviceManagementId);

        var entity = this.findService(serviceManagementId);
        var response = this.toResponse(entity);

        // Remove associações antes de deletar
        log.debug("Removendo associações de usuários para o serviço: {}", serviceManagementId);
        this.serviceRepository.deleteUserServicesByServiceId(
                entity.getServiceManagementId()
        );

        this.serviceRepository.delete(entity);

        log.info("Serviço deletado com sucesso: {}", serviceManagementId);
        return response;
    }

    // ==============================================================================================

    // ============================================ GET ALL ==========================================

    public Page<ResponseServiceManagementDto> getAllServicesManagement(
            JwtAuthenticationToken token,
            int page,
            int size,
            String search
    ) {
        var unit = this.unitContext.getCurrentUnit(token);

        String normalizedSearch = this.normalizeSearch(search);

        log.debug("Buscando serviços - unidade: {}, página: {}, tamanho: {}, busca: {}",
                unit.getUnitId(), page, size, normalizedSearch);

        return this.serviceRepository.findAllWithSearch(
                unit.getUnitId(),
                normalizedSearch,
                PageRequest.of(page, size)
        );
    }

    // ==============================================================================================

    // ============================================ GET BY ID ========================================

    public ResponseGetServiceByIdDto getServiceManagementById(
            String serviceManagementId
    ) {
        log.debug("Buscando serviço por ID: {}", serviceManagementId);
        return this.toInfoResponse(findService(serviceManagementId));
    }

    // ==============================================================================================

    // ======================================== SERVICES USER ========================================

    public List<ResponseServicesForCreatedUser> servicesForCreatedUser() {
        log.debug("Buscando serviços ativos para criação de tickets");

        return this.serviceRepository.findAll()
                .stream()
                .filter(ServiceManagement::getActive)
                .map(service ->
                        new ResponseServicesForCreatedUser(
                                service.getServiceManagementId(),
                                service.getName(),
                                service.getDepartment().getName()
                        )
                )
                .toList();
    }

    // ==============================================================================================

    // =========================================== DASHBOARD =========================================

    public ResponseServiceDashBoardDto getStatistics(
            JwtAuthenticationToken token
    ) {
        var unit = this.unitContext.getCurrentUnit(token);
        log.debug("Buscando estatísticas para unidade: {}", unit.getUnitId());

        return new ResponseServiceDashBoardDto(
                serviceRepository.countTotalServicesStatisticsDto(unit.getUnitId()),
                serviceRepository.getServicePercentagesStatisticsDto(unit.getUnitId()),
                serviceRepository.countServicesCreatedByMonth(unit.getUnitId()),
                serviceRepository.countServicesByDepartmentStatistics(unit.getUnitId()),
                serviceRepository.countUsersByServiceStatistics(unit.getUnitId()),
                serviceRepository.countSchedulesByServiceStatistics(unit.getUnitId()),
                serviceRepository.countTicketsByServiceStatistics(unit.getUnitId())
        );
    }

    // ==============================================================================================

    // ========================================== AUXILIARES =========================================

    private ServiceManagement findService(String id) {
        return this.serviceRepository.findByServiceManagementId(id)
                .orElseThrow(() -> {
                    log.warn("Serviço não encontrado com ID: {}", id);
                    return new ServiceManagementNotFoundException(
                            "Serviço não encontrado com ID: " + id
                    );
                });
    }

    private Department findDepartment(String name) {
        return this.departmentRepository.findByName(name)
                .orElseThrow(() -> {
                    log.warn("Departamento não encontrado: {}", name);
                    return new DepartmentNotFoundException(
                            "Departamento não encontrado com nome: " + name
                    );
                });
    }

    private String normalizeSearch(String search) {
        return (search == null || search.isBlank()) ? null : search.trim();
    }

    private ResponseServiceManagementDto toResponse(
            ServiceManagement entity
    ) {
        return new ResponseServiceManagementDto(
                entity.getServiceManagementId(),
                entity.getName(),
                entity.getCode(),
                entity.getDescription(),
                entity.getDepartment().getDepartmentId(),
                entity.getDepartment().getName(),
                entity.getActive()
        );
    }

    private ResponseGetServiceByIdDto toInfoResponse(
            ServiceManagement entity
    ) {
        var updatedAt = entity.getUpdatedAt() != null
                ? entity.getUpdatedAt().format(DATE_FORMATTER)
                : null;

        return new ResponseGetServiceByIdDto(
                entity.getServiceManagementId(),
                entity.getName(),
                entity.getCode(),
                entity.getDescription(),
                entity.getDepartment().getDepartmentId(),
                entity.getDepartment().getName(),
                entity.getActive(),
                entity.getCreatedAt().format(DATE_FORMATTER),
                updatedAt
        );
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}