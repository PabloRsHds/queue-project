package br.com.queue.service.customer;

import br.com.queue.dtos.customer.allCustomer.ResponseAllCustomersDto;
import br.com.queue.dtos.customer.create.CreateCustomerDto;
import br.com.queue.dtos.customer.statistics.ResponseCountTotalCustomersStatisticsDto;
import br.com.queue.dtos.customer.statistics.ResponseCustomersCreatedByMonthStatisticsDto;
import br.com.queue.dtos.customer.update.UpdateCustomerDto;
import br.com.queue.entities.customer.Customer;
import br.com.queue.entities.ticket.Ticket;
import br.com.queue.entities.unit.Unit;
import br.com.queue.infra.customer.CustomerNotFoundException;
import br.com.queue.infra.customer.CustomerValidationException;
import br.com.queue.repositories.customer.CustomerRepository;
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
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private UnitContext unitContext;

    @Mock
    private JwtAuthenticationToken token;

    @InjectMocks
    private CustomerService customerService;

    private Unit unit;
    private Customer customer;
    private Ticket ticket;
    private CreateCustomerDto createDto;
    private UpdateCustomerDto updateDto;

    @BeforeEach
    void setUp() {
        unit = new Unit();
        unit.setUnitId("unit-123");
        unit.setName("Unidade Teste");
        unit.setActive(true);

        ticket = new Ticket();
        ticket.setTicketId("ticket-123");
        ticket.setCode("ST-001");

        customer = new Customer();
        customer.setCustomerId("customer-123");
        customer.setName("João Silva");
        customer.setCpf("123.456.789-00");
        customer.setRg("12.345.678-9");
        customer.setPhone("(11) 99999-9999");
        customer.setEmail("joao@email.com");
        customer.setUnit(unit);
        customer.setCreatedAt(LocalDateTime.of(2026, 8, 29, 10, 0));
        customer.setTickets(new ArrayList<>(List.of(ticket)));

        createDto = new CreateCustomerDto(
                "Maria Santos",
                "987.654.321-00",
                "98.765.432-1",
                "(11) 88888-8888",
                "maria@email.com"
        );

        updateDto = new UpdateCustomerDto(
                "customer-123",
                "João Silva Atualizado",
                "123.456.789-00",
                "12.345.678-9",
                "(11) 77777-7777",
                "joao.atualizado@email.com"
        );
    }

    // =========================================================
    // CREATE CUSTOMER
    // =========================================================

    @Nested
    @DisplayName("Testes de criação de cliente")
    class CreateCustomerTests {

        @Test
        @DisplayName("Deve criar cliente com sucesso")
        void shouldCreateCustomerSuccessfully() {
            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(customerRepository.existsByCpf("987.654.321-00")).thenReturn(false);
            when(customerRepository.existsByRg("98.765.432-1")).thenReturn(false);
            when(customerRepository.existsByPhone("(11) 88888-8888")).thenReturn(false);
            when(customerRepository.existsByEmail("maria@email.com")).thenReturn(false);
            when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
                Customer saved = invocation.getArgument(0);
                saved.setCustomerId("customer-456");
                return saved;
            });

            var response = customerService.registerCustomer(token, createDto);

            assertNotNull(response);
            assertEquals("customer-456", response.customerId());
            assertEquals("Maria Santos", response.name());
            assertEquals("987.654.321-00", response.cpf());
            assertEquals("98.765.432-1", response.rg());
            assertEquals("(11) 88888-8888", response.phone());
            assertEquals("maria@email.com", response.email());
            assertNotNull(response.createdAt());
            assertNull(response.updatedAt());

            verify(unitContext).getCurrentUnit(token);
            verify(customerRepository).existsByCpf("987.654.321-00");
            verify(customerRepository).existsByRg("98.765.432-1");
            verify(customerRepository).existsByPhone("(11) 88888-8888");
            verify(customerRepository).existsByEmail("maria@email.com");
            verify(customerRepository).save(any(Customer.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando CPF já existe")
        void shouldThrowWhenCpfAlreadyExists() {
            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(customerRepository.existsByCpf("987.654.321-00")).thenReturn(true);

            var exception = assertThrows(
                    CustomerValidationException.class,
                    () -> customerService.registerCustomer(token, createDto)
            );

            assertEquals("Já existe um cliente com este CPF.", exception.getMessage());

            verify(customerRepository).existsByCpf("987.654.321-00");
            verify(customerRepository, never()).save(any(Customer.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando RG já existe")
        void shouldThrowWhenRgAlreadyExists() {
            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(customerRepository.existsByCpf("987.654.321-00")).thenReturn(false);
            when(customerRepository.existsByRg("98.765.432-1")).thenReturn(true);

            var exception = assertThrows(
                    CustomerValidationException.class,
                    () -> customerService.registerCustomer(token, createDto)
            );

            assertEquals("Já existe um cliente com este RG.", exception.getMessage());

            verify(customerRepository).existsByRg("98.765.432-1");
            verify(customerRepository, never()).save(any(Customer.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando telefone já existe")
        void shouldThrowWhenPhoneAlreadyExists() {
            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(customerRepository.existsByCpf("987.654.321-00")).thenReturn(false);
            when(customerRepository.existsByRg("98.765.432-1")).thenReturn(false);
            when(customerRepository.existsByPhone("(11) 88888-8888")).thenReturn(true);

            var exception = assertThrows(
                    CustomerValidationException.class,
                    () -> customerService.registerCustomer(token, createDto)
            );

            assertEquals("Já existe um cliente com este telefone.", exception.getMessage());

            verify(customerRepository).existsByPhone("(11) 88888-8888");
            verify(customerRepository, never()).save(any(Customer.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando e-mail já existe")
        void shouldThrowWhenEmailAlreadyExists() {
            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(customerRepository.existsByCpf("987.654.321-00")).thenReturn(false);
            when(customerRepository.existsByRg("98.765.432-1")).thenReturn(false);
            when(customerRepository.existsByPhone("(11) 88888-8888")).thenReturn(false);
            when(customerRepository.existsByEmail("maria@email.com")).thenReturn(true);

            var exception = assertThrows(
                    CustomerValidationException.class,
                    () -> customerService.registerCustomer(token, createDto)
            );

            assertEquals("Já existe um cliente com este e-mail.", exception.getMessage());

            verify(customerRepository).existsByEmail("maria@email.com");
            verify(customerRepository, never()).save(any(Customer.class));
        }
    }

    // =========================================================
    // UPDATE CUSTOMER
    // =========================================================

    @Nested
    @DisplayName("Testes de atualização de cliente")
    class UpdateCustomerTests {

        @Test
        @DisplayName("Deve atualizar cliente com sucesso")
        void shouldUpdateCustomerSuccessfully() {
            when(customerRepository.findByCustomerId("customer-123")).thenReturn(Optional.of(customer));
            when(customerRepository.existsByPhone("(11) 77777-7777")).thenReturn(false);
            when(customerRepository.existsByEmail("joao.atualizado@email.com")).thenReturn(false);
            when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

            var response = customerService.updateCustomer(updateDto);

            assertNotNull(response);
            assertEquals("customer-123", response.customerId());
            assertEquals("João Silva Atualizado", response.name());
            assertEquals("(11) 77777-7777", response.phone());
            assertEquals("joao.atualizado@email.com", response.email());
            assertNotNull(response.updatedAt());

            verify(customerRepository).findByCustomerId("customer-123");
            verify(customerRepository, never()).existsByCpf(anyString()); // Não deve validar CPF
            verify(customerRepository, never()).existsByRg(anyString()); // Não deve validar RG
            verify(customerRepository).existsByPhone("(11) 77777-7777");
            verify(customerRepository).existsByEmail("joao.atualizado@email.com");
            verify(customerRepository).save(any(Customer.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não existe")
        void shouldThrowWhenCustomerDoesNotExist() {
            when(customerRepository.findByCustomerId("customer-123")).thenReturn(Optional.empty());

            var exception = assertThrows(
                    CustomerNotFoundException.class,
                    () -> customerService.updateCustomer(updateDto)
            );

            assertEquals("Cliente não encontrado com ID: customer-123", exception.getMessage());

            verify(customerRepository).findByCustomerId("customer-123");
            verify(customerRepository, never()).save(any(Customer.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando CPF já existe em outro cliente")
        void shouldThrowWhenCpfAlreadyExistsInUpdate() {

            var dtoWithDifferentCpf = new UpdateCustomerDto(
                    "customer-123",
                    "João Silva",
                    "999.999.999-99",
                    null,
                    null,
                    null
            );

            when(customerRepository.findByCustomerId("customer-123")).thenReturn(Optional.of(customer));
            when(customerRepository.existsByCpf("999.999.999-99")).thenReturn(true);

            var exception = assertThrows(
                    CustomerValidationException.class,
                    () -> customerService.updateCustomer(dtoWithDifferentCpf)
            );

            assertEquals("Já existe um cliente com este CPF.", exception.getMessage());

            verify(customerRepository).findByCustomerId("customer-123");
            verify(customerRepository).existsByCpf("999.999.999-99");
            verify(customerRepository, never()).save(any(Customer.class));
        }

        @Test
        @DisplayName("Deve permitir atualizar mantendo o mesmo CPF")
        void shouldAllowUpdateWithSameCpf() {
            var dto = new UpdateCustomerDto(
                    "customer-123",
                    "João Silva",
                    "123.456.789-00", // Mesmo CPF
                    null,
                    null,
                    null
            );

            when(customerRepository.findByCustomerId("customer-123")).thenReturn(Optional.of(customer));
            when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

            var response = customerService.updateCustomer(dto);

            assertNotNull(response);
            assertEquals("João Silva", response.name());
            assertEquals("123.456.789-00", response.cpf());

            verify(customerRepository).findByCustomerId("customer-123");
            verify(customerRepository, never()).existsByCpf(anyString());
            verify(customerRepository).save(any(Customer.class));
        }

        @Test
        @DisplayName("Não deve atualizar quando nenhum campo é alterado")
        void shouldNotUpdateWhenNoChanges() {
            var dto = new UpdateCustomerDto(
                    "customer-123",
                    null,
                    null,
                    null,
                    null,
                    null
            );

            when(customerRepository.findByCustomerId("customer-123")).thenReturn(Optional.of(customer));
            when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

            var response = customerService.updateCustomer(dto);

            assertNotNull(response);
            assertNull(response.updatedAt());

            verify(customerRepository).findByCustomerId("customer-123");
            verify(customerRepository).save(any(Customer.class));
        }
    }

    // =========================================================
    // GET ALL CUSTOMERS
    // =========================================================

    @Nested
    @DisplayName("Testes de busca de todos os clientes")
    class GetAllCustomersTests {

        @Test
        @DisplayName("Deve buscar todos os clientes com paginação")
        void shouldGetAllCustomersWithPagination() {
            var pageable = PageRequest.of(0, 10);

            var responseDto = new ResponseAllCustomersDto(
                    "customer-123",
                    "João Silva",
                    "123.456.789-00",
                    "12.345.678-9",
                    "(11) 99999-9999",
                    "joao@email.com"
            );

            Page<ResponseAllCustomersDto> page = new PageImpl<>(List.of(responseDto));

            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(customerRepository.findAllWithSearch("unit-123", null, pageable)).thenReturn(page);

            var result = customerService.getAllCustomers(token, 0, 10, null);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals("customer-123", result.getContent().get(0).customerId());
            assertEquals("João Silva", result.getContent().get(0).name());
            assertEquals("123.456.789-00", result.getContent().get(0).cpf());

            verify(unitContext).getCurrentUnit(token);
            verify(customerRepository).findAllWithSearch("unit-123", null, pageable);
        }

        @Test
        @DisplayName("Deve buscar clientes com filtro de busca")
        void shouldGetCustomersWithSearchFilter() {
            var pageable = PageRequest.of(0, 10);

            var responseDto = new ResponseAllCustomersDto(
                    "customer-123",
                    "João Silva",
                    "123.456.789-00",
                    "12.345.678-9",
                    "(11) 99999-9999",
                    "joao@email.com"
            );

            Page<ResponseAllCustomersDto> page = new PageImpl<>(List.of(responseDto));

            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(customerRepository.findAllWithSearch("unit-123", "João", pageable)).thenReturn(page);

            var result = customerService.getAllCustomers(token, 0, 10, "João");

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());

            verify(unitContext).getCurrentUnit(token);
            verify(customerRepository).findAllWithSearch("unit-123", "João", pageable);
        }

        @Test
        @DisplayName("Deve retornar página vazia quando não há clientes")
        void shouldReturnEmptyPage() {
            var pageable = PageRequest.of(0, 10);
            Page<ResponseAllCustomersDto> page = new PageImpl<>(List.of());

            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(customerRepository.findAllWithSearch("unit-123", null, pageable)).thenReturn(page);

            var result = customerService.getAllCustomers(token, 0, 10, null);

            assertNotNull(result);
            assertEquals(0, result.getTotalElements());
            assertTrue(result.getContent().isEmpty());

            verify(unitContext).getCurrentUnit(token);
            verify(customerRepository).findAllWithSearch("unit-123", null, pageable);
        }
    }

    // =========================================================
    // GET CUSTOMER IDS AND NAMES
    // =========================================================

    @Nested
    @DisplayName("Testes de busca de IDs e nomes dos clientes")
    class GetCustomerIdsAndNamesTests {

        @Test
        @DisplayName("Deve retornar lista de IDs e nomes dos clientes")
        void shouldReturnCustomerIdsAndNames() {
            when(customerRepository.findAll()).thenReturn(List.of(customer));

            var result = customerService.getCustomerIdsAndNames();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("customer-123", result.get(0).customerId());
            assertEquals("João Silva", result.get(0).name());

            verify(customerRepository).findAll();
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há clientes")
        void shouldReturnEmptyList() {
            when(customerRepository.findAll()).thenReturn(List.of());

            var result = customerService.getCustomerIdsAndNames();

            assertNotNull(result);
            assertTrue(result.isEmpty());

            verify(customerRepository).findAll();
        }
    }

    // =========================================================
    // GET CUSTOMER BY ID
    // =========================================================

    @Nested
    @DisplayName("Testes de busca de cliente por ID")
    class GetCustomerByIdTests {

        @Test
        @DisplayName("Deve buscar cliente por ID com sucesso")
        void shouldGetCustomerByIdSuccessfully() {
            when(customerRepository.findByCustomerId("customer-123")).thenReturn(Optional.of(customer));

            var response = customerService.getCustomerById("customer-123");

            assertNotNull(response);
            assertEquals("customer-123", response.customerId());
            assertEquals("João Silva", response.name());
            assertEquals("123.456.789-00", response.cpf());
            assertEquals("12.345.678-9", response.rg());
            assertEquals("(11) 99999-9999", response.phone());
            assertEquals("joao@email.com", response.email());
            assertEquals("ST-001", response.ticketCode());
            assertNotNull(response.createdAt());
            assertNull(response.updatedAt());

            verify(customerRepository).findByCustomerId("customer-123");
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não existe")
        void shouldThrowWhenCustomerDoesNotExist() {
            when(customerRepository.findByCustomerId("customer-123")).thenReturn(Optional.empty());

            var exception = assertThrows(
                    CustomerNotFoundException.class,
                    () -> customerService.getCustomerById("customer-123")
            );

            assertEquals("Cliente não encontrado com ID: customer-123", exception.getMessage());

            verify(customerRepository).findByCustomerId("customer-123");
        }

        @Test
        @DisplayName("Deve retornar ticketCode como null quando cliente não tem tickets")
        void shouldReturnNullTicketCodeWhenCustomerHasNoTickets() {
            customer.setTickets(new ArrayList<>());

            when(customerRepository.findByCustomerId("customer-123")).thenReturn(Optional.of(customer));

            var response = customerService.getCustomerById("customer-123");

            assertNotNull(response);
            assertNull(response.ticketCode());

            verify(customerRepository).findByCustomerId("customer-123");
        }
    }

    // =========================================================
    // DELETE CUSTOMER
    // =========================================================

    @Nested
    @DisplayName("Testes de deleção de cliente")
    class DeleteCustomerTests {

        @Test
        @DisplayName("Deve deletar cliente com sucesso")
        void shouldDeleteCustomerSuccessfully() {
            when(customerRepository.findByCustomerId("customer-123")).thenReturn(Optional.of(customer));
            doNothing().when(customerRepository).delete(customer);

            var response = customerService.deleteCustomer("customer-123");

            assertNotNull(response);
            assertEquals("customer-123", response.customerId());
            assertEquals("João Silva", response.name());

            verify(customerRepository).findByCustomerId("customer-123");
            verify(customerRepository).delete(customer);
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não existe")
        void shouldThrowWhenCustomerDoesNotExist() {
            when(customerRepository.findByCustomerId("customer-123")).thenReturn(Optional.empty());

            var exception = assertThrows(
                    CustomerNotFoundException.class,
                    () -> customerService.deleteCustomer("customer-123")
            );

            assertEquals("Cliente não encontrado com ID: customer-123", exception.getMessage());

            verify(customerRepository).findByCustomerId("customer-123");
            verify(customerRepository, never()).delete(any(Customer.class));
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
            var totalDto = mock(ResponseCountTotalCustomersStatisticsDto.class);
            var createdByMonth = List.of(mock(ResponseCustomersCreatedByMonthStatisticsDto.class));

            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(customerRepository.countTotalCustomerStatisticsDto("unit-123")).thenReturn(totalDto);
            when(customerRepository.countCustomersCreatedByMonth("unit-123")).thenReturn(createdByMonth);

            var response = customerService.getStatistics(token);

            assertNotNull(response);
            assertEquals(totalDto, response.countTotalCustomersStatistics());
            assertEquals(createdByMonth, response.customersCreatedByMonthStatistics());

            verify(unitContext).getCurrentUnit(token);
            verify(customerRepository).countTotalCustomerStatisticsDto("unit-123");
            verify(customerRepository).countCustomersCreatedByMonth("unit-123");
        }

        @Test
        @DisplayName("Deve retornar estatísticas mesmo com dados vazios")
        void shouldReturnStatisticsEvenWithEmptyData() {
            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(customerRepository.countTotalCustomerStatisticsDto("unit-123")).thenReturn(null);
            when(customerRepository.countCustomersCreatedByMonth("unit-123")).thenReturn(List.of());

            var response = customerService.getStatistics(token);

            assertNotNull(response);
            assertNull(response.countTotalCustomersStatistics());
            assertTrue(response.customersCreatedByMonthStatistics().isEmpty());

            verify(unitContext).getCurrentUnit(token);
            verify(customerRepository).countTotalCustomerStatisticsDto("unit-123");
            verify(customerRepository).countCustomersCreatedByMonth("unit-123");
        }
    }

    // =========================================================
    // FIND CUSTOMER BY ID (AUXILIAR)
    // =========================================================

    @Nested
    @DisplayName("Testes do método auxiliar findCustomerById")
    class FindCustomerByIdTests {

        @Test
        @DisplayName("Deve encontrar cliente com sucesso")
        void shouldFindCustomerSuccessfully() {
            when(customerRepository.findByCustomerId("customer-123")).thenReturn(Optional.of(customer));

            var result = customerService.deleteCustomer("customer-123");

            assertNotNull(result);
            assertEquals("customer-123", result.customerId());

            verify(customerRepository).findByCustomerId("customer-123");
        }

        @Test
        @DisplayName("Deve lançar CustomerNotFoundException quando cliente não existe")
        void shouldThrowCustomerNotFoundExceptionWhenCustomerDoesNotExist() {
            when(customerRepository.findByCustomerId("customer-123")).thenReturn(Optional.empty());

            var exception = assertThrows(
                    CustomerNotFoundException.class,
                    () -> customerService.deleteCustomer("customer-123")
            );

            assertEquals("Cliente não encontrado com ID: customer-123", exception.getMessage());

            verify(customerRepository).findByCustomerId("customer-123");
        }
    }

    // =========================================================
    // TO RESPONSE (testado indiretamente)
    // =========================================================

    @Nested
    @DisplayName("Testes de conversão para DTO via métodos públicos")
    class ToResponseTests {

        @Test
        @DisplayName("Deve converter Customer para ResponseCustomerDto via getAll")
        void shouldConvertCustomerToResponseViaGetAll() {
            var pageable = PageRequest.of(0, 10);

            var responseDto = new ResponseAllCustomersDto(
                    "customer-123",
                    "João Silva",
                    "123.456.789-00",
                    "12.345.678-9",
                    "(11) 99999-9999",
                    "joao@email.com"
            );

            Page<ResponseAllCustomersDto> page = new PageImpl<>(List.of(responseDto));

            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(customerRepository.findAllWithSearch("unit-123", null, pageable)).thenReturn(page);

            var result = customerService.getAllCustomers(token, 0, 10, null);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals("customer-123", result.getContent().get(0).customerId());
            assertEquals("João Silva", result.getContent().get(0).name());

            verify(unitContext).getCurrentUnit(token);
            verify(customerRepository).findAllWithSearch("unit-123", null, pageable);
        }

        @Test
        @DisplayName("Deve converter Customer para ResponseCustomerDto via update")
        void shouldConvertCustomerToResponseViaUpdate() {
            when(customerRepository.findByCustomerId("customer-123")).thenReturn(Optional.of(customer));
            when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

            var dto = new UpdateCustomerDto(
                    "customer-123",
                    "João Silva Atualizado",
                    null,
                    null,
                    null,
                    null
            );

            var response = customerService.updateCustomer(dto);

            assertNotNull(response);
            assertEquals("customer-123", response.customerId());
            assertEquals("João Silva Atualizado", response.name());

            verify(customerRepository).findByCustomerId("customer-123");
            verify(customerRepository).save(any(Customer.class));
        }
    }
}