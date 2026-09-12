package br.com.queue.service.department;

import br.com.queue.dtos.department.ResponseDepartmentDto;
import br.com.queue.dtos.department.create.CreateDepartmentDto;
import br.com.queue.dtos.department.statistics.ResponseDepartmentDashBoardDto;
import br.com.queue.dtos.department.update.UpdateDepartmentDto;
import br.com.queue.entities.department.Department;
import br.com.queue.entities.serviceManagement.ServiceManagement;
import br.com.queue.entities.unit.Unit;
import br.com.queue.infra.department.DepartmentNotFoundException;
import br.com.queue.repositories.department.DepartmentRepository;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UnitContext unitContext;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // =========================================== CREATE ===========================================

    @Transactional
    public ResponseDepartmentDto createDepartment(JwtAuthenticationToken token, CreateDepartmentDto dto) {
        log.info("Criando departamento: {}", dto.name());

        var unit = this.unitContext.getCurrentUnit(token);
        var entity = this.buildDepartmentEntity(unit, dto);

        log.info("Departamento criado com sucesso: {}, ID: {}", entity.getName(), entity.getDepartmentId());
        return this.toResponse(this.departmentRepository.save(entity));
    }

    private Department buildDepartmentEntity(
            Unit unit,
            CreateDepartmentDto dto
    ) {
        log.debug("Construindo entidade Department para: {}", dto.name());

        var entity = new Department();
        entity.setName(dto.name());
        entity.setDescription(this.normalizeOptional(dto.description()));
        entity.setActive(true);
        entity.setUnit(unit);
        entity.setCreatedAt(LocalDateTime.now());

        return entity;
    }

    // ================================================================================================

    // =========================================== UPDATE ===========================================

    @Transactional
    public ResponseDepartmentDto updateDepartment(UpdateDepartmentDto dto) {
        log.info("Atualizando departamento: {}", dto.departmentId());

        var entity = this.findDepartmentById(dto.departmentId());
        this.updateDepartmentFields(entity, dto);

        return this.toResponse(this.departmentRepository.save(entity));
    }

    private void updateDepartmentFields(Department entity, UpdateDepartmentDto dto) {
        log.debug("Atualizando campos do departamento: {}", entity.getDepartmentId());

        boolean hasChanges = false;

        if (dto.name() != null && !dto.name().isBlank()) {
            entity.setName(dto.name());
            hasChanges = true;
        }

        if (dto.description() != null) {
            entity.setDescription(dto.description());
            hasChanges = true;
        }

        if (dto.active() != null) {
            entity.setActive(dto.active());
            hasChanges = true;
        }

        if (hasChanges) {
            entity.setUpdatedAt(LocalDateTime.now());
            log.info("Departamento atualizado com sucesso: {}, ID: {}", entity.getName(), entity.getDepartmentId());
        } else {
            log.debug("Nenhuma alteração detectada para o departamento: {}", dto.departmentId());
        }
    }

    // ================================================================================================

    // ============================================ GET ALL ==========================================

    public Page<ResponseDepartmentDto> getAllDepartments(
            JwtAuthenticationToken token,
            int page,
            int size,
            String search
    ) {
        var unit = this.unitContext.getCurrentUnit(token);
        String normalizedSearch = this.normalizeSearch(search);

        log.debug("Buscando departamentos - unidade: {}, página: {}, tamanho: {}, busca: {}",
                unit.getUnitId(), page, size, normalizedSearch);

        return this.departmentRepository.findAllWithSearch(
                unit.getUnitId(),
                normalizedSearch,
                PageRequest.of(page, size)
        ).map(this::toResponse);
    }

    // ================================================================================================

    // =========================================== GET BY ID ==========================================

    public ResponseDepartmentDto getDepartmentById(
            String departmentId
    ) {
        return toResponse(this.findDepartmentById(departmentId));
    }

    // ================================================================================================


    // =========================================== DELETE ===========================================

    @Transactional
    public ResponseDepartmentDto deleteDepartment(String departmentId) {
        log.info("Deletando departamento: {}", departmentId);

        var entity = this.findDepartmentById(departmentId);

        var response = this.toResponse(entity);

        this.departmentRepository.delete(entity);

        log.info("Departamento deletado com sucesso: {}, ID: {}", entity.getName(), departmentId);
        return response;
    }

    // ================================================================================================

    // =========================================== STATISTICS ========================================

    public ResponseDepartmentDashBoardDto getStatistics(JwtAuthenticationToken token) {
        var unit = this.unitContext.getCurrentUnit(token);
        log.debug("Buscando estatísticas de departamentos para unidade: {}", unit.getUnitId());

        var totalDepartment = this.departmentRepository.countTotalDepartmentsStatisticsDto(unit.getUnitId());
        var countServicesByDepartments = this.departmentRepository.countServicesByDepartmentStatisticsDto(unit.getUnitId());
        var departmentPercentages = this.departmentRepository.getDepartmentPercentagesStatisticsDto(unit.getUnitId());
        var departmentsCreatedByMonth = this.departmentRepository.countDepartmentsCreatedByMonth(unit.getUnitId());

        log.debug(
                "Estatísticas coletadas: total={}, " +
                        "serviços ativos por departamento={}," +
                        "serviços inativos por departamento={}, " +
                        "percentuais={}, por mês={}",
                totalDepartment,
                countServicesByDepartments != null ? countServicesByDepartments.size() : 0,
                departmentPercentages != null ? departmentPercentages.percentageActive() : 0,
                departmentPercentages != null ? departmentPercentages.percentageInactive() : 0,
                departmentsCreatedByMonth != null ? departmentsCreatedByMonth.size() : 0);

        return new ResponseDepartmentDashBoardDto(
                totalDepartment,
                countServicesByDepartments,
                departmentPercentages,
                departmentsCreatedByMonth
        );
    }

    // ================================================================================================

    // ======================================== AUXILIARES ============================================

    private Department findDepartmentById(String departmentId) {
        return this.departmentRepository.findByDepartmentId(departmentId)
                .orElseThrow(() -> {
                    log.warn("Departamento não encontrado com ID: {}", departmentId);
                    return new DepartmentNotFoundException("Departamento não encontrado com ID: " + departmentId);
                });
    }

    private String normalizeSearch(String search) {
        return (search == null || search.isBlank()) ? null : search.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private ResponseDepartmentDto toResponse(Department entity) {

        var services = entity.getServices()
                .stream()
                .map(ServiceManagement::getName)
                .collect(Collectors.toList());

        var updatedAt = entity.getUpdatedAt() != null
                ? entity.getUpdatedAt().format(DATE_TIME_FORMATTER)
                : null;

        return new ResponseDepartmentDto(
                entity.getDepartmentId(),
                entity.getName(),
                entity.getDescription(),
                entity.getActive(),
                entity.getCreatedAt().format(DATE_TIME_FORMATTER),
                updatedAt,
                services
        );
    }
}