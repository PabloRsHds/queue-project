package br.com.queue.service.login;

import br.com.queue.dtos.loginDto.RequestLoginDto;
import br.com.queue.dtos.loginDto.ResponseUserForLogin;
import br.com.queue.dtos.tokenDto.ResponseTokens;
import br.com.queue.entities.unit.Unit;
import br.com.queue.entities.user.User;
import br.com.queue.enums.Role;
import br.com.queue.infra.unit.UnitNotFoundException;
import br.com.queue.infra.user.UserInactiveException;
import br.com.queue.infra.user.UserNotFoundException;
import br.com.queue.infra.user.UserPasswordInvalidException;
import br.com.queue.infra.user.UserUnitMismatchException;
import br.com.queue.repositories.unit.UnitRepository;
import br.com.queue.repositories.user.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private JwtDecoder jwtDecoder;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private LoginService loginService;

    private User user;
    private Unit unit;
    private RequestLoginDto loginDto;

    @BeforeEach
    void setUp() {
        unit = new Unit();
        unit.setUnitId("unit-123");
        unit.setName("Unidade Teste");
        unit.setActive(true);

        user = new User();
        user.setUserId("user-123");
        user.setUsername("joao.silva");
        user.setEmail("joao@email.com");
        user.setName("João");
        user.setSurname("Silva");
        user.setPassword("encoded_password");
        user.setRole(Role.ATTENDANT);
        user.setActive(true);
        user.setUnit(unit);
        user.setCreatedAt(LocalDateTime.now());

        // CORRIGIDO: Ordem correta do RequestLoginDto
        loginDto = new RequestLoginDto(
                "unit-123",      // 1º: unitId
                "joao.silva",    // 2º: emailOrUsername
                "password123"    // 3º: password
        );
    }

    // =========================================================
    // LOGIN
    // =========================================================

    @Nested
    @DisplayName("Testes de login")
    class LoginTests {

        @Test
        @DisplayName("Deve realizar login com sucesso para usuário ATTENDANT")
        void shouldLoginSuccessfullyForAttendant() {
            when(userRepository.findByEmailOrUsername("joao.silva")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);
            when(unitRepository.findById("unit-123")).thenReturn(Optional.of(unit));

            when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenAnswer(invocation -> {
                Jwt mockJwt = mock(Jwt.class);
                when(mockJwt.getTokenValue()).thenReturn("access_token_mock");
                return mockJwt;
            });

            var responseTokens = loginService.login(loginDto, response);

            assertNotNull(responseTokens);
            assertEquals("access_token_mock", responseTokens.accessToken());

            verify(userRepository).findByEmailOrUsername("joao.silva");
            verify(passwordEncoder).matches("password123", "encoded_password");
            verify(unitRepository).findById("unit-123");
            verify(jwtEncoder, times(2)).encode(any(JwtEncoderParameters.class));
            verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());
        }

        @Test
        @DisplayName("Deve realizar login com sucesso para usuário ADMIN")
        void shouldLoginSuccessfullyForAdmin() {
            user.setRole(Role.ADMIN);

            when(userRepository.findByEmailOrUsername("joao.silva")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);

            when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenAnswer(invocation -> {
                Jwt mockJwt = mock(Jwt.class);
                when(mockJwt.getTokenValue()).thenReturn("access_token_mock");
                return mockJwt;
            });

            var responseTokens = loginService.login(loginDto, response);

            assertNotNull(responseTokens);
            assertEquals("access_token_mock", responseTokens.accessToken());

            verify(userRepository).findByEmailOrUsername("joao.silva");
            verify(passwordEncoder).matches("password123", "encoded_password");
            verify(unitRepository, never()).findById(anyString());
            verify(jwtEncoder, times(2)).encode(any(JwtEncoderParameters.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário não existe")
        void shouldThrowWhenUserDoesNotExist() {
            when(userRepository.findByEmailOrUsername("joao.silva")).thenReturn(Optional.empty());

            var exception = assertThrows(
                    UserNotFoundException.class,
                    () -> loginService.login(loginDto, response)
            );

            assertEquals("Usuário ou senha estão incorretos.", exception.getMessage());

            verify(userRepository).findByEmailOrUsername("joao.silva");
            verify(passwordEncoder, never()).matches(anyString(), anyString());
            verify(unitRepository, never()).findById(anyString());
            verify(jwtEncoder, never()).encode(any(JwtEncoderParameters.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando senha está incorreta")
        void shouldThrowWhenPasswordIsIncorrect() {
            when(userRepository.findByEmailOrUsername("joao.silva")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(false);

            var exception = assertThrows(
                    UserPasswordInvalidException.class,
                    () -> loginService.login(loginDto, response)
            );

            assertEquals("Usuário ou senha estão incorretos.", exception.getMessage());

            verify(userRepository).findByEmailOrUsername("joao.silva");
            verify(passwordEncoder).matches("password123", "encoded_password");
            verify(unitRepository, never()).findById(anyString());
            verify(jwtEncoder, never()).encode(any(JwtEncoderParameters.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando unidade não existe para usuário não-ADMIN")
        void shouldThrowWhenUnitDoesNotExist() {
            when(userRepository.findByEmailOrUsername("joao.silva")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);
            when(unitRepository.findById("unit-123")).thenReturn(Optional.empty());

            var exception = assertThrows(
                    UnitNotFoundException.class,
                    () -> loginService.login(loginDto, response)
            );

            assertEquals("Unidade não encontrada. ", exception.getMessage());

            verify(userRepository).findByEmailOrUsername("joao.silva");
            verify(passwordEncoder).matches("password123", "encoded_password");
            verify(unitRepository).findById("unit-123");
            verify(jwtEncoder, never()).encode(any(JwtEncoderParameters.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário não pertence à unidade")
        void shouldThrowWhenUserDoesNotBelongToUnit() {
            var anotherUnit = new Unit();
            anotherUnit.setUnitId("unit-456");
            anotherUnit.setName("Outra Unidade");

            user.setUnit(anotherUnit);

            when(userRepository.findByEmailOrUsername("joao.silva")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);
            when(unitRepository.findById("unit-123")).thenReturn(Optional.of(unit));

            var exception = assertThrows(
                    UserUnitMismatchException.class,
                    () -> loginService.login(loginDto, response)
            );

            assertEquals("Usuário não pertence à unidade: " + anotherUnit.getName(), exception.getMessage());

            verify(userRepository).findByEmailOrUsername("joao.silva");
            verify(passwordEncoder).matches("password123", "encoded_password");
            verify(unitRepository).findById("unit-123");
            verify(jwtEncoder, never()).encode(any(JwtEncoderParameters.class));
        }
    }

    // =========================================================
    // VERIFY USER
    // =========================================================

    @Nested
    @DisplayName("Testes de verificação de usuário")
    class VerifyUserTests {

        @Test
        @DisplayName("Deve verificar usuário ATTENDANT com sucesso")
        void shouldVerifyAttendantUserSuccessfully() {
            when(userRepository.findByEmailOrUsername("joao.silva")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);
            when(unitRepository.findById("unit-123")).thenReturn(Optional.of(unit));

            var result = loginService.verifyUser("unit-123", "joao.silva", "password123");

            assertNotNull(result);
            assertEquals("user-123", result.userId());
            assertEquals("ATTENDANT", result.role());
            assertEquals("unit-123", result.unitId());

            verify(userRepository).findByEmailOrUsername("joao.silva");
            verify(passwordEncoder).matches("password123", "encoded_password");
            verify(unitRepository).findById("unit-123");
        }

        @Test
        @DisplayName("Deve verificar usuário ADMIN com sucesso sem validar unidade")
        void shouldVerifyAdminUserSuccessfully() {
            user.setRole(Role.ADMIN);

            when(userRepository.findByEmailOrUsername("joao.silva")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);

            var result = loginService.verifyUser("unit-123", "joao.silva", "password123");

            assertNotNull(result);
            assertEquals("user-123", result.userId());
            assertEquals("ADMIN", result.role());
            assertEquals("unit-123", result.unitId());

            verify(userRepository).findByEmailOrUsername("joao.silva");
            verify(passwordEncoder).matches("password123", "encoded_password");
            verify(unitRepository, never()).findById(anyString());
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário não existe no verify")
        void shouldThrowWhenUserDoesNotExistInVerify() {
            when(userRepository.findByEmailOrUsername("joao.silva")).thenReturn(Optional.empty());

            var exception = assertThrows(
                    UserNotFoundException.class,
                    () -> loginService.verifyUser("unit-123", "joao.silva", "password123")
            );

            assertEquals("Usuário ou senha estão incorretos.", exception.getMessage());

            verify(userRepository).findByEmailOrUsername("joao.silva");
            verify(passwordEncoder, never()).matches(anyString(), anyString());
            verify(unitRepository, never()).findById(anyString());
        }

        @Test
        @DisplayName("Deve lançar exceção quando senha está incorreta no verify")
        void shouldThrowWhenPasswordIsIncorrectInVerify() {
            when(userRepository.findByEmailOrUsername("joao.silva")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(false);

            var exception = assertThrows(
                    UserPasswordInvalidException.class,
                    () -> loginService.verifyUser("unit-123", "joao.silva", "password123")
            );

            assertEquals("Usuário ou senha estão incorretos.", exception.getMessage());

            verify(userRepository).findByEmailOrUsername("joao.silva");
            verify(passwordEncoder).matches("password123", "encoded_password");
            verify(unitRepository, never()).findById(anyString());
        }

        @Test
        @DisplayName("Deve lançar exceção quando unidade não existe no verify")
        void shouldThrowWhenUnitDoesNotExistInVerify() {
            when(userRepository.findByEmailOrUsername("joao.silva")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);
            when(unitRepository.findById("unit-123")).thenReturn(Optional.empty());

            var exception = assertThrows(
                    UnitNotFoundException.class,
                    () -> loginService.verifyUser("unit-123", "joao.silva", "password123")
            );

            assertEquals("Unidade não encontrada. ", exception.getMessage());

            verify(userRepository).findByEmailOrUsername("joao.silva");
            verify(passwordEncoder).matches("password123", "encoded_password");
            verify(unitRepository).findById("unit-123");
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário não pertence à unidade no verify")
        void shouldThrowWhenUserDoesNotBelongToUnitInVerify() {
            var anotherUnit = new Unit();
            anotherUnit.setUnitId("unit-456");
            anotherUnit.setName("Outra Unidade");

            user.setUnit(anotherUnit);

            when(userRepository.findByEmailOrUsername("joao.silva")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);
            when(unitRepository.findById("unit-123")).thenReturn(Optional.of(unit));

            var exception = assertThrows(
                    UserUnitMismatchException.class,
                    () -> loginService.verifyUser("unit-123", "joao.silva", "password123")
            );

            assertEquals("Usuário não pertence à unidade: " + anotherUnit.getName(), exception.getMessage());

            verify(userRepository).findByEmailOrUsername("joao.silva");
            verify(passwordEncoder).matches("password123", "encoded_password");
            verify(unitRepository).findById("unit-123");
        }
    }

    // =========================================================
    // GENERATE TOKENS
    // =========================================================

    @Nested
    @DisplayName("Testes de geração de tokens")
    class GenerateTokensTests {

        @Test
        @DisplayName("Deve gerar tokens com sucesso")
        void shouldGenerateTokensSuccessfully() {
            when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenAnswer(invocation -> {
                Jwt mockJwt = mock(Jwt.class);
                when(mockJwt.getTokenValue()).thenReturn("access_token_mock");
                return mockJwt;
            });

            var result = loginService.generateTokens(
                    "user-123",
                    "ATTENDANT",
                    "unit-123",
                    response
            );

            assertNotNull(result);
            assertEquals("access_token_mock", result.accessToken());

            verify(jwtEncoder, times(2)).encode(any(JwtEncoderParameters.class));
            verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());
        }

        @Test
        @DisplayName("Deve lançar exceção quando falha ao gerar tokens")
        void shouldThrowWhenTokenGenerationFails() {
            // CORRIGIDO: Mock retorna null para o primeiro encode
            when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(null);

            var exception = assertThrows(
                    JwtEncodingException.class,
                    () -> loginService.generateTokens(
                            "user-123",
                            "ATTENDANT",
                            "unit-123",
                            response
                    )
            );

            assertEquals("Falha ao gerar tokens para o usuário", exception.getMessage());

            verify(jwtEncoder, atLeastOnce()).encode(any(JwtEncoderParameters.class));
            verify(response, never()).addHeader(anyString(), anyString());
        }
    }

    // =========================================================
    // LOGOUT
    // =========================================================

    @Nested
    @DisplayName("Testes de logout")
    class LogoutTests {

        @Test
        @DisplayName("Deve realizar logout com sucesso")
        void shouldLogoutSuccessfully() {
            loginService.logout(response);

            verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());
        }
    }

    // =========================================================
    // REFRESH TOKENS
    // =========================================================

    @Nested
    @DisplayName("Testes de refresh de tokens")
    class RefreshTokensTests {

        @Test
        @DisplayName("Deve refreshar tokens com sucesso")
        void shouldRefreshTokensSuccessfully() {
            Jwt jwt = mock(Jwt.class);
            when(jwt.getSubject()).thenReturn("user-123");
            when(jwt.getClaimAsString("UNIT_ID")).thenReturn("unit-123");

            when(jwtDecoder.decode("refresh_token")).thenReturn(jwt);
            when(userRepository.findByUserId("user-123")).thenReturn(Optional.of(user));
            when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenAnswer(invocation -> {
                Jwt mockJwt = mock(Jwt.class);
                when(mockJwt.getTokenValue()).thenReturn("new_access_token");
                return mockJwt;
            });

            var result = loginService.refreshTokens("refresh_token", response);

            assertNotNull(result);
            assertEquals("new_access_token", result.accessToken());

            verify(jwtDecoder).decode("refresh_token");
            verify(userRepository).findByUserId("user-123");
            verify(jwtEncoder, times(2)).encode(any(JwtEncoderParameters.class));
            verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário não existe no refresh")
        void shouldThrowWhenUserDoesNotExistInRefresh() {
            Jwt jwt = mock(Jwt.class);
            when(jwt.getSubject()).thenReturn("user-123");

            when(jwtDecoder.decode("refresh_token")).thenReturn(jwt);
            when(userRepository.findByUserId("user-123")).thenReturn(Optional.empty());

            var exception = assertThrows(
                    UserNotFoundException.class,
                    () -> loginService.refreshTokens("refresh_token", response)
            );

            assertEquals("Usuário não encontrado: user-123", exception.getMessage());

            verify(jwtDecoder).decode("refresh_token");
            verify(userRepository).findByUserId("user-123");
            verify(jwtEncoder, never()).encode(any(JwtEncoderParameters.class));
            verify(response, never()).addHeader(anyString(), anyString());
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário está inativo no refresh")
        void shouldThrowWhenUserIsInactiveInRefresh() {
            user.setActive(false);

            Jwt jwt = mock(Jwt.class);
            when(jwt.getSubject()).thenReturn("user-123");

            when(jwtDecoder.decode("refresh_token")).thenReturn(jwt);
            when(userRepository.findByUserId("user-123")).thenReturn(Optional.of(user));

            var exception = assertThrows(
                    UserInactiveException.class,
                    () -> loginService.refreshTokens("refresh_token", response)
            );

            assertEquals("Usuário inativo: user-123", exception.getMessage());

            verify(jwtDecoder).decode("refresh_token");
            verify(userRepository).findByUserId("user-123");
            verify(jwtEncoder, never()).encode(any(JwtEncoderParameters.class));
            verify(response, never()).addHeader(anyString(), anyString());
        }
    }
}