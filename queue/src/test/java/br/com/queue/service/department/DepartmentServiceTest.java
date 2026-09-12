package br.com.queue.service.department;

import br.com.queue.dtos.department.create.CreateDepartmentDto;
import br.com.queue.dtos.department.statistics.*;
import br.com.queue.dtos.department.update.UpdateDepartmentDto;
import br.com.queue.entities.department.Department;
import br.com.queue.entities.serviceManagement.ServiceManagement;
import br.com.queue.entities.unit.Unit;
import br.com.queue.infra.department.DepartmentNotFoundException;
import br.com.queue.repositories.department.DepartmentRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private UnitContext unitContext;

    @Mock
    private JwtAuthenticationToken token;

    @InjectMocks
    private DepartmentService departmentService;

    private Unit unit;
    private Department department;
    private ServiceManagement service;
    private CreateDepartmentDto createDto;
    private UpdateDepartmentDto updateDto;

    @BeforeEach
    void setUp() {
        unit = new Unit();
        unit.setUnitId("unit-123");
        unit.setName("Unidade Teste");
        unit.setActive(true);

        service = new ServiceManagement();
        service.setServiceManagementId("service-123");
        service.setName("Suporte Técnico");

        department = new Department();
        department.setDepartmentId("dept-123");
        department.setName("TI");
        department.setDescription("Departamento de Tecnologia da Informação");
        department.setActive(true);
        department.setUnit(unit);
        department.setCreatedAt(LocalDateTime.of(2026, 8, 29, 10, 0));
        department.setServices(new ArrayList<>(List.of(service)));

        createDto = new CreateDepartmentDto(
                "RH",
                "Departamento de Recursos Humanos"
        );

        updateDto = new UpdateDepartmentDto(
                "dept-123",
                "TI Atualizado",
                "Departamento de TI Atualizado",
                true
        );
    }

    // =========================================================
    // CREATE DEPARTMENT
    // =========================================================

    @Nested
    @DisplayName("Testes de criação de departamento")
    class CreateDepartmentTests {

        @Test
        @DisplayName("Deve criar departamento com sucesso")
        void shouldCreateDepartmentSuccessfully() {
            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> {
                Department saved = invocation.getArgument(0);
                saved.setDepartmentId("dept-456");
                return saved;
            });

            var response = departmentService.createDepartment(token, createDto);

            assertNotNull(response);
            assertEquals("dept-456", response.departmentId());
            assertEquals("RH", response.name());
            assertEquals("Departamento de Recursos Humanos", response.description());
            assertTrue(response.active());
            assertNotNull(response.createdAt());
            assertNull(response.updatedAt());
            assertTrue(response.services().isEmpty());

            verify(unitContext).getCurrentUnit(token);
            verify(departmentRepository).save(any(Department.class));
        }

        @Test
        @DisplayName("Deve criar departamento sem descrição")
        void shouldCreateDepartmentWithoutDescription() {
            var dto = new CreateDepartmentDto("Financeiro", null);

            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> {
                Department saved = invocation.getArgument(0);
                saved.setDepartmentId("dept-456");
                return saved;
            });

            var response = departmentService.createDepartment(token, dto);

            assertNotNull(response);
            assertEquals("Financeiro", response.name());
            assertNull(response.description());
            assertTrue(response.active());

            verify(departmentRepository).save(any(Department.class));
        }
    }

    // =========================================================
    // UPDATE DEPARTMENT
    // =========================================================

    @Nested
    @DisplayName("Testes de atualização de departamento")
    class UpdateDepartmentTests {

        @Test
        @DisplayName("Deve atualizar departamento com sucesso")
        void shouldUpdateDepartmentSuccessfully() {
            when(departmentRepository.findByDepartmentId("dept-123")).thenReturn(Optional.of(department));
            when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> invocation.getArgument(0));

            var response = departmentService.updateDepartment(updateDto);

            assertNotNull(response);
            assertEquals("dept-123", response.departmentId());
            assertEquals("TI Atualizado", response.name());
            assertEquals("Departamento de TI Atualizado", response.description());
            assertTrue(response.active());
            assertNotNull(response.updatedAt());

            verify(departmentRepository).findByDepartmentId("dept-123");
            verify(departmentRepository).save(any(Department.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando departamento não existe")
        void shouldThrowWhenDepartmentDoesNotExist() {
            when(departmentRepository.findByDepartmentId("dept-123")).thenReturn(Optional.empty());

            var exception = assertThrows(
                    DepartmentNotFoundException.class,
                    () -> departmentService.updateDepartment(updateDto)
            );

            assertEquals("Departamento não encontrado com ID: dept-123", exception.getMessage());

            verify(departmentRepository).findByDepartmentId("dept-123");
            verify(departmentRepository, never()).save(any(Department.class));
        }

        @Test
        @DisplayName("Não deve atualizar quando nenhum campo é alterado")
        void shouldNotUpdateWhenNoChanges() {
            var dto = new UpdateDepartmentDto(
                    "dept-123",
                    null,
                    null,
                    null
            );

            when(departmentRepository.findByDepartmentId("dept-123")).thenReturn(Optional.of(department));
            when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> invocation.getArgument(0));

            var response = departmentService.updateDepartment(dto);

            assertNotNull(response);
            assertEquals("TI", response.name());
            assertEquals("Departamento de Tecnologia da Informação", response.description());
            assertNull(response.updatedAt());

            verify(departmentRepository).findByDepartmentId("dept-123");
            verify(departmentRepository).save(any(Department.class));
        }

        @Test
        @DisplayName("Deve atualizar apenas o nome")
        void shouldUpdateOnlyName() {
            var dto = new UpdateDepartmentDto(
                    "dept-123",
                    "TI Novo",
                    null,
                    null
            );

            when(departmentRepository.findByDepartmentId("dept-123")).thenReturn(Optional.of(department));
            when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> invocation.getArgument(0));

            var response = departmentService.updateDepartment(dto);

            assertNotNull(response);
            assertEquals("TI Novo", response.name());
            assertEquals("Departamento de Tecnologia da Informação", response.description());
            assertNotNull(response.updatedAt());

            verify(departmentRepository).save(any(Department.class));
        }

        @Test
        @DisplayName("Deve atualizar apenas a descrição")
        void shouldUpdateOnlyDescription() {
            var dto = new UpdateDepartmentDto(
                    "dept-123",
                    null,
                    "Nova descrição do TI",
                    null
            );

            when(departmentRepository.findByDepartmentId("dept-123")).thenReturn(Optional.of(department));
            when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> invocation.getArgument(0));

            var response = departmentService.updateDepartment(dto);

            assertNotNull(response);
            assertEquals("TI", response.name());
            assertEquals("Nova descrição do TI", response.description());
            assertNotNull(response.updatedAt());

            verify(departmentRepository).save(any(Department.class));
        }

        @Test
        @DisplayName("Deve inativar departamento")
        void shouldDeactivateDepartment() {
            var dto = new UpdateDepartmentDto(
                    "dept-123",
                    null,
                    null,
                    false
            );

            when(departmentRepository.findByDepartmentId("dept-123")).thenReturn(Optional.of(department));
            when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> invocation.getArgument(0));

            var response = departmentService.updateDepartment(dto);

            assertNotNull(response);
            assertFalse(response.active());
            assertNotNull(response.updatedAt());

            verify(departmentRepository).save(any(Department.class));
        }
    }

    // =========================================================
    // GET ALL DEPARTMENTS
    // =========================================================

    @Nested
    @DisplayName("Testes de busca de todos os departamentos")
    class GetAllDepartmentsTests {

        @Test
        @DisplayName("Deve buscar todos os departamentos com paginação")
        void shouldGetAllDepartmentsWithPagination() {
            var pageable = PageRequest.of(0, 10);
            Page<Department> page = new PageImpl<>(List.of(department));

            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(departmentRepository.findAllWithSearch("unit-123", null, pageable)).thenReturn(page);

            var result = departmentService.getAllDepartments(token, 0, 10, null);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals("dept-123", result.getContent().get(0).departmentId());
            assertEquals("TI", result.getContent().get(0).name());
            assertEquals("Departamento de Tecnologia da Informação", result.getContent().get(0).description());
            assertTrue(result.getContent().get(0).active());
            assertEquals(1, result.getContent().get(0).services().size());
            assertEquals("Suporte Técnico", result.getContent().get(0).services().get(0));

            verify(unitContext).getCurrentUnit(token);
            verify(departmentRepository).findAllWithSearch("unit-123", null, pageable);
        }

        @Test
        @DisplayName("Deve buscar departamentos com filtro de busca")
        void shouldGetDepartmentsWithSearchFilter() {
            var pageable = PageRequest.of(0, 10);
            Page<Department> page = new PageImpl<>(List.of(department));

            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(departmentRepository.findAllWithSearch("unit-123", "TI", pageable)).thenReturn(page);

            var result = departmentService.getAllDepartments(token, 0, 10, "TI");

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());

            verify(unitContext).getCurrentUnit(token);
            verify(departmentRepository).findAllWithSearch("unit-123", "TI", pageable);
        }

        @Test
        @DisplayName("Deve retornar página vazia quando não há departamentos")
        void shouldReturnEmptyPage() {
            var pageable = PageRequest.of(0, 10);
            Page<Department> page = new PageImpl<>(List.of());

            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(departmentRepository.findAllWithSearch("unit-123", null, pageable)).thenReturn(page);

            var result = departmentService.getAllDepartments(token, 0, 10, null);

            assertNotNull(result);
            assertEquals(0, result.getTotalElements());
            assertTrue(result.getContent().isEmpty());

            verify(unitContext).getCurrentUnit(token);
            verify(departmentRepository).findAllWithSearch("unit-123", null, pageable);
        }
    }

    // =========================================================
    // DELETE DEPARTMENT
    // =========================================================

    @Nested
    @DisplayName("Testes de deleção de departamento")
    class DeleteDepartmentTests {

        @Test
        @DisplayName("Deve deletar departamento com sucesso")
        void shouldDeleteDepartmentSuccessfully() {
            when(departmentRepository.findByDepartmentId("dept-123")).thenReturn(Optional.of(department));
            doNothing().when(departmentRepository).delete(department);

            var response = departmentService.deleteDepartment("dept-123");

            assertNotNull(response);
            assertEquals("dept-123", response.departmentId());
            assertEquals("TI", response.name());

            verify(departmentRepository).findByDepartmentId("dept-123");
            verify(departmentRepository).delete(department);
        }

        @Test
        @DisplayName("Deve lançar exceção quando departamento não existe")
        void shouldThrowWhenDepartmentDoesNotExist() {
            when(departmentRepository.findByDepartmentId("dept-123")).thenReturn(Optional.empty());

            var exception = assertThrows(
                    DepartmentNotFoundException.class,
                    () -> departmentService.deleteDepartment("dept-123")
            );

            assertEquals("Departamento não encontrado com ID: dept-123", exception.getMessage());

            verify(departmentRepository).findByDepartmentId("dept-123");
            verify(departmentRepository, never()).delete(any(Department.class));
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
            var totalDto = mock(ResponseCountTotalDepartmentsStatisticsDto.class);
            var percentageDto = mock(ResponseDepartmentPercentagesStatisticsDto.class);
            var servicesByDepartment = List.of(mock(ResponseCountServicesByDepartmentsStatisticsDto.class));
            var createdByMonth = List.of(mock(ResponseDepartmentsCreatedByMonthStatisticsDto.class));

            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(departmentRepository.countTotalDepartmentsStatisticsDto("unit-123")).thenReturn(totalDto);
            when(departmentRepository.getDepartmentPercentagesStatisticsDto("unit-123")).thenReturn(percentageDto);
            when(departmentRepository.countServicesByDepartmentStatisticsDto("unit-123")).thenReturn(servicesByDepartment);
            when(departmentRepository.countDepartmentsCreatedByMonth("unit-123")).thenReturn(createdByMonth);

            var response = departmentService.getStatistics(token);

            assertNotNull(response);
            assertEquals(totalDto, response.countTotalDepartmentsStatistics());
            assertEquals(percentageDto, response.departmentPercentagesStatistics());
            assertEquals(servicesByDepartment, response.countServicesByDepartments());
            assertEquals(createdByMonth, response.departmentsCreatedByMonthStatistics());

            verify(unitContext).getCurrentUnit(token);
            verify(departmentRepository).countTotalDepartmentsStatisticsDto("unit-123");
            verify(departmentRepository).getDepartmentPercentagesStatisticsDto("unit-123");
            verify(departmentRepository).countServicesByDepartmentStatisticsDto("unit-123");
            verify(departmentRepository).countDepartmentsCreatedByMonth("unit-123");
        }

        @Test
        @DisplayName("Deve retornar estatísticas mesmo com dados vazios")
        void shouldReturnStatisticsEvenWithEmptyData() {
            when(unitContext.getCurrentUnit(token)).thenReturn(unit);
            when(departmentRepository.countTotalDepartmentsStatisticsDto("unit-123")).thenReturn(null);
            when(departmentRepository.getDepartmentPercentagesStatisticsDto("unit-123")).thenReturn(null);
            when(departmentRepository.countServicesByDepartmentStatisticsDto("unit-123")).thenReturn(List.of());
            when(departmentRepository.countDepartmentsCreatedByMonth("unit-123")).thenReturn(List.of());

            var response = departmentService.getStatistics(token);

            assertNotNull(response);
            assertNull(response.countTotalDepartmentsStatistics());
            assertNull(response.departmentPercentagesStatistics());
            assertTrue(response.countServicesByDepartments().isEmpty());
            assertTrue(response.departmentsCreatedByMonthStatistics().isEmpty());

            verify(unitContext).getCurrentUnit(token);
            verify(departmentRepository).countTotalDepartmentsStatisticsDto("unit-123");
            verify(departmentRepository).getDepartmentPercentagesStatisticsDto("unit-123");
            verify(departmentRepository).countServicesByDepartmentStatisticsDto("unit-123");
            verify(departmentRepository).countDepartmentsCreatedByMonth("unit-123");
        }
    }

    // =========================================================
    // FIND DEPARTMENT BY ID (AUXILIAR)
    // =========================================================

    @Nested
    @DisplayName("Testes do método auxiliar findDepartmentById")
    class FindDepartmentByIdTests {

        @Test
        @DisplayName("Deve encontrar departamento com sucesso")
        void shouldFindDepartmentSuccessfully() {
            when(departmentRepository.findByDepartmentId("dept-123")).thenReturn(Optional.of(department));

            var result = departmentService.deleteDepartment("dept-123");

            assertNotNull(result);
            assertEquals("dept-123", result.departmentId());

            verify(departmentRepository).findByDepartmentId("dept-123");
        }

        @Test
        @DisplayName("Deve lançar DepartmentNotFoundException quando departamento não existe")
        void shouldThrowDepartmentNotFoundExceptionWhenDepartmentDoesNotExist() {
            when(departmentRepository.findByDepartmentId("dept-123")).thenReturn(Optional.empty());

            var exception = assertThrows(
                    DepartmentNotFoundException.class,
                    () -> departmentService.deleteDepartment("dept-123")
            );

            assertEquals("Departamento não encontrado com ID: dept-123", exception.getMessage());

            verify(departmentRepository).findByDepartmentId("dept-123");
        }
    }

    // =========================================================
    // TO RESPONSE
    // =========================================================

    @Nested
    @DisplayName("Testes de conversão para DTO")
    class ToResponseTests {

        @Test
        @DisplayName("Deve converter Department sem serviços para ResponseDepartmentDto")
        void shouldConvertDepartmentToResponseWithoutServices() {
            department.setServices(new ArrayList<>());

            when(departmentRepository.findByDepartmentId("dept-123")).thenReturn(Optional.of(department));
            when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> invocation.getArgument(0));

            var dto = new UpdateDepartmentDto(
                    "dept-123",
                    "TI Atualizado",
                    null,
                    null
            );

            var response = departmentService.updateDepartment(dto);

            assertNotNull(response);
            assertTrue(response.services().isEmpty());
        }
    }
}