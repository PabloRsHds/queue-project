package br.com.queue.service.customer;

import br.com.queue.dtos.customer.allCustomer.ResponseAllCustomersDto;
import br.com.queue.dtos.customer.create.CreateCustomerDto;
import br.com.queue.dtos.customer.create.ResponseCustomerDto;
import br.com.queue.dtos.customer.getCustomer.ResponseCustomerById;
import br.com.queue.dtos.customer.getCustomer.ResponseGetCustomerIdsAndNames;
import br.com.queue.dtos.customer.statistics.ResponseCustomerDashBoardDto;
import br.com.queue.dtos.customer.update.UpdateCustomerDto;
import br.com.queue.entities.customer.Customer;
import br.com.queue.entities.ticket.Ticket;
import br.com.queue.entities.unit.Unit;
import br.com.queue.infra.customer.CustomerNotFoundException;
import br.com.queue.infra.customer.CustomerValidationException;
import br.com.queue.repositories.customer.CustomerRepository;
import br.com.queue.service.unit.UnitContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UnitContext unitContext;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // =========================================== CREATE ===========================================

    @Transactional
    public ResponseCustomerDto registerCustomer(JwtAuthenticationToken token, CreateCustomerDto dto) {
        log.info("Registrando cliente: {}", dto.name());

        var unit = this.unitContext.getCurrentUnit(token);
        this.validateCreateCustomer(dto);
        var entity = this.buildCustomerEntity(unit, dto);

        log.info("Cliente registrado com sucesso: {}, ID: {}", entity.getName(), entity.getCustomerId());
        return this.toResponse(this.customerRepository.save(entity));
    }

    private void validateCreateCustomer(CreateCustomerDto dto) {
        log.debug("Validando dados para criação do cliente: {}", dto.name());

        var verifyCpf = this.customerRepository.existsByCpf(dto.cpf());
        var verifyRg = this.customerRepository.existsByRg(dto.rg());
        var verifyPhone = this.customerRepository.existsByPhone(dto.phone());
        var verifyEmail = this.customerRepository.existsByEmail(dto.email());

        if (verifyCpf) {
            log.warn("Tentativa de criar cliente com CPF já existente: {}", dto.cpf());
            throw new CustomerValidationException("Já existe um cliente com este CPF.");
        }

        if (verifyRg) {
            log.warn("Tentativa de criar cliente com RG já existente: {}", dto.rg());
            throw new CustomerValidationException("Já existe um cliente com este RG.");
        }

        if (verifyPhone) {
            log.warn("Tentativa de criar cliente com telefone já existente: {}", dto.phone());
            throw new CustomerValidationException("Já existe um cliente com este telefone.");
        }

        if (verifyEmail) {
            log.warn("Tentativa de criar cliente com e-mail já existente: {}", dto.email());
            throw new CustomerValidationException("Já existe um cliente com este e-mail.");
        }

        log.debug("Validação concluída com sucesso para o cliente: {}", dto.name());
    }

    private Customer buildCustomerEntity(
            Unit unit,
            CreateCustomerDto dto
    ) {
        log.debug("Construindo entidade Customer para: {}", dto.name());

        var entity = new Customer();
        entity.setName(dto.name());
        entity.setCpf(this.normalizeOptional(dto.cpf()));
        entity.setRg(this.normalizeOptional(dto.rg()));
        entity.setPhone(this.normalizeOptional(dto.phone()));
        entity.setEmail(this.normalizeOptional(dto.email()));
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUnit(unit);

        return entity;
    }

    // ================================================================================================

    // =========================================== UPDATE ===========================================

    @Transactional
    public ResponseCustomerDto updateCustomer(UpdateCustomerDto dto) {
        log.info("Atualizando cliente: {}", dto.customerId());

        var entity = this.findCustomerById(dto.customerId());

        // Verifica se as novas credenciais já existem em outro cliente
        this.validateUpdateCredentials(dto, entity);

        this.updateCustomerFields(entity, dto);

        return this.toResponse(this.customerRepository.save(entity));
    }

    private void validateUpdateCredentials(UpdateCustomerDto dto, Customer entity) {
        log.debug("Validando credenciais para atualização do cliente: {}", entity.getCustomerId());

        // Verifica CPF
        if (dto.cpf() != null
                && !dto.cpf().isBlank()
                && !dto.cpf().equals(entity.getCpf())
                && this.customerRepository.existsByCpf(dto.cpf())) {
            // REMOVIDO o if duplicado
            log.warn("Tentativa de atualizar cliente com CPF já existente: {}", dto.cpf());
            throw new CustomerValidationException("Já existe um cliente com este CPF.");
        }

        // Verifica RG
        if (dto.rg() != null
                && !dto.rg().isBlank()
                && !dto.rg().equals(entity.getRg())
                && this.customerRepository.existsByRg(dto.rg())) {
            // REMOVIDO o if duplicado
            log.warn("Tentativa de atualizar cliente com RG já existente: {}", dto.rg());
            throw new CustomerValidationException("Já existe um cliente com este RG.");
        }

        // Verifica Phone
        if (dto.phone() != null
                && !dto.phone().isBlank()
                && !dto.phone().equals(entity.getPhone())
                && this.customerRepository.existsByPhone(dto.phone())) {
            // REMOVIDO o if duplicado
            log.warn("Tentativa de atualizar cliente com telefone já existente: {}", dto.phone());
            throw new CustomerValidationException("Já existe um cliente com este telefone.");
        }

        // Verifica Email
        if (dto.email() != null
                && !dto.email().isBlank()
                && !dto.email().equals(entity.getEmail())
                && this.customerRepository.existsByEmail(dto.email())) {
            // REMOVIDO o if duplicado
            log.warn("Tentativa de atualizar cliente com e-mail já existente: {}", dto.email());
            throw new CustomerValidationException("Já existe um cliente com este e-mail.");
        }

        log.debug("Validação de atualização concluída para o cliente: {}", entity.getCustomerId());
    }

    private void updateCustomerFields(Customer entity, UpdateCustomerDto dto) {
        log.debug("Atualizando campos do cliente: {}", entity.getCustomerId());

        boolean hasChanges = false;

        if (dto.name() != null && !dto.name().isBlank()) {
            entity.setName(dto.name());
            hasChanges = true;
        }

        if (dto.cpf() != null && !dto.cpf().isBlank()) {
            entity.setCpf(dto.cpf());
            hasChanges = true;
        }

        if (dto.rg() != null && !dto.rg().isBlank()) {
            entity.setRg(dto.rg());
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

        if (hasChanges) {
            entity.setUpdatedAt(LocalDateTime.now());
            log.info("Cliente atualizado com sucesso: {}, ID: {}", entity.getName(), entity.getCustomerId());
        } else {
            log.debug("Nenhuma alteração detectada para o cliente: {}", dto.customerId());
        }
    }

    // ================================================================================================

    // ============================================ GET ALL ==========================================

    public Page<ResponseAllCustomersDto> getAllCustomers(
            JwtAuthenticationToken token,
            int page,
            int size,
            String search
    ) {
        var unit = this.unitContext.getCurrentUnit(token);
        String normalizedSearch = this.normalizeSearch(search);

        log.debug("Buscando clientes - unidade: {}, página: {}, tamanho: {}, busca: {}",
                unit.getUnitId(), page, size, normalizedSearch);

        return this.customerRepository.findAllWithSearch(
                unit.getUnitId(),
                normalizedSearch,
                PageRequest.of(page, size)
        );
    }

    // ================================================================================================

    // ============================================ GET NAMES ========================================

    public List<ResponseGetCustomerIdsAndNames> getCustomerIdsAndNames() {
        log.debug("Buscando IDs e nomes de todos os clientes");

        return this.customerRepository.findAll()
                .stream()
                .map(customer ->
                        new ResponseGetCustomerIdsAndNames(
                                customer.getCustomerId(),
                                customer.getName()
                        )
                )
                .collect(Collectors.toList());
    }

    // ================================================================================================

    // ============================================ GET BY ID =========================================

    public ResponseCustomerById getCustomerById(String customerId) {
        log.debug("Buscando cliente por ID: {}", customerId);

        var entity = this.findCustomerById(customerId);

        String updateAt = entity.getUpdatedAt() != null
                ? entity.getUpdatedAt().format(DATE_TIME_FORMATTER)
                : null;

        String ticketCode = entity.getTickets()
                .stream()
                .findFirst()
                .map(Ticket::getCode)
                .filter(code -> code != null && !code.isBlank())
                .orElse(null);

        log.debug("Cliente encontrado: {}, com ticket: {}", entity.getName(), ticketCode != null ? ticketCode : "nenhum");

        return new ResponseCustomerById(
                entity.getCustomerId(),
                entity.getName(),
                entity.getCpf(),
                entity.getRg(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getCreatedAt().format(DATE_TIME_FORMATTER),
                updateAt,
                ticketCode
        );
    }

    // ================================================================================================

    // =========================================== DELETE ===========================================

    @Transactional
    public ResponseCustomerDto deleteCustomer(String customerId) {
        log.info("Deletando cliente: {}", customerId);

        var entity = this.findCustomerById(customerId);
        var response = this.toResponse(entity);

        this.customerRepository.delete(entity);

        log.info("Cliente deletado com sucesso: {}, ID: {}", entity.getName(), customerId);
        return response;
    }

    // ================================================================================================

    // =========================================== STATISTICS ========================================

    public ResponseCustomerDashBoardDto getStatistics(JwtAuthenticationToken token) {
        var unit = this.unitContext.getCurrentUnit(token);
        log.debug("Buscando estatísticas de clientes para unidade: {}", unit.getUnitId());

        var countTotalCustomersStatistics = this.customerRepository.countTotalCustomerStatisticsDto(unit.getUnitId());
        var customersCreatedByMonthStatistics = this.customerRepository.countCustomersCreatedByMonth(unit.getUnitId());

        log.debug("Estatísticas coletadas: total={}, por mês={}",
                countTotalCustomersStatistics,
                customersCreatedByMonthStatistics != null ? customersCreatedByMonthStatistics.size() : 0);

        return new ResponseCustomerDashBoardDto(
                countTotalCustomersStatistics,
                customersCreatedByMonthStatistics
        );
    }

    // ================================================================================================

    // ======================================== AUXILIARES ============================================

    private Customer findCustomerById(String customerId) {
        return this.customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> {
                    log.warn("Cliente não encontrado com ID: {}", customerId);
                    return new CustomerNotFoundException("Cliente não encontrado com ID: " + customerId);
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

    private ResponseCustomerDto toResponse(Customer entity) {
        var updateAt = entity.getUpdatedAt() != null
                ? entity.getUpdatedAt().format(DATE_TIME_FORMATTER)
                : null;

        return new ResponseCustomerDto(
                entity.getCustomerId(),
                entity.getName(),
                entity.getCpf(),
                entity.getRg(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getCreatedAt().format(DATE_TIME_FORMATTER),
                updateAt
        );
    }
}