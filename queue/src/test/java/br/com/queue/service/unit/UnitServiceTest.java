package br.com.queue.service.unit;

import br.com.queue.dtos.unit.CreateUnitDto;
import br.com.queue.dtos.unit.ResponseUnitDto;
import br.com.queue.dtos.unit.UpdateUnitDto;
import br.com.queue.entities.unit.Unit;
import br.com.queue.infra.unit.UnitIsPresentException;
import br.com.queue.infra.unit.UnitNotFoundException;
import br.com.queue.repositories.unit.UnitRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnitServiceTest {

    @Mock
    private UnitRepository unitRepository;

    @InjectMocks
    private UnitService unitService;

    private Unit unit;
    private CreateUnitDto createUnitDto;
    private UpdateUnitDto updateUnitDto;

    @BeforeEach
    void setUp() {

        unit = new Unit();
        unit.setUnitId("unit-123");
        unit.setName("Unidade Teste");
        unit.setAddress("Rua Teste, 123");
        unit.setActive(true);
        unit.setCreatedAt(
                LocalDateTime.of(2026, 8, 27, 10, 30)
        );

        createUnitDto = new CreateUnitDto(
                "Nova Unidade",
                "Rua Nova, 456"
        );

        updateUnitDto = new UpdateUnitDto(
                "unit-123",
                "Unidade Atualizada",
                "Rua Atualizada, 789",
                false
        );
    }


    // =========================================================
    // CREATE UNIT
    // =========================================================

    @Nested
    @DisplayName("Testes de criação de unidade")
    class CreateUnitTests {

        @Test
        @DisplayName("Deve criar unidade com sucesso")
        void shouldCreateUnitSuccessfully() {

            when(unitRepository.existsByName("Nova Unidade"))
                    .thenReturn(false);

            when(unitRepository.save(any(Unit.class)))
                    .thenAnswer(invocation -> {

                        var entity = invocation.getArgument(0, Unit.class);

                        entity.setUnitId("unit-456");

                        return entity;
                    });

            var response = unitService.createUnit(createUnitDto);

            assertNotNull(response);

            assertEquals("unit-456", response.unitId());
            assertEquals("Nova Unidade", response.name());
            assertEquals("Rua Nova, 456", response.address());
            assertTrue(response.active());

            assertNotNull(response.createdAt());
            assertNull(response.updatedAt());

            verify(unitRepository).existsByName("Nova Unidade");
            verify(unitRepository).save(any(Unit.class));
        }


        @Test
        @DisplayName("Não deve criar unidade quando nome já existe")
        void shouldNotCreateWhenNameAlreadyExists() {

            when(unitRepository.existsByName("Nova Unidade"))
                    .thenReturn(true);

            var exception = assertThrows(
                    UnitIsPresentException.class,
                    () -> unitService.createUnit(createUnitDto)
            );

            assertEquals(
                    "Uma unidade com o nome 'Nova Unidade' já existe.",
                    exception.getMessage()
            );

            verify(unitRepository).existsByName("Nova Unidade");

            verify(unitRepository, never())
                    .save(any(Unit.class));
        }
    }


    // =========================================================
    // UPDATE UNIT
    // =========================================================

    @Nested
    @DisplayName("Testes de atualização de unidade")
    class UpdateUnitTests {

        @Test
        @DisplayName("Deve atualizar unidade com sucesso")
        void shouldUpdateUnitSuccessfully() {

            when(unitRepository.findById("unit-123"))
                    .thenReturn(Optional.of(unit));

            when(unitRepository.existsByName("Unidade Atualizada"))
                    .thenReturn(false);

            when(unitRepository.save(any(Unit.class)))
                    .thenAnswer(invocation ->
                            invocation.getArgument(0, Unit.class)
                    );

            var response = unitService.updateUnit(updateUnitDto);

            assertNotNull(response);

            assertEquals("unit-123", response.unitId());
            assertEquals("Unidade Atualizada", response.name());
            assertEquals("Rua Atualizada, 789", response.address());
            assertFalse(response.active());

            assertNotNull(response.updatedAt());

            verify(unitRepository).findById("unit-123");
            verify(unitRepository).existsByName("Unidade Atualizada");
            verify(unitRepository).save(unit);
        }


        @Test
        @DisplayName("Deve lançar exceção quando unidade não existe")
        void shouldThrowWhenUnitDoesNotExist() {

            when(unitRepository.findById("unit-123"))
                    .thenReturn(Optional.empty());

            var exception = assertThrows(
                    UnitNotFoundException.class,
                    () -> unitService.updateUnit(updateUnitDto)
            );

            assertEquals(
                    "Unidade não encontrada com ID: unit-123",
                    exception.getMessage()
            );

            verify(unitRepository).findById("unit-123");

            verify(unitRepository, never())
                    .save(any(Unit.class));

            verify(unitRepository, never())
                    .existsByName(anyString());
        }


        @Test
        @DisplayName("Não deve atualizar quando nome já existe")
        void shouldThrowWhenUpdatedNameAlreadyExists() {

            when(unitRepository.findById("unit-123"))
                    .thenReturn(Optional.of(unit));

            when(unitRepository.existsByName("Unidade Atualizada"))
                    .thenReturn(true);

            var exception = assertThrows(
                    UnitIsPresentException.class,
                    () -> unitService.updateUnit(updateUnitDto)
            );

            assertEquals(
                    "Uma unidade com o nome 'Unidade Atualizada' já existe.",
                    exception.getMessage()
            );

            verify(unitRepository).findById("unit-123");
            verify(unitRepository).existsByName("Unidade Atualizada");

            verify(unitRepository, never())
                    .save(any(Unit.class));
        }


        @Test
        @DisplayName("Não deve validar nome quando nome não foi informado")
        void shouldNotValidateNameWhenNameIsNull() {

            var dto = new UpdateUnitDto(
                    "unit-123",
                    null,
                    "Novo Endereço",
                    true
            );

            when(unitRepository.findById("unit-123"))
                    .thenReturn(Optional.of(unit));

            when(unitRepository.save(any(Unit.class)))
                    .thenAnswer(invocation ->
                            invocation.getArgument(0, Unit.class)
                    );

            var response = unitService.updateUnit(dto);

            assertNotNull(response);

            assertEquals("Unidade Teste", response.name());
            assertEquals("Novo Endereço", response.address());
            assertTrue(response.active());

            verify(unitRepository).findById("unit-123");

            verify(unitRepository, never())
                    .existsByName(anyString());

            verify(unitRepository).save(unit);
        }


        @Test
        @DisplayName("Não deve validar nome quando nome está em branco")
        void shouldNotValidateBlankName() {

            var dto = new UpdateUnitDto(
                    "unit-123",
                    "   ",
                    "Novo Endereço",
                    true
            );

            when(unitRepository.findById("unit-123"))
                    .thenReturn(Optional.of(unit));

            when(unitRepository.save(any(Unit.class)))
                    .thenAnswer(invocation ->
                            invocation.getArgument(0, Unit.class)
                    );

            var response = unitService.updateUnit(dto);

            assertNotNull(response);

            assertEquals("Unidade Teste", response.name());
            assertEquals("Novo Endereço", response.address());

            verify(unitRepository).findById("unit-123");

            verify(unitRepository, never())
                    .existsByName(anyString());

            verify(unitRepository).save(unit);
        }


        @Test
        @DisplayName("Deve permitir manter o mesmo nome da unidade")
        void shouldAllowSameName() {

            var dto = new UpdateUnitDto(
                    "unit-123",
                    "Unidade Teste",
                    null,
                    null
            );

            when(unitRepository.findById("unit-123"))
                    .thenReturn(Optional.of(unit));

            when(unitRepository.save(any(Unit.class)))
                    .thenAnswer(invocation ->
                            invocation.getArgument(0, Unit.class)
                    );

            var response = unitService.updateUnit(dto);

            assertNotNull(response);
            assertEquals("Unidade Teste", response.name());

            verify(unitRepository).findById("unit-123");

            verify(unitRepository, never())
                    .existsByName(anyString());

            verify(unitRepository).save(unit);
        }


        @Test
        @DisplayName("Deve atualizar somente os campos informados")
        void shouldUpdateOnlyProvidedFields() {

            var dto = new UpdateUnitDto(
                    "unit-123",
                    "Nova Unidade",
                    null,
                    null
            );

            when(unitRepository.findById("unit-123"))
                    .thenReturn(Optional.of(unit));

            when(unitRepository.existsByName("Nova Unidade"))
                    .thenReturn(false);

            when(unitRepository.save(any(Unit.class)))
                    .thenAnswer(invocation ->
                            invocation.getArgument(0, Unit.class)
                    );

            unitService.updateUnit(dto);

            assertEquals("Nova Unidade", unit.getName());

            assertEquals(
                    "Rua Teste, 123",
                    unit.getAddress()
            );

            assertTrue(unit.getActive());

            assertNotNull(unit.getUpdatedAt());

            verify(unitRepository).save(unit);
        }


        @Test
        @DisplayName("Não deve alterar updatedAt quando nenhum campo for informado")
        void shouldNotUpdateUpdatedAtWhenNoFieldsAreProvided() {

            var originalUpdatedAt =
                    LocalDateTime.of(2026, 8, 27, 15, 45);

            unit.setUpdatedAt(originalUpdatedAt);

            var dto = new UpdateUnitDto(
                    "unit-123",
                    null,
                    null,
                    null
            );

            when(unitRepository.findById("unit-123"))
                    .thenReturn(Optional.of(unit));

            when(unitRepository.save(any(Unit.class)))
                    .thenAnswer(invocation ->
                            invocation.getArgument(0, Unit.class)
                    );

            unitService.updateUnit(dto);

            assertEquals(
                    "Unidade Teste",
                    unit.getName()
            );

            assertEquals(
                    "Rua Teste, 123",
                    unit.getAddress()
            );

            assertTrue(unit.getActive());

            assertEquals(
                    originalUpdatedAt,
                    unit.getUpdatedAt()
            );

            verify(unitRepository).save(unit);
        }
    }


    // =========================================================
    // DELETE UNIT
    // =========================================================

    @Nested
    @DisplayName("Testes de exclusão de unidade")
    class DeleteUnitTests {

        @Test
        @DisplayName("Deve deletar unidade com sucesso")
        void shouldDeleteUnitSuccessfully() {

            when(unitRepository.findById("unit-123"))
                    .thenReturn(Optional.of(unit));

            doNothing()
                    .when(unitRepository)
                    .delete(unit);

            var response = unitService.deleteUnit("unit-123");

            assertNotNull(response);

            assertEquals("unit-123", response.unitId());
            assertEquals("Unidade Teste", response.name());
            assertEquals("Rua Teste, 123", response.address());
            assertTrue(response.active());

            assertEquals(
                    "27/08/2026 10:30",
                    response.createdAt()
            );

            assertNull(response.updatedAt());

            verify(unitRepository).findById("unit-123");
            verify(unitRepository).delete(unit);
        }


        @Test
        @DisplayName("Deve lançar exceção quando unidade não existe")
        void shouldThrowWhenDeletingNonExistingUnit() {

            when(unitRepository.findById("unit-123"))
                    .thenReturn(Optional.empty());

            var exception = assertThrows(
                    UnitNotFoundException.class,
                    () -> unitService.deleteUnit("unit-123")
            );

            assertEquals(
                    "Unidade não encontrada com ID: unit-123",
                    exception.getMessage()
            );

            verify(unitRepository).findById("unit-123");

            verify(unitRepository, never())
                    .delete(any(Unit.class));
        }
    }


    // =========================================================
    // GET ALL UNITS
    // =========================================================

    @Nested
    @DisplayName("Testes de busca de unidades")
    class GetAllUnitsTests {

        @Test
        @DisplayName("Deve buscar unidades com paginação")
        void shouldGetAllUnitsWithPagination() {

            var pageable = PageRequest.of(0, 10);

            Page<ResponseUnitDto> page =
                    new PageImpl<>(List.of());

            when(unitRepository.findAllWithSearch(
                    isNull(),
                    eq(pageable)
            )).thenReturn(page);

            var result = unitService.getAllUnits(
                    0,
                    10,
                    null
            );

            assertNotNull(result);
            assertEquals(0, result.getTotalElements());

            verify(unitRepository).findAllWithSearch(
                    isNull(),
                    eq(pageable)
            );
        }


        @Test
        @DisplayName("Deve remover espaços da pesquisa")
        void shouldTrimSearch() {

            var pageable = PageRequest.of(1, 5);

            Page<ResponseUnitDto> page =
                    new PageImpl<>(List.of());

            when(unitRepository.findAllWithSearch(
                    eq("rodrigo"),
                    eq(pageable)
            )).thenReturn(page);

            var result = unitService.getAllUnits(
                    1,
                    5,
                    "   rodrigo   "
            );

            assertNotNull(result);

            verify(unitRepository).findAllWithSearch(
                    eq("rodrigo"),
                    eq(pageable)
            );
        }


        @Test
        @DisplayName("Deve transformar pesquisa vazia em null")
        void shouldConvertBlankSearchToNull() {

            var pageable = PageRequest.of(0, 10);

            Page<ResponseUnitDto> page =
                    new PageImpl<>(List.of());

            when(unitRepository.findAllWithSearch(
                    isNull(),
                    eq(pageable)
            )).thenReturn(page);

            var result = unitService.getAllUnits(
                    0,
                    10,
                    "   "
            );

            assertNotNull(result);

            verify(unitRepository).findAllWithSearch(
                    isNull(),
                    eq(pageable)
            );
        }
    }
}