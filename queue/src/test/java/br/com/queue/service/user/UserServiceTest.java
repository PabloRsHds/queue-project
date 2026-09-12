package br.com.queue.service.user;

import br.com.queue.dtos.user.ResponseUserDto;
import br.com.queue.dtos.user.create.CreateUserDto;
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

import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ServiceManagementRepository serviceManagementRepository;

    @Mock
    private UnitContext unitContext;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtAuthenticationToken token;

    @InjectMocks
    private UserService userService;

    private User user;
    private Unit unit;
    private ServiceManagement service;
    private CreateUserDto createUserDto;
    private UpdateUserDto updateUserDto;

    @BeforeEach
    void setUp() {

        unit = new Unit();
        unit.setUnitId("unit-123");
        unit.setName("Unidade Teste");

        service = new ServiceManagement();
        service.setServiceManagementId("service-123");
        service.setName("Serviço Teste");

        user = new User();
        user.setUserId("user-123");
        user.setUsername("testuser");
        user.setName("Test");
        user.setSurname("User");
        user.setEmail("test@test.com");
        user.setPhone("11999999999");
        user.setPassword("encodedPassword");
        user.setRole(Role.ATTENDANT);
        user.setCounterNumber(1);
        user.setActive(true);
        user.setUnit(unit);
        user.setServices(Set.of(service));
        user.setCreatedAt(LocalDateTime.of(2026, 8, 27, 10, 30));

        createUserDto = new CreateUserDto(
                "newuser",
                "New",
                "User",
                "11988888888",
                "new@test.com",
                "password123",
                "ATTENDANT",
                2,
                new HashSet<>(Set.of("service-123"))
        );

        updateUserDto = new UpdateUserDto(
                "user-123",
                "updateduser",
                "Updated",
                "User",
                "11977777777",
                "updated@test.com",
                "newPassword123",
                "ATTENDANT",
                true,
                3,
                new HashSet<>(Set.of("service-123"))
        );

    }


    // =========================================================
    // CREATE USER
    // =========================================================

    @Nested
    @DisplayName("Testes de criação de usuário")
    class CreateUserTests {

        @Test
        @DisplayName("Deve criar usuário com sucesso")
        void shouldCreateUserSuccessfully() {

            when(unitContext.getCurrentUnit(token))
                    .thenReturn(unit);

            when(userRepository.existsByUsername(anyString()))
                    .thenReturn(false);

            when(userRepository.existsByEmail(anyString()))
                    .thenReturn(false);

            when(userRepository.existsByPhone(anyString()))
                    .thenReturn(false);

            when(userRepository.existsByCounterNumber(anyInt()))
                    .thenReturn(false);

            when(serviceManagementRepository
                    .findAllByServiceManagementIdIn(anySet()))
                    .thenReturn(Set.of(service));

            when(passwordEncoder.encode("password123"))
                    .thenReturn("encodedPassword");

            when(userRepository.save(any(User.class)))
                    .thenReturn(user);

            var response = userService.createUser(token, createUserDto);

            assertNotNull(response);

            assertEquals("testuser", response.username());
            assertEquals("Test", response.name());
            assertEquals("User", response.surname());
            assertEquals("11999999999", response.phone());
            assertEquals("test@test.com", response.email());
            assertEquals("ATTENDANT", response.role());
            assertEquals(1, response.counterNumber());
            assertTrue(response.active());

            verify(unitContext).getCurrentUnit(token);
            verify(userRepository).save(any(User.class));
            verify(passwordEncoder).encode("password123");
        }


        @Test
        @DisplayName("Não deve salvar quando username já existe")
        void shouldNotCreateWhenUsernameAlreadyExists() {

            when(unitContext.getCurrentUnit(token))
                    .thenReturn(unit);

            when(userRepository.existsByUsername("newuser"))
                    .thenReturn(true);

            assertThrows(
                    UserValidationException.class,
                    () -> userService.createUser(token, createUserDto)
            );

            verify(userRepository, never()).save(any(User.class));
        }


        @Test
        @DisplayName("Não deve salvar quando email já existe")
        void shouldNotCreateWhenEmailAlreadyExists() {

            when(unitContext.getCurrentUnit(token))
                    .thenReturn(unit);

            when(userRepository.existsByUsername("newuser"))
                    .thenReturn(false);

            when(userRepository.existsByEmail("new@test.com"))
                    .thenReturn(true);

            assertThrows(
                    UserValidationException.class,
                    () -> userService.createUser(token, createUserDto)
            );

            verify(userRepository, never()).save(any(User.class));
        }


        @Test
        @DisplayName("Não deve salvar quando telefone já existe")
        void shouldNotCreateWhenPhoneAlreadyExists() {

            when(unitContext.getCurrentUnit(token))
                    .thenReturn(unit);

            when(userRepository.existsByUsername("newuser"))
                    .thenReturn(false);

            when(userRepository.existsByEmail("new@test.com"))
                    .thenReturn(false);

            when(userRepository.existsByPhone("11988888888"))
                    .thenReturn(true);

            assertThrows(
                    UserValidationException.class,
                    () -> userService.createUser(token, createUserDto)
            );

            verify(userRepository, never()).save(any(User.class));
        }


        @Test
        @DisplayName("Não deve salvar quando guichê já está ocupado")
        void shouldNotCreateWhenCounterAlreadyExists() {

            when(unitContext.getCurrentUnit(token))
                    .thenReturn(unit);

            when(userRepository.existsByUsername("newuser"))
                    .thenReturn(false);

            when(userRepository.existsByEmail("new@test.com"))
                    .thenReturn(false);

            when(userRepository.existsByPhone("11988888888"))
                    .thenReturn(false);

            when(userRepository.existsByCounterNumber(2))
                    .thenReturn(true);

            assertThrows(
                    UserValidationException.class,
                    () -> userService.createUser(token, createUserDto)
            );

            verify(userRepository, never()).save(any(User.class));
        }
    }


    // =========================================================
    // VALIDATE CREATE USER
    // =========================================================

    @Nested
    @DisplayName("Testes de validação")
    class ValidationTests {

        @Test
        @DisplayName("Deve validar usuário corretamente")
        void shouldValidateUserSuccessfully() {

            when(userRepository.existsByUsername(anyString()))
                    .thenReturn(false);

            when(userRepository.existsByEmail(anyString()))
                    .thenReturn(false);

            when(userRepository.existsByPhone(anyString()))
                    .thenReturn(false);

            when(userRepository.existsByCounterNumber(anyInt()))
                    .thenReturn(false);

            assertDoesNotThrow(
                    () -> userService.validateCreateUser(createUserDto)
            );
        }


        @Test
        @DisplayName("Deve lançar exceção para username duplicado")
        void shouldThrowWhenUsernameAlreadyExists() {

            when(userRepository.existsByUsername("newuser"))
                    .thenReturn(true);

            var exception = assertThrows(
                    UserValidationException.class,
                    () -> userService.validateCreateUser(createUserDto)
            );

            assertEquals(
                    "Usuário já cadastrado com username: newuser",
                    exception.getMessage()
            );
        }


        @Test
        @DisplayName("Deve lançar exceção para email duplicado")
        void shouldThrowWhenEmailAlreadyExists() {

            when(userRepository.existsByUsername("newuser"))
                    .thenReturn(false);

            when(userRepository.existsByEmail("new@test.com"))
                    .thenReturn(true);

            var exception = assertThrows(
                    UserValidationException.class,
                    () -> userService.validateCreateUser(createUserDto)
            );

            assertEquals(
                    "E-mail já cadastrado: new@test.com",
                    exception.getMessage()
            );
        }


        @Test
        @DisplayName("Deve lançar exceção para telefone duplicado")
        void shouldThrowWhenPhoneAlreadyExists() {

            when(userRepository.existsByUsername("newuser"))
                    .thenReturn(false);

            when(userRepository.existsByEmail("new@test.com"))
                    .thenReturn(false);

            when(userRepository.existsByPhone("11988888888"))
                    .thenReturn(true);

            var exception = assertThrows(
                    UserValidationException.class,
                    () -> userService.validateCreateUser(createUserDto)
            );

            assertEquals(
                    "Telefone já cadastrado: 11988888888",
                    exception.getMessage()
            );
        }


        @Test
        @DisplayName("Deve lançar exceção para guichê duplicado")
        void shouldThrowWhenCounterAlreadyExists() {

            when(userRepository.existsByUsername("newuser"))
                    .thenReturn(false);

            when(userRepository.existsByEmail("new@test.com"))
                    .thenReturn(false);

            when(userRepository.existsByPhone("11988888888"))
                    .thenReturn(false);

            when(userRepository.existsByCounterNumber(2))
                    .thenReturn(true);

            var exception = assertThrows(
                    UserValidationException.class,
                    () -> userService.validateCreateUser(createUserDto)
            );

            assertEquals(
                    "Guichê já alocado: 2",
                    exception.getMessage()
            );
        }


        @Test
        @DisplayName("Não deve validar telefone nulo como duplicado")
        void shouldAllowNullPhone() {

            var dto = new CreateUserDto(
                    "newuser",
                    "New",
                    "User",
                    null,
                    "new@test.com",
                    "password123",
                    "ATTENDANT",
                    null,
                    new HashSet<>(Set.of("service-123"))
            );

            when(userRepository.existsByUsername(anyString()))
                    .thenReturn(false);

            when(userRepository.existsByEmail(anyString()))
                    .thenReturn(false);

            when(userRepository.existsByPhone(isNull()))
                    .thenReturn(true);

            when(userRepository.existsByCounterNumber(isNull()))
                    .thenReturn(false);

            assertDoesNotThrow(
                    () -> userService.validateCreateUser(dto)
            );
        }
    }


    // =========================================================
    // TO ENTITY
    // =========================================================

    @Nested
    @DisplayName("Testes de conversão para Entity")
    class ToEntityTests {

        @Test
        @DisplayName("Deve converter CreateUserDto para User corretamente")
        void shouldConvertDtoToEntity() {

            when(serviceManagementRepository
                    .findAllByServiceManagementIdIn(createUserDto.serviceIds()))
                    .thenReturn(Set.of(service));

            when(passwordEncoder.encode("password123"))
                    .thenReturn("encodedPassword");

            var entity = userService.toEntity(createUserDto, unit);

            assertNotNull(entity);

            assertEquals("newuser", entity.getUsername());
            assertEquals("New", entity.getName());
            assertEquals("User", entity.getSurname());
            assertEquals("11988888888", entity.getPhone());
            assertEquals("new@test.com", entity.getEmail());
            assertEquals("encodedPassword", entity.getPassword());
            assertEquals(Role.ATTENDANT, entity.getRole());
            assertEquals(2, entity.getCounterNumber());
            assertTrue(entity.getActive());
            assertEquals(unit, entity.getUnit());
            assertEquals(Set.of(service), entity.getServices());
            assertNotNull(entity.getCreatedAt());

            verify(passwordEncoder).encode("password123");
            verify(serviceManagementRepository)
                    .findAllByServiceManagementIdIn(createUserDto.serviceIds());
        }
    }


    // =========================================================
    // UPDATE USER
    // =========================================================

    @Nested
    @DisplayName("Testes de atualização")
    class UpdateUserTests {

        @Test
        @DisplayName("Deve atualizar usuário com sucesso")
        void shouldUpdateUserSuccessfully() {

            when(userRepository.findByUserId("user-123"))
                    .thenReturn(Optional.of(user));

            when(userRepository.existsByUsername("updateduser"))
                    .thenReturn(false);

            when(userRepository.existsByEmail("updated@test.com"))
                    .thenReturn(false);

            when(userRepository.existsByPhone("11977777777"))
                    .thenReturn(false);

            when(userRepository.existsByCounterNumber(3))
                    .thenReturn(false);

            when(serviceManagementRepository
                    .findAllByServiceManagementIdIn(updateUserDto.serviceIds()))
                    .thenReturn(Set.of(service));

            when(passwordEncoder.encode("newPassword123"))
                    .thenReturn("newEncodedPassword");

            when(userRepository.save(any(User.class)))
                    .thenReturn(user);

            var response = userService.updateUser(updateUserDto);

            assertNotNull(response);

            assertEquals("updateduser", response.username());

            verify(userRepository).findByUserId("user-123");
            verify(userRepository).save(user);
            verify(passwordEncoder).encode("newPassword123");
        }


        @Test
        @DisplayName("Deve lançar exceção quando usuário não existe")
        void shouldThrowWhenUserDoesNotExist() {

            when(userRepository.findByUserId("user-123"))
                    .thenReturn(Optional.empty());

            assertThrows(
                    UserNotFoundException.class,
                    () -> userService.updateUser(updateUserDto)
            );

            verify(userRepository, never()).save(any(User.class));
        }


        @Test
        @DisplayName("Deve lançar exceção quando username já existe")
        void shouldThrowWhenUpdatedUsernameAlreadyExists() {

            when(userRepository.findByUserId("user-123"))
                    .thenReturn(Optional.of(user));

            when(userRepository.existsByUsername("updateduser"))
                    .thenReturn(true);

            var exception = assertThrows(
                    UserValidationException.class,
                    () -> userService.updateUser(updateUserDto)
            );

            assertEquals(
                    "Usuário já cadastrado com username: updateduser",
                    exception.getMessage()
            );

            verify(userRepository, never()).save(any(User.class));
        }


        @Test
        @DisplayName("Deve lançar exceção quando email já existe")
        void shouldThrowWhenUpdatedEmailAlreadyExists() {

            when(userRepository.findByUserId("user-123"))
                    .thenReturn(Optional.of(user));

            when(userRepository.existsByUsername("updateduser"))
                    .thenReturn(false);

            when(userRepository.existsByEmail("updated@test.com"))
                    .thenReturn(true);

            assertThrows(
                    UserValidationException.class,
                    () -> userService.updateUser(updateUserDto)
            );

            verify(userRepository, never()).save(any(User.class));
        }


        @Test
        @DisplayName("Deve lançar exceção quando telefone já existe")
        void shouldThrowWhenUpdatedPhoneAlreadyExists() {

            when(userRepository.findByUserId("user-123"))
                    .thenReturn(Optional.of(user));

            when(userRepository.existsByUsername("updateduser"))
                    .thenReturn(false);

            when(userRepository.existsByEmail("updated@test.com"))
                    .thenReturn(false);

            when(userRepository.existsByPhone("11977777777"))
                    .thenReturn(true);

            assertThrows(
                    UserValidationException.class,
                    () -> userService.updateUser(updateUserDto)
            );

            verify(userRepository, never()).save(any(User.class));
        }


        @Test
        @DisplayName("Deve lançar exceção quando guichê já está ocupado")
        void shouldThrowWhenUpdatedCounterAlreadyExists() {

            when(userRepository.findByUserId("user-123"))
                    .thenReturn(Optional.of(user));

            when(userRepository.existsByUsername("updateduser"))
                    .thenReturn(false);

            when(userRepository.existsByEmail("updated@test.com"))
                    .thenReturn(false);

            when(userRepository.existsByPhone("11977777777"))
                    .thenReturn(false);

            when(userRepository.existsByCounterNumber(3))
                    .thenReturn(true);

            assertThrows(
                    UserValidationException.class,
                    () -> userService.updateUser(updateUserDto)
            );

            verify(userRepository, never()).save(any(User.class));
        }


        @Test
        @DisplayName("Deve remover guichê quando role for alterada para MANAGER")
        void shouldRemoveCounterWhenRoleChangesFromAttendant() {

            var dto = new UpdateUserDto(
                    "user-123",
                    "updateduser",
                    "Updated",
                    "User",
                    "11977777777",
                    "updated@test.com",
                    null,
                    "MANAGER",
                    true,
                    null,
                    new HashSet<>(Set.of("service-123"))
            );

            when(userRepository.findByUserId("user-123"))
                    .thenReturn(Optional.of(user));

            when(userRepository.existsByUsername("updateduser"))
                    .thenReturn(false);

            when(userRepository.existsByEmail("updated@test.com"))
                    .thenReturn(false);

            when(userRepository.existsByPhone("11977777777"))
                    .thenReturn(false);

            when(serviceManagementRepository
                    .findAllByServiceManagementIdIn(dto.serviceIds()))
                    .thenReturn(Set.of(service));

            when(userRepository.save(any(User.class)))
                    .thenReturn(user);

            userService.updateUser(dto);

            assertEquals(Role.MANAGER, user.getRole());
            assertNull(user.getCounterNumber());

            verify(userRepository).save(user);
        }


        @Test
        @DisplayName("Deve atualizar somente os campos informados")
        void shouldUpdateOnlyProvidedFields() {

            var dto = new UpdateUserDto(
                    "user-123",
                    "novoUsername",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            when(userRepository.findByUserId("user-123"))
                    .thenReturn(Optional.of(user));

            when(userRepository.existsByUsername("novoUsername"))
                    .thenReturn(false);

            when(userRepository.save(any(User.class)))
                    .thenReturn(user);

            userService.updateUser(dto);

            assertEquals("novoUsername", user.getUsername());

            // Os campos abaixo devem permanecer iguais
            assertEquals("Test", user.getName());
            assertEquals("User", user.getSurname());
            assertEquals("11999999999", user.getPhone());
            assertEquals("test@test.com", user.getEmail());
            assertEquals(Role.ATTENDANT, user.getRole());
            assertEquals(1, user.getCounterNumber());
        }


        @Test
        @DisplayName("Não deve atualizar senha quando senha for nula")
        void shouldNotUpdatePasswordWhenPasswordIsNull() {

            var dto = new UpdateUserDto(
                    "user-123",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            when(userRepository.findByUserId("user-123"))
                    .thenReturn(Optional.of(user));

            when(userRepository.save(any(User.class)))
                    .thenReturn(user);

            userService.updateUser(dto);

            assertEquals("encodedPassword", user.getPassword());

            verify(passwordEncoder, never()).encode(anyString());
        }
    }


    // =========================================================
    // DELETE USER
    // =========================================================

    @Nested
    @DisplayName("Testes de exclusão")
    class DeleteUserTests {

        @Test
        @DisplayName("Deve deletar usuário com sucesso")
        void shouldDeleteUserSuccessfully() {

            when(userRepository.findByUserId("user-123"))
                    .thenReturn(Optional.of(user));

            doNothing()
                    .when(userRepository)
                    .delete(user);

            var response = userService.deleteUser("user-123");

            assertNotNull(response);
            assertEquals("testuser", response.username());

            verify(userRepository).findByUserId("user-123");
            verify(userRepository).delete(user);
        }


        @Test
        @DisplayName("Deve lançar exceção quando usuário não existe")
        void shouldThrowWhenDeletingNonExistingUser() {

            when(userRepository.findByUserId("user-123"))
                    .thenReturn(Optional.empty());

            assertThrows(
                    UserNotFoundException.class,
                    () -> userService.deleteUser("user-123")
            );

            verify(userRepository, never())
                    .delete(any(User.class));
        }
    }


    // =========================================================
    // GET ALL USERS
    // =========================================================

    @Nested
    @DisplayName("Testes de busca de usuários")
    class GetUsersTests {

        @Test
        @DisplayName("Deve buscar usuários com paginação")
        void shouldGetAllUsersWithPagination() {

            var pageable = PageRequest.of(0, 10);

            Page<ResponseUserDto> page =
                    new PageImpl<>(List.of());

            when(unitContext.getCurrentUnit(token))
                    .thenReturn(unit);

            when(userRepository.findAllWithSearch(
                    eq("unit-123"),
                    isNull(),
                    any(PageRequest.class)
            )).thenReturn(page);

            var result = userService.getAllUsers(
                    token,
                    0,
                    10,
                    null
            );

            assertNotNull(result);
            assertEquals(0, result.getTotalElements());

            verify(userRepository).findAllWithSearch(
                    eq("unit-123"),
                    isNull(),
                    eq(pageable)
            );
        }


        @Test
        @DisplayName("Deve remover espaços da pesquisa")
        void shouldTrimSearch() {

            when(unitContext.getCurrentUnit(token))
                    .thenReturn(unit);

            when(userRepository.findAllWithSearch(
                    eq("unit-123"),
                    eq("rodrigo"),
                    any(PageRequest.class)
            )).thenReturn(new PageImpl<>(List.of()));

            userService.getAllUsers(
                    token,
                    1,
                    5,
                    "   rodrigo   "
            );

            verify(userRepository).findAllWithSearch(
                    eq("unit-123"),
                    eq("rodrigo"),
                    eq(PageRequest.of(1, 5))
            );
        }


        @Test
        @DisplayName("Deve transformar pesquisa vazia em null")
        void shouldConvertBlankSearchToNull() {

            when(unitContext.getCurrentUnit(token))
                    .thenReturn(unit);

            when(userRepository.findAllWithSearch(
                    eq("unit-123"),
                    isNull(),
                    any(PageRequest.class)
            )).thenReturn(new PageImpl<>(List.of()));

            userService.getAllUsers(
                    token,
                    0,
                    10,
                    "   "
            );

            verify(userRepository).findAllWithSearch(
                    eq("unit-123"),
                    isNull(),
                    eq(PageRequest.of(0, 10))
            );
        }
    }


    // =========================================================
    // GET USER BY ID
    // =========================================================

    @Nested
    @DisplayName("Testes de usuário por ID")
    class GetUserByIdTests {

        @Test
        @DisplayName("Deve buscar usuário por ID")
        void shouldGetUserByIdSuccessfully() {

            when(userRepository.findByUserId("user-123"))
                    .thenReturn(Optional.of(user));

            var response =
                    userService.getUserById("user-123");

            assertNotNull(response);

            assertEquals("user-123", response.userId());
            assertEquals("testuser", response.username());
            assertEquals("Test", response.name());
            assertEquals("User", response.surname());
            assertEquals("11999999999", response.phone());
            assertEquals("test@test.com", response.email());
            assertEquals("ATTENDANT", response.role());
            assertEquals(1, response.counterNumber());
            assertTrue(response.active());
        }


        @Test
        @DisplayName("Deve lançar exceção quando usuário não existe")
        void shouldThrowWhenUserNotFound() {

            when(userRepository.findByUserId("user-123"))
                    .thenReturn(Optional.empty());

            assertThrows(
                    UserNotFoundException.class,
                    () -> userService.getUserById("user-123")
            );
        }
    }


    // =========================================================
    // GET USER BY TOKEN
    // =========================================================

    @Nested
    @DisplayName("Testes de usuário pelo token")
    class GetUserByTokenTests {

        @Test
        @DisplayName("Deve buscar usuário pelo token")
        void shouldGetUserByTokenSuccessfully() {

            when(token.getName())
                    .thenReturn("user-123");

            when(userRepository.findByUserId("user-123"))
                    .thenReturn(Optional.of(user));

            var response =
                    userService.getUserByToken(token);

            assertNotNull(response);
            assertEquals("user-123", response.userId());
            assertEquals("testuser", response.username());

            verify(userRepository).findByUserId("user-123");
        }


        @Test
        @DisplayName("Deve lançar exceção quando usuário do token não existe")
        void shouldThrowWhenTokenUserNotFound() {

            when(token.getName())
                    .thenReturn("user-123");

            when(userRepository.findByUserId("user-123"))
                    .thenReturn(Optional.empty());

            assertThrows(
                    UserNotFoundException.class,
                    () -> userService.getUserByToken(token)
            );
        }
    }


    // =========================================================
    // STATISTICS
    // =========================================================

    @Nested
    @DisplayName("Testes de estatísticas")
    class StatisticsTests {

        @Test
        @DisplayName("Deve buscar estatísticas da unidade")
        void shouldGetStatisticsSuccessfully() {

            when(unitContext.getCurrentUnit(token))
                    .thenReturn(unit);

            when(userRepository.countTotalUsersStatisticsDto("unit-123"))
                    .thenReturn(null);

            when(userRepository.getUserPercentagesStatisticsDto("unit-123"))
                    .thenReturn(null);

            when(userRepository.countUsersCreatedByMonth("unit-123"))
                    .thenReturn(null);

            when(userRepository.countServicesByUserStatistics("unit-123"))
                    .thenReturn(null);

            when(userRepository.countUsersByRoleStatistics("unit-123"))
                    .thenReturn(null);

            var response =
                    userService.getUserStatistics(token);

            assertNotNull(response);

            verify(userRepository)
                    .countTotalUsersStatisticsDto("unit-123");

            verify(userRepository)
                    .getUserPercentagesStatisticsDto("unit-123");

            verify(userRepository)
                    .countUsersCreatedByMonth("unit-123");

            verify(userRepository)
                    .countServicesByUserStatistics("unit-123");

            verify(userRepository)
                    .countUsersByRoleStatistics("unit-123");
        }
    }


    // =========================================================
    // TO RESPONSE
    // =========================================================

    @Nested
    @DisplayName("Testes de conversão para Response")
    class ResponseTests {

        @Test
        @DisplayName("Deve converter User para ResponseUserDto")
        void shouldConvertUserToResponse() {

            var response =
                    userService.toResponse(user);

            assertNotNull(response);

            assertEquals(user.getUserId(), response.userId());
            assertEquals(user.getUsername(), response.username());
            assertEquals(user.getName(), response.name());
            assertEquals(user.getSurname(), response.surname());
            assertEquals(user.getPhone(), response.phone());
            assertEquals(user.getEmail(), response.email());
            assertEquals(user.getRole().name(), response.role());
            assertEquals(user.getCounterNumber(), response.counterNumber());
            assertEquals(user.getActive(), response.active());

            assertNull(response.updatedAt());
        }


        @Test
        @DisplayName("Deve formatar updatedAt corretamente")
        void shouldFormatUpdatedAt() {

            user.setUpdatedAt(
                    LocalDateTime.of(2026, 8, 27, 15, 45)
            );

            var response =
                    userService.toResponse(user);

            assertEquals(
                    LocalDateTime.of(2026, 8, 27, 15, 45),
                    response.updatedAt()
            );
        }
    }


    // =========================================================
    // TO INFO RESPONSE
    // =========================================================

    @Nested
    @DisplayName("Testes de conversão para ResponseInfo")
    class InfoResponseTests {

        @Test
        @DisplayName("Deve converter User para ResponseUserInfoDto")
        void shouldConvertUserToInfoResponse() {

            var response =
                    userService.toInfoResponse(user);

            assertNotNull(response);

            assertEquals(user.getUserId(), response.userId());
            assertEquals(user.getUsername(), response.username());
            assertEquals(user.getName(), response.name());
            assertEquals(user.getSurname(), response.surname());
            assertEquals(user.getPhone(), response.phone());
            assertEquals(user.getEmail(), response.email());
            assertEquals(user.getRole().name(), response.role());
            assertEquals(user.getCounterNumber(), response.counterNumber());
            assertEquals(user.getActive(), response.active());

            assertEquals(
                    "27/08/2026 10:30",
                    response.createdAt()
            );

            assertNull(response.updatedAt());

            assertNotNull(response.serviceNames());
            assertEquals(
                    Set.of("Serviço Teste"),
                    response.serviceNames()
            );
        }


        @Test
        @DisplayName("Deve retornar múltiplos serviços")
        void shouldConvertMultipleServices() {

            var service2 = new ServiceManagement();
            service2.setServiceManagementId("service-456");
            service2.setName("Outro Serviço");

            user.setServices(
                    Set.of(service, service2)
            );

            var response =
                    userService.toInfoResponse(user);

            assertEquals(
                    Set.of("Serviço Teste", "Outro Serviço"),
                    response.serviceNames()
            );
        }
    }
}
