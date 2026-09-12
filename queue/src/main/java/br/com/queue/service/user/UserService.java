package br.com.queue.service.user;

import br.com.queue.dtos.user.ResponseUserDto;
import br.com.queue.dtos.user.create.CreateUserDto;
import br.com.queue.dtos.user.get_user.ResponseUserInfoDto;
import br.com.queue.dtos.user.metrics.ResponseUserDashBoardDto;
import br.com.queue.dtos.user.update.UpdateUserDto;
import br.com.queue.entities.serviceManagement.ServiceManagement;
import br.com.queue.entities.unit.Unit;
import br.com.queue.entities.user.User;
import br.com.queue.enums.Role;
import br.com.queue.infra.user.UserNotFoundException;
import br.com.queue.infra.user.UserValidationException;
import br.com.queue.repositories.serviceManagement.ServiceManagementRepository;
import br.com.queue.repositories.user.UserRepository;
import br.com.queue.service.unit.UnitContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final ServiceManagementRepository serviceManagementRepository;
    private final UnitContext unitContext;
    private final PasswordEncoder passwordEncoder;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // =========================================== CREATE ===========================================================

    @Transactional
    public ResponseUserDto createUser(JwtAuthenticationToken token, CreateUserDto dto) {
        log.info("Criando usuário: {}", dto.username());

        // Verifico em que unidade o usuario está logado,
        // e passo ao usuario que for criado para o sistema
        var unit = this.unitContext.getCurrentUnit(token);
        this.validateCreateUser(dto);
        var entity = this.toEntity(dto, unit);

        log.info("Usuário criado com sucesso: {}, ID: {}", entity.getUsername(), entity.getUserId());
        return this.toResponse(this.userRepository.save(entity));
    }

    public void validateCreateUser(CreateUserDto dto) {
        log.debug("Validando dados para criação do usuário: {}", dto.username());

        var verifyUsername = this.userRepository.existsByUsername(dto.username());
        var verifyEmail = this.userRepository.existsByEmail(dto.email());
        var verifyPhone = this.userRepository.existsByPhone(dto.phone());
        var verifyCounterNumber = this.userRepository.existsByCounterNumber(dto.counterNumber());

        if (verifyUsername) {
            log.warn("Tentativa de criar usuário com username já existente: {}", dto.username());
            throw new UserValidationException("Usuário já cadastrado com username: " + dto.username());
        }

        if (verifyEmail) {
            log.warn("Tentativa de criar usuário com e-mail já existente: {}", dto.email());
            throw new UserValidationException("E-mail já cadastrado: " + dto.email());
        }

        if (dto.phone() != null
                && verifyPhone) {
            throw new UserValidationException("Telefone já cadastrado: " + dto.phone());
        }

        if (dto.counterNumber() != null
                && verifyCounterNumber) {
            throw new UserValidationException("Guichê já alocado: " + dto.counterNumber());
        }


        log.debug("Validação concluída com sucesso para o usuário: {}", dto.username());
    }

    public User toEntity(CreateUserDto dto, Unit unit) {
        log.debug("Convertendo CreateUserDto para entidade User: {}", dto.username());

        Set<ServiceManagement> services =
                serviceManagementRepository.findAllByServiceManagementIdIn(dto.serviceIds());

        var entity = new User();
        entity.setUsername(dto.username());
        entity.setName(dto.name());
        entity.setSurname(dto.surname());
        entity.setPhone(this.normalizeOptional(dto.phone()));
        entity.setEmail(this.normalizeOptional(dto.email()));
        entity.setPassword(passwordEncoder.encode(dto.password()));
        entity.setRole(Role.valueOf(dto.role()));
        entity.setCounterNumber(dto.counterNumber());
        entity.setActive(true);
        entity.setServices(services);
        entity.setUnit(unit);
        entity.setCreatedAt(LocalDateTime.now());

        return entity;
    }

    // ===============================================================================================================

    // ============================================== UPDATE =========================================================
    @Transactional
    public ResponseUserDto updateUser(UpdateUserDto dto) {
        log.info("Atualizando usuário: {}", dto.userId());

        var entity = this.findUser(dto.userId());

        this.validateUpdateUser(dto, entity);
        this.updateEntity(dto, entity);

        log.info("Usuário atualizado com sucesso: {}, ID: {}", entity.getUsername(), entity.getUserId());
        return this.toResponse(this.userRepository.save(entity));
    }

    private void validateUpdateUser(UpdateUserDto dto, User entity) {
        log.debug("Validando dados para atualização do usuário: {}", entity.getUserId());

        if (dto.username() != null
                && !dto.username().isBlank()
                && !dto.username().equals(entity.getUsername())
                && this.userRepository.existsByUsername(dto.username())) {

            log.warn("Tentativa de atualizar usuário com username já existente: {}", dto.username());
            throw new UserValidationException("Usuário já cadastrado com username: " + dto.username());
        }

        if (dto.email() != null
                && !dto.email().isBlank()
                && !dto.email().equals(entity.getEmail())
                && this.userRepository.existsByEmail(dto.email())) {

            log.warn("Tentativa de atualizar usuário com e-mail já existente: {}", dto.email());
            throw new UserValidationException("E-mail já cadastrado: " + dto.email());
        }

        if (dto.phone() != null
                && !dto.phone().isBlank()
                && !dto.phone().equals(entity.getPhone())
                && this.userRepository.existsByPhone(dto.phone())) {

            log.warn("Tentativa de atualizar usuário com telefone já existente: {}", dto.phone());
            throw new UserValidationException("Telefone já cadastrado: " + dto.phone());
        }

        if (dto.counterNumber() != null
                && !dto.counterNumber().equals(entity.getCounterNumber())
                && this.userRepository.existsByCounterNumber(dto.counterNumber())) {

            log.warn("Tentativa de atualizar usuário com guichê já alocado: {}", dto.counterNumber());
            throw new UserValidationException("Guichê já alocado: " + dto.counterNumber());
        }

        log.debug("Validação de atualização concluída para o usuário: {}", entity.getUserId());
    }

    private void updateEntity(UpdateUserDto dto, User entity) {
        log.debug("Atualizando campos do usuário: {}", entity.getUserId());

        boolean hasChanges = false;

        if (dto.username() != null && !dto.username().isBlank()) {
            entity.setUsername(dto.username());
            hasChanges = true;
        }

        if (dto.name() != null && !dto.name().isBlank()) {
            entity.setName(dto.name());
            hasChanges = true;
        }

        if (dto.surname() != null && !dto.surname().isBlank()) {
            entity.setSurname(dto.surname());
            hasChanges = true;
        }

        if (dto.phone() != null && !dto.phone().isBlank()) {
            entity.setPhone(dto.phone());
            hasChanges = true;
        }

        if (dto.email() != null && !dto.email().isBlank()) {
            entity.setEmail(dto.email());
            hasChanges = true;
        }

        if (dto.role() != null && !dto.role().isBlank()) {
            entity.setRole(Role.valueOf(dto.role()));
            hasChanges = true;

            if (!Role.ATTENDANT.name().equals(dto.role())) {
                entity.setCounterNumber(null);
                log.debug("Role alterada para {}, removendo contador", dto.role());
            }
        }

        if (dto.counterNumber() != null) {
            entity.setCounterNumber(dto.counterNumber());
            hasChanges = true;
        }

        if (dto.active() != null) {
            entity.setActive(dto.active());
            hasChanges = true;
        }

        if (dto.serviceIds() != null) {
            Set<ServiceManagement> services = new HashSet<>(
                    this.serviceManagementRepository.findAllByServiceManagementIdIn(dto.serviceIds())
            );
            entity.setServices(services);
            hasChanges = true;
            log.debug("Serviços do usuário atualizados: {}", dto.serviceIds().size() + " serviços");
        }

        if (dto.password() != null && !dto.password().isBlank()) {
            entity.setPassword(passwordEncoder.encode(dto.password()));
            hasChanges = true;
            log.debug("Senha do usuário atualizada");
        }

        if (hasChanges) {
            entity.setUpdatedAt(LocalDateTime.now());
            log.debug("Campos do usuário {} foram atualizados", entity.getUserId());
        } else {
            log.debug("Nenhuma alteração detectada para o usuário {}", entity.getUserId());
        }
    }

    // ==============================================================================================================

    // ================================================= DELETE =====================================================
    @Transactional
    public ResponseUserDto deleteUser(String userId) {
        log.info("Deletando usuário: {}", userId);

        var entity = this.findUser(userId);
        var response = this.toResponse(entity);
        this.userRepository.delete(entity);

        log.info("Usuário deletado com sucesso: {}, ID: {}", entity.getUsername(), userId);
        return response;
    }
    // ==============================================================================================================

    // ============================================== ALL USERS =====================================================
    public Page<ResponseUserDto> getAllUsers(JwtAuthenticationToken token, int page, int size, String search) {
        var unit = this.unitContext.getCurrentUnit(token);

        String normalizedSearch = (search == null || search.isBlank())
                ? null
                : search.trim();

        log.debug("Buscando usuários - unidade: {}, página: {}, tamanho: {}, busca: {}",
                unit.getUnitId(), page, size, normalizedSearch);

        return this.userRepository.findAllWithSearch(
                unit.getUnitId(),
                normalizedSearch,
                PageRequest.of(page, size));
    }
    // ==============================================================================================================

    // ========================================== USER INFO =========================================================
    public ResponseUserInfoDto getUserById(String userId) {
        log.debug("Buscando usuário por ID: {}", userId);

        return this.toInfoResponse(this.findUser(userId));
    }
    // ==============================================================================================================

    // ====================================== GET USER BY TOKEN =====================================================
    public ResponseUserInfoDto getUserByToken(JwtAuthenticationToken token) {
        log.debug("Buscando usuário pelo token: {}", token.getName());

        return this.toInfoResponse(this.findUser(token.getName()));
    }
    // ==============================================================================================================

    // ========================================== GET STATISTICS ====================================================
    public ResponseUserDashBoardDto getUserStatistics(JwtAuthenticationToken token) {
        var unit = this.unitContext.getCurrentUnit(token);

        log.debug("Buscando estatísticas de usuários para unidade: {}", unit.getUnitId());
        var countTotalUsersStatistics = this.userRepository.countTotalUsersStatisticsDto(unit.getUnitId());
        var userPercentagesStatistics = this.userRepository.getUserPercentagesStatisticsDto(unit.getUnitId());
        var usersCreatedByMonthStatistics = this.userRepository.countUsersCreatedByMonth(unit.getUnitId());
        var countServicesByUsers = this.userRepository.countServicesByUserStatistics(unit.getUnitId());
        var countRoleByUsers = this.userRepository.countUsersByRoleStatistics(unit.getUnitId());

        log.debug("Estatísticas coletadas: total={}, percentuais ativos={}, percentuais inativos={}, por mês={}",
                countTotalUsersStatistics,
                userPercentagesStatistics != null ? userPercentagesStatistics.percentageActive() : 0,
                userPercentagesStatistics != null ? userPercentagesStatistics.percentageInactive() : 0,
                usersCreatedByMonthStatistics != null ? usersCreatedByMonthStatistics.size() : 0);

        return new ResponseUserDashBoardDto(
                countTotalUsersStatistics,
                userPercentagesStatistics,
                usersCreatedByMonthStatistics,
                countServicesByUsers,
                countRoleByUsers
        );
    }
    // ===============================================================================================================

    // Serviços auxiliares
    public ResponseUserDto toResponse(User entity) {
        var updateAt = entity.getUpdatedAt() != null
                ? entity.getUpdatedAt()
                : null;

        return new ResponseUserDto(
                entity.getUserId(),
                entity.getUsername(),
                entity.getName(),
                entity.getSurname(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getRole().name(),
                entity.getCounterNumber(),
                entity.getActive(),
                entity.getCreatedAt(),
                updateAt
        );
    }

    public ResponseUserInfoDto toInfoResponse(User entity) {
        var updateAt = entity.getUpdatedAt() != null
                ? entity.getUpdatedAt().format(DATE_FORMATTER)
                : null;

        return new ResponseUserInfoDto(
                entity.getUserId(),
                entity.getUsername(),
                entity.getName(),
                entity.getSurname(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getRole().name(),
                entity.getCounterNumber(),
                entity.getActive(),
                entity.getCreatedAt().format(DATE_FORMATTER),
                updateAt,
                entity.getServices()
                        .stream()
                        .map(ServiceManagement::getName)
                        .collect(Collectors.toSet())
        );
    }

    private User findUser(String userId) {
        return this.userRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("Usuário não encontrado com ID: {}", userId);
                    return new UserNotFoundException("Usuário não encontrado com ID: " + userId);
                });
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}