package br.com.queue.service.serviceManagement;

import br.com.queue.dtos.serviceManagement.ResponseServiceManagementDto;
import br.com.queue.dtos.serviceManagement.create.CreateServiceManagementDto;
import br.com.queue.dtos.serviceManagement.getServiceDto.ResponseGetServiceByIdDto;
import br.com.queue.dtos.serviceManagement.list_service.ResponseServicesForCreatedUser;
import br.com.queue.dtos.serviceManagement.statistics.*;
import br.com.queue.dtos.serviceManagement.update.UpdateServiceManagementDto;
import br.com.queue.entities.department.Department;
import br.com.queue.entities.serviceManagement.ServiceManagement;
import br.com.queue.entities.unit.Unit;
import br.com.queue.infra.department.DepartmentNotFoundException;
import br.com.queue.infra.serviceManagement.ServiceManagementAlreadyExistsException;
import br.com.queue.infra.serviceManagement.ServiceManagementNotFoundException;
import br.com.queue.repositories.department.DepartmentRepository;
import br.com.queue.repositories.serviceManagement.ServiceManagementRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceManagementServiceTest {

    @Mock
    private ServiceManagementRepository serviceRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private UnitContext unitContext;

    @Mock
    private JwtAuthenticationToken token;

    @InjectMocks
    private ServiceManagementService serviceManagementService;

    private Unit unit;
    private Department department;
    private ServiceManagement serviceManagement;
    private CreateServiceManagementDto createDto;
    private UpdateServiceManagementDto updateDto;

    @BeforeEach
    void setUp() {
        unit = new Unit();
        unit.setUnitId("unit-123");
        unit.setName("Unidade Teste");
        unit.setActive(true);

        department = new Department();
        department.setDepartmentId("dept-123");
        department.setName("TI");

        serviceManagement = new ServiceManagement();
        serviceManagement.setServiceManagementId("service-123");
        serviceManagement.setName("Suporte Técnico");
        serviceManagement.setCode("ST");
        serviceManagement.setDescription("Suporte técnico especializado");
        serviceManagement.setDepartment(department);
        serviceManagement.setUnit(unit);
        serviceManagement.setActive(true);
        serviceManagement.setLastTicketNumber(0L);
        serviceManagement.setCreatedAt(LocalDateTime.of(2026, 8, 28, 10, 0));

        createDto = new CreateServiceManagementDto(
                "Suporte Técnico",
                "ST",
                "Suporte técnico especializado",
                "TI"
        );

        updateDto = new UpdateServiceManagementDto(
                "service-123",
                "Suporte Técnico Atualizado",
                "STA",
                "Suporte técnico especializado atualizado",
                true,
                "TI"
        );
    }

    // =========================================================
    // CREATE SERVICE MANAGEMENT
    // =========================================================

    @Nested
    @DisplayName("Testes de criação de serviço")
    class CreateServiceManagementTests {

        @Test
        @DisplayName("Deve criar serviço com sucesso")
        void shouldCreateServiceSuccessfully() {
            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(serviceRepository.findByName("Suporte Técnico")).thenReturn(Optional.empty());
            when(departmentRepository.findByName("TI")).thenReturn(Optional.of(department));
            when(serviceRepository.save(any(ServiceManagement.class))).thenAnswer(invocation -> {
                ServiceManagement saved = invocation.getArgument(0);
                saved.setServiceManagementId("service-123");
                return saved;
            });

            var response = serviceManagementService.createServiceManagement(token, createDto);

            assertNotNull(response);
            assertEquals("Suporte Técnico", response.name());
            assertEquals("ST", response.code());
            assertEquals("Suporte técnico especializado", response.description());
            assertEquals("TI", response.departmentName());
            assertTrue(response.active());

            verify(unitContext).getCurrentUnit(token);
            verify(serviceRepository).findByName("Suporte Técnico");
            verify(departmentRepository).findByName("TI");
            verify(serviceRepository).save(any(ServiceManagement.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando serviço já existe")
        void shouldThrowWhenServiceAlreadyExists() {
            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(serviceRepository.findByName("Suporte Técnico")).thenReturn(Optional.of(serviceManagement));

            var exception = assertThrows(
                    ServiceManagementAlreadyExistsException.class,
                    () -> serviceManagementService.createServiceManagement(token, createDto)
            );

            assertEquals("Já existe um serviço com o nome: Suporte Técnico", exception.getMessage());

            verify(serviceRepository).findByName("Suporte Técnico");
            verify(departmentRepository, never()).findByName(anyString());
            verify(serviceRepository, never()).save(any(ServiceManagement.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando departamento não existe")
        void shouldThrowWhenDepartmentDoesNotExist() {
            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(serviceRepository.findByName("Suporte Técnico")).thenReturn(Optional.empty());
            when(departmentRepository.findByName("TI")).thenReturn(Optional.empty());

            var exception = assertThrows(
                    DepartmentNotFoundException.class,
                    () -> serviceManagementService.createServiceManagement(token, createDto)
            );

            assertEquals("Departamento não encontrado com nome: TI", exception.getMessage());

            verify(departmentRepository).findByName("TI");
            verify(serviceRepository, never()).save(any(ServiceManagement.class));
        }
    }

    // =========================================================
    // UPDATE SERVICE MANAGEMENT
    // =========================================================

    @Nested
    @DisplayName("Testes de atualização de serviço")
    class UpdateServiceManagementTests {

        @Test
        @DisplayName("Deve atualizar serviço com sucesso")
        void shouldUpdateServiceSuccessfully() {
            when(serviceRepository.findByServiceManagementId("service-123")).thenReturn(Optional.of(serviceManagement));
            when(serviceRepository.findByName("Suporte Técnico Atualizado")).thenReturn(Optional.empty());
            when(departmentRepository.findByName("TI")).thenReturn(Optional.of(department));
            when(serviceRepository.save(any(ServiceManagement.class))).thenAnswer(invocation -> invocation.getArgument(0));

            var response = serviceManagementService.updateServiceManagement(updateDto);

            assertNotNull(response);
            assertEquals("service-123", response.serviceManagementId());
            assertEquals("Suporte Técnico Atualizado", response.name());
            assertEquals("STA", response.code());
            assertEquals("Suporte técnico especializado atualizado", response.description());
            assertEquals("TI", response.departmentName());
            assertTrue(response.active());

            verify(serviceRepository).findByServiceManagementId("service-123");
            verify(serviceRepository).findByName("Suporte Técnico Atualizado");
            verify(departmentRepository).findByName("TI");
            verify(serviceRepository).save(any(ServiceManagement.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando serviço não existe")
        void shouldThrowWhenServiceDoesNotExist() {
            when(serviceRepository.findByServiceManagementId("service-123")).thenReturn(Optional.empty());

            var exception = assertThrows(
                    ServiceManagementNotFoundException.class,
                    () -> serviceManagementService.updateServiceManagement(updateDto)
            );

            assertEquals("Serviço não encontrado com ID: service-123", exception.getMessage());

            verify(serviceRepository).findByServiceManagementId("service-123");
            verify(serviceRepository, never()).save(any(ServiceManagement.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando nome já existe em outro serviço")
        void shouldThrowWhenNameAlreadyExists() {
            var existingService = new ServiceManagement();
            existingService.setServiceManagementId("service-456");
            existingService.setName("Suporte Técnico Atualizado");

            when(serviceRepository.findByServiceManagementId("service-123")).thenReturn(Optional.of(serviceManagement));
            when(serviceRepository.findByName("Suporte Técnico Atualizado")).thenReturn(Optional.of(existingService));

            var exception = assertThrows(
                    ServiceManagementAlreadyExistsException.class,
                    () -> serviceManagementService.updateServiceManagement(updateDto)
            );

            assertEquals("Já existe um serviço com o nome: Suporte Técnico Atualizado", exception.getMessage());

            verify(serviceRepository).findByServiceManagementId("service-123");
            verify(serviceRepository).findByName("Suporte Técnico Atualizado");
            verify(serviceRepository, never()).save(any(ServiceManagement.class));
        }

        @Test
        @DisplayName("Deve permitir atualização mantendo o mesmo nome")
        void shouldAllowUpdateWithSameName() {
            when(serviceRepository.findByServiceManagementId("service-123")).thenReturn(Optional.of(serviceManagement));
            when(departmentRepository.findByName("TI")).thenReturn(Optional.of(department));
            when(serviceRepository.save(any(ServiceManagement.class))).thenAnswer(invocation -> invocation.getArgument(0));

            var dto = new UpdateServiceManagementDto(
                    "service-123",
                    "Suporte Técnico",
                    "STA",
                    "Descrição atualizada",
                    true,
                    "TI"
            );

            var response = serviceManagementService.updateServiceManagement(dto);

            assertNotNull(response);
            assertEquals("Suporte Técnico", response.name());

            verify(serviceRepository).findByServiceManagementId("service-123");
            verify(serviceRepository, never()).findByName("Suporte Técnico");
            verify(serviceRepository).save(any(ServiceManagement.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando departamento não existe no update")
        void shouldThrowWhenDepartmentDoesNotExistInUpdate() {
            when(serviceRepository.findByServiceManagementId("service-123")).thenReturn(Optional.of(serviceManagement));
            when(serviceRepository.findByName("Suporte Técnico Atualizado")).thenReturn(Optional.empty());
            when(departmentRepository.findByName("TI")).thenReturn(Optional.empty());

            var exception = assertThrows(
                    DepartmentNotFoundException.class,
                    () -> serviceManagementService.updateServiceManagement(updateDto)
            );

            assertEquals("Departamento não encontrado com nome: TI", exception.getMessage());

            verify(departmentRepository).findByName("TI");
            verify(serviceRepository, never()).save(any(ServiceManagement.class));
        }
    }

    // =========================================================
    // DELETE SERVICE MANAGEMENT
    // =========================================================

    @Nested
    @DisplayName("Testes de deleção de serviço")
    class DeleteServiceManagementTests {

        @Test
        @DisplayName("Deve deletar serviço com sucesso")
        void shouldDeleteServiceSuccessfully() {
            when(serviceRepository.findByServiceManagementId("service-123")).thenReturn(Optional.of(serviceManagement));
            doNothing().when(serviceRepository).deleteUserServicesByServiceId("service-123");
            doNothing().when(serviceRepository).delete(serviceManagement);

            var response = serviceManagementService.deleteServiceManagement("service-123");

            assertNotNull(response);
            assertEquals("service-123", response.serviceManagementId());
            assertEquals("Suporte Técnico", response.name());

            verify(serviceRepository).findByServiceManagementId("service-123");
            verify(serviceRepository).deleteUserServicesByServiceId("service-123");
            verify(serviceRepository).delete(serviceManagement);
        }

        @Test
        @DisplayName("Deve lançar exceção quando serviço não existe")
        void shouldThrowWhenServiceDoesNotExist() {
            when(serviceRepository.findByServiceManagementId("service-123")).thenReturn(Optional.empty());

            var exception = assertThrows(
                    ServiceManagementNotFoundException.class,
                    () -> serviceManagementService.deleteServiceManagement("service-123")
            );

            assertEquals("Serviço não encontrado com ID: service-123", exception.getMessage());

            verify(serviceRepository).findByServiceManagementId("service-123");
            verify(serviceRepository, never()).delete(any(ServiceManagement.class));
        }
    }

    // =========================================================
    // GET ALL SERVICES
    // =========================================================

    @Nested
    @DisplayName("Testes de busca de todos os serviços")
    class GetAllServicesTests {

        @Test
        @DisplayName("Deve buscar todos os serviços com paginação")
        void shouldGetAllServicesWithPagination() {

            var pageable = PageRequest.of(0, 10);

            // Cria um ResponseServiceManagementDto
            var responseDto = new ResponseServiceManagementDto(
                    "service-123",
                    "Suporte Técnico",
                    "ST",
                    "Suporte técnico especializado",
                    "dept-123",
                    "TI",
                    true
            );

            // Cria a página com o DTO correto
            Page<ResponseServiceManagementDto> page = new PageImpl<>(List.of(responseDto));

            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(serviceRepository.findAllWithSearch("unit-123", null, pageable)).thenReturn(page);

            var result = serviceManagementService.getAllServicesManagement(token, 0, 10, null);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals("service-123", result.getContent().get(0).serviceManagementId());
            assertEquals("Suporte Técnico", result.getContent().get(0).name());

            verify(unitContext).getCurrentUnit(token);
            verify(serviceRepository).findAllWithSearch("unit-123", null, pageable);
        }

        @Test
        @DisplayName("Deve buscar serviços com filtro de busca")
        void shouldGetServicesWithSearchFilter() {
            var pageable = PageRequest.of(0, 10);

            var responseDto = new ResponseServiceManagementDto(
                    "service-123",
                    "Suporte Técnico",
                    "ST",
                    "Suporte técnico especializado",
                    "dept-123",
                    "TI",
                    true
            );

            Page<ResponseServiceManagementDto> page = new PageImpl<>(List.of(responseDto));

            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(serviceRepository.findAllWithSearch("unit-123", "Suporte", pageable)).thenReturn(page);

            var result = serviceManagementService.getAllServicesManagement(token, 0, 10, "Suporte");

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());

            verify(unitContext).getCurrentUnit(token);
            verify(serviceRepository).findAllWithSearch("unit-123", "Suporte", pageable);
        }

        @Test
        @DisplayName("Deve retornar página vazia quando não há serviços")
        void shouldReturnEmptyPage() {
            var pageable = PageRequest.of(0, 10);

            // Página vazia com o tipo correto
            Page<ResponseServiceManagementDto> page = new PageImpl<>(List.of());

            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(serviceRepository.findAllWithSearch("unit-123", null, pageable)).thenReturn(page);

            var result = serviceManagementService.getAllServicesManagement(token, 0, 10, null);

            assertNotNull(result);
            assertEquals(0, result.getTotalElements());
            assertTrue(result.getContent().isEmpty());

            verify(unitContext).getCurrentUnit(token);
            verify(serviceRepository).findAllWithSearch("unit-123", null, pageable);
        }
    }

    // =========================================================
    // GET SERVICE BY ID
    // =========================================================

    @Nested
    @DisplayName("Testes de busca de serviço por ID")
    class GetServiceByIdTests {

        @Test
        @DisplayName("Deve buscar serviço por ID com sucesso")
        void shouldGetServiceByIdSuccessfully() {
            when(serviceRepository.findByServiceManagementId("service-123")).thenReturn(Optional.of(serviceManagement));

            var response = serviceManagementService.getServiceManagementById("service-123");

            assertNotNull(response);
            assertEquals("service-123", response.serviceManagementId());
            assertEquals("Suporte Técnico", response.name());
            assertEquals("ST", response.code());
            assertEquals("Suporte técnico especializado", response.description());
            assertEquals("TI", response.departmentName());
            assertTrue(response.active());

            verify(serviceRepository).findByServiceManagementId("service-123");
        }

        @Test
        @DisplayName("Deve lançar exceção quando serviço não existe")
        void shouldThrowWhenServiceDoesNotExist() {
            when(serviceRepository.findByServiceManagementId("service-123")).thenReturn(Optional.empty());

            var exception = assertThrows(
                    ServiceManagementNotFoundException.class,
                    () -> serviceManagementService.getServiceManagementById("service-123")
            );

            assertEquals("Serviço não encontrado com ID: service-123", exception.getMessage());

            verify(serviceRepository).findByServiceManagementId("service-123");
        }
    }

    // =========================================================
    // SERVICES FOR CREATED USER
    // =========================================================

    @Nested
    @DisplayName("Testes de serviços para criação de usuário")
    class ServicesForCreatedUserTests {

        @Test
        @DisplayName("Deve retornar apenas serviços ativos")
        void shouldReturnOnlyActiveServices() {
            var inactiveService = new ServiceManagement();
            inactiveService.setServiceManagementId("service-456");
            inactiveService.setName("Inativo");
            inactiveService.setActive(false);
            inactiveService.setDepartment(department);

            when(serviceRepository.findAll()).thenReturn(List.of(serviceManagement, inactiveService));

            var result = serviceManagementService.servicesForCreatedUser();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("service-123", result.get(0).serviceManagementId());
            assertEquals("Suporte Técnico", result.get(0).name());
            assertEquals("TI", result.get(0).departmentName());

            verify(serviceRepository).findAll();
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há serviços ativos")
        void shouldReturnEmptyListWhenNoActiveServices() {
            var inactiveService = new ServiceManagement();
            inactiveService.setServiceManagementId("service-456");
            inactiveService.setName("Inativo");
            inactiveService.setActive(false);
            inactiveService.setDepartment(department);

            when(serviceRepository.findAll()).thenReturn(List.of(inactiveService));

            var result = serviceManagementService.servicesForCreatedUser();

            assertNotNull(result);
            assertTrue(result.isEmpty());

            verify(serviceRepository).findAll();
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
            // Criando mocks com os DTOs corretos
            var totalDto = mock(ResponseCountTotalServicesStatisticsDto.class);
            var percentageDto = mock(ResponseServicePercentagesStatisticsDto.class);
            var createdByMonth = List.of(mock(ResponseServicesCreatedByMonthStatisticsDto.class));
            var byDepartment = List.of(mock(ResponseServicesByDepartmentStatisticsDto.class));
            var byUser = List.of(mock(ResponseUsersByServiceStatisticsDto.class));
            var bySchedule = List.of(mock(ResponseSchedulesByServiceStatisticsDto.class));
            var byTicket = List.of(mock(ResponseTicketsByServiceStatisticsDto.class));

            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(serviceRepository.countTotalServicesStatisticsDto("unit-123")).thenReturn(totalDto);
            when(serviceRepository.getServicePercentagesStatisticsDto("unit-123")).thenReturn(percentageDto);
            when(serviceRepository.countServicesCreatedByMonth("unit-123")).thenReturn(createdByMonth);
            when(serviceRepository.countServicesByDepartmentStatistics("unit-123")).thenReturn(byDepartment);
            when(serviceRepository.countUsersByServiceStatistics("unit-123")).thenReturn(byUser);
            when(serviceRepository.countSchedulesByServiceStatistics("unit-123")).thenReturn(bySchedule);
            when(serviceRepository.countTicketsByServiceStatistics("unit-123")).thenReturn(byTicket);

            var response = serviceManagementService.getStatistics(token);

            assertNotNull(response);
            assertEquals(totalDto, response.countTotalServicesStatistics());
            assertEquals(percentageDto, response.servicePercentagesStatistics());
            assertEquals(createdByMonth, response.servicesCreatedByMonth());
            assertEquals(byDepartment, response.servicesByDepartment());
            assertEquals(byUser, response.usersByService());
            assertEquals(bySchedule, response.schedulesByService());
            assertEquals(byTicket, response.ticketsByService());

            verify(unitContext).getCurrentUnit(token);
            verify(serviceRepository).countTotalServicesStatisticsDto("unit-123");
            verify(serviceRepository).getServicePercentagesStatisticsDto("unit-123");
            verify(serviceRepository).countServicesCreatedByMonth("unit-123");
            verify(serviceRepository).countServicesByDepartmentStatistics("unit-123");
            verify(serviceRepository).countUsersByServiceStatistics("unit-123");
            verify(serviceRepository).countSchedulesByServiceStatistics("unit-123");
            verify(serviceRepository).countTicketsByServiceStatistics("unit-123");
        }

        @Test
        @DisplayName("Deve retornar estatísticas mesmo com dados vazios")
        void shouldReturnStatisticsEvenWithEmptyData() {
            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(serviceRepository.countTotalServicesStatisticsDto("unit-123")).thenReturn(null);
            when(serviceRepository.getServicePercentagesStatisticsDto("unit-123")).thenReturn(null);
            when(serviceRepository.countServicesCreatedByMonth("unit-123")).thenReturn(List.of());
            when(serviceRepository.countServicesByDepartmentStatistics("unit-123")).thenReturn(List.of());
            when(serviceRepository.countUsersByServiceStatistics("unit-123")).thenReturn(List.of());
            when(serviceRepository.countSchedulesByServiceStatistics("unit-123")).thenReturn(List.of());
            when(serviceRepository.countTicketsByServiceStatistics("unit-123")).thenReturn(List.of());

            var response = serviceManagementService.getStatistics(token);

            assertNotNull(response);
            assertNull(response.countTotalServicesStatistics());
            assertNull(response.servicePercentagesStatistics());
            assertTrue(response.servicesCreatedByMonth().isEmpty());
            assertTrue(response.servicesByDepartment().isEmpty());
            assertTrue(response.usersByService().isEmpty());
            assertTrue(response.schedulesByService().isEmpty());
            assertTrue(response.ticketsByService().isEmpty());

            verify(unitContext).getCurrentUnit(token);
            verify(serviceRepository).countTotalServicesStatisticsDto("unit-123");
            verify(serviceRepository).getServicePercentagesStatisticsDto("unit-123");
            verify(serviceRepository).countServicesCreatedByMonth("unit-123");
            verify(serviceRepository).countServicesByDepartmentStatistics("unit-123");
            verify(serviceRepository).countUsersByServiceStatistics("unit-123");
            verify(serviceRepository).countSchedulesByServiceStatistics("unit-123");
            verify(serviceRepository).countTicketsByServiceStatistics("unit-123");
        }
    }
}