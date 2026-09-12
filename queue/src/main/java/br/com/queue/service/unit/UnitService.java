package br.com.queue.service.unit;

import br.com.queue.dtos.unit.CreateUnitDto;
import br.com.queue.dtos.unit.ResponseUnitDto;
import br.com.queue.dtos.unit.UpdateUnitDto;
import br.com.queue.entities.unit.Unit;
import br.com.queue.infra.unit.UnitIsPresentException;
import br.com.queue.infra.unit.UnitNotFoundException;
import br.com.queue.repositories.unit.UnitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnitService {

    private final UnitRepository unitRepository;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ============================================ CREATE ============================================

    @Transactional
    public ResponseUnitDto createUnit(CreateUnitDto dto) {
        log.info("Criando unidade: {}", dto.name());

        this.validateCreateUnit(dto);
        var entity = this.toEntity(dto);

        log.info("Unidade criada com sucesso: {}, ID: {}", entity.getName(), entity.getUnitId());
        return this.toResponse(this.unitRepository.save(entity));
    }

    private Unit toEntity(CreateUnitDto dto) {
        log.debug("Convertendo CreateUnitDto para entidade Unit: {}", dto.name());

        var entity = new Unit();
        entity.setName(dto.name());
        entity.setAddress(this.normalizeOptional(dto.address()));
        entity.setActive(true);
        entity.setCreatedAt(LocalDateTime.now());

        return entity;
    }

    // ================================================================================================

    // ============================================ UPDATE ============================================

    @Transactional
    public ResponseUnitDto updateUnit(UpdateUnitDto dto) {
        log.info("Atualizando unidade: {}", dto.unitId());

        var entity = this.findUnit(dto.unitId());

        this.validateUpdateUnit(dto, entity);
        this.updateEntity(dto, entity);

        log.info("Unidade atualizada com sucesso: {}, ID: {}", entity.getName(), entity.getUnitId());
        return this.toResponse(this.unitRepository.save(entity));
    }

    private void updateEntity(UpdateUnitDto dto, Unit entity) {
        log.debug("Atualizando campos da unidade: {}", entity.getUnitId());

        boolean hasChanges = false;

        if (dto.name() != null && !dto.name().isBlank()) {
            entity.setName(dto.name());
            hasChanges = true;
        }

        if (dto.address() != null) {
            entity.setAddress(dto.address());
            hasChanges = true;
        }

        if (dto.active() != null) {
            entity.setActive(dto.active());
            hasChanges = true;
        }

        if (hasChanges) {
            entity.setUpdatedAt(LocalDateTime.now());
            log.debug("Campos da unidade {} foram atualizados", entity.getUnitId());
        } else {
            log.debug("Nenhuma alteração detectada para a unidade {}", entity.getUnitId());
        }
    }

    // ================================================================================================

    // ============================================ DELETE ============================================

    @Transactional
    public ResponseUnitDto deleteUnit(String unitId) {
        log.info("Deletando unidade: {}", unitId);

        var entity = this.findUnit(unitId);
        var response = this.toResponse(entity);

        this.unitRepository.delete(entity);

        log.info("Unidade deletada com sucesso: {}, ID: {}", entity.getName(), unitId);
        return response;
    }

    // ================================================================================================

    // ================================= GET BY ID ====================================================

    public ResponseUnitDto getById(String unitId) {
        return this.toResponse(this.findUnit(unitId));
    }
    // ================================================================================================

    // ============================================ GET ALL ===========================================

    public Page<ResponseUnitDto> getAllUnits(int page, int size, String search) {
        String normalizedSearch = this.normalizeSearch(search);

        log.debug("Buscando unidades - página: {}, tamanho: {}, busca: {}",
                page, size, normalizedSearch);

        return this.unitRepository.findAllWithSearch(
                normalizedSearch,
                PageRequest.of(page, size)
        );
    }

    // ================================================================================================

    // ======================================== AUXILIARES ============================================

    private Unit findUnit(String unitId) {
        return this.unitRepository.findById(unitId)
                .orElseThrow(() -> {
                    log.warn("Unidade não encontrada com ID: {}", unitId);
                    return new UnitNotFoundException("Unidade não encontrada com ID: " + unitId);
                });
    }

    private void validateCreateUnit(CreateUnitDto dto) {
        log.debug("Validando dados para criação da unidade: {}", dto.name());

        if (this.unitRepository.existsByName(dto.name())) {
            log.warn("Tentativa de criar unidade com nome já existente: {}", dto.name());
            throw new UnitIsPresentException("Uma unidade com o nome '" + dto.name() + "' já existe.");
        }

        log.debug("Validação concluída com sucesso para a unidade: {}", dto.name());
    }

    private void validateUpdateUnit(UpdateUnitDto dto, Unit entity) {
        log.debug("Validando dados para atualização da unidade: {}", entity.getUnitId());

        if (dto.name() != null
                && !dto.name().isBlank()
                && !dto.name().equals(entity.getName())
                && this.unitRepository.existsByName(dto.name())) {

            log.warn("Tentativa de atualizar unidade com nome já existente: {}", dto.name());
            throw new UnitIsPresentException("Uma unidade com o nome '" + dto.name() + "' já existe.");
        }

        log.debug("Validação de atualização concluída para a unidade: {}", entity.getUnitId());
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

    private ResponseUnitDto toResponse(Unit entity) {
        var updatedAt = entity.getUpdatedAt() != null
                ? entity.getUpdatedAt().format(DATE_FORMATTER)
                : null;

        return new ResponseUnitDto(
                entity.getUnitId(),
                entity.getName(),
                entity.getAddress(),
                entity.getActive(),
                entity.getCreatedAt() != null
                        ? entity.getCreatedAt().format(DATE_FORMATTER)
                        : null,
                updatedAt
        );
    }
}