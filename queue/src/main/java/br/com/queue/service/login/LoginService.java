package br.com.queue.service.login;

import br.com.queue.dtos.loginDto.RequestLoginDto;
import br.com.queue.dtos.loginDto.ResponseUserForLogin;
import br.com.queue.dtos.tokenDto.ResponseTokens;
import br.com.queue.enums.Role;
import br.com.queue.infra.unit.UnitNotFoundException;
import br.com.queue.infra.user.UserInactiveException;
import br.com.queue.infra.user.UserNotFoundException;
import br.com.queue.infra.user.UserPasswordInvalidException;
import br.com.queue.infra.user.UserUnitMismatchException;
import br.com.queue.repositories.unit.UnitRepository;
import br.com.queue.repositories.user.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@RequiredArgsConstructor
@Service
@Slf4j
public class LoginService {

    private final UserRepository userRepository;
    private final UnitRepository unitRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    // ========================================== LOGIN ==============================================================

    public ResponseTokens login(RequestLoginDto request, HttpServletResponse response) {
        log.info("Tentativa de login para usuário: {}, unidade: {}", request.emailOrUsername(), request.unitId());

        // Faço uma verificação para ver se o usuário existe, e também verifico se o e-mail e a senha estão corretos
        var user = this.verifyUser(request.unitId(), request.emailOrUsername(), request.password());

        log.info("Login bem-sucedido para usuário: {}, role: {}", user.userId(), user.role());

        // Retorno os tokens caso o usuário exista
        return this.generateTokens(user.userId(), user.role(), user.unitId(), response);
    }

    public ResponseUserForLogin verifyUser(String unitId, String emailOrUsername, String password) {
        log.debug("Verificando credenciais para usuário: {}", emailOrUsername);

        var user = this.userRepository.findByEmailOrUsername(emailOrUsername)
                .orElseThrow(() -> {
                    log.warn("Usuário não encontrado: {}", emailOrUsername);
                    return new UserNotFoundException("Usuário ou senha estão incorretos.");
                });

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Senha incorreta para o usuário: {}", emailOrUsername);
            throw new UserPasswordInvalidException("Usuário ou senha estão incorretos.");
        }

        if (user.getRole() != Role.ADMIN) {
            var unit = this.unitRepository.findById(unitId)
                    .orElseThrow(() -> {
                        log.warn("Unidade não encontrada: {}", unitId);
                        return new UnitNotFoundException("Unidade não encontrada. ");
                    });

            if (!user.getUnit().getUnitId().equals(unit.getUnitId())) {
                log.warn("Usuário {} não pertence à unidade {}", user.getUserId(), unitId);
                throw new UserUnitMismatchException(
                        "Usuário não pertence à unidade: " + user.getUnit().getName()
                );
            }

            log.debug("Usuário {} verificado com sucesso para a unidade {}", user.getUserId(), unitId);
            return new ResponseUserForLogin(
                    user.getUserId(),
                    null,
                    user.getRole().name(),
                    user.getUnit().getUnitId()
            );
        }

        log.debug("Usuário ADMIN {} verificado com sucesso", user.getUserId());
        return new ResponseUserForLogin(
                user.getUserId(),
                null,
                user.getRole().name(),
                unitId
        );
    }

    // Metodo de geração de tokens e refreshTokens
    public ResponseTokens generateTokens(
            String userId,
            String role,
            String unitId,
            HttpServletResponse response) {
        log.info("Gerando tokens para usuário: {}, role: {}, unidade: {}", userId, role, unitId);

        var expireToken = LocalDateTime.now().plusMinutes(10).toInstant(ZoneOffset.of("-03:00"));
        var now = Instant.now();

        var claims = JwtClaimsSet.builder()
                .issuer("QUEUE-LOGIN")
                .issuedAt(now)
                .subject(userId)
                .expiresAt(expireToken)
                .claim("SCOPE", role)
                .claim("UNIT_ID", unitId)
                .build();

        var expireRefreshToken = LocalDateTime.now().plusDays(30).toInstant(ZoneOffset.of("-03:00"));

        var claimsRefresh = JwtClaimsSet.builder()
                .issuer("QUEUE-LOGIN")
                .issuedAt(now)
                .subject(userId)
                .expiresAt(expireRefreshToken)
                .claim("SCOPE", role)
                .claim("UNIT_ID", unitId)
                .build();

        var accessTokenJwt = this.jwtEncoder.encode(JwtEncoderParameters.from(claims));
        var refreshTokenJwt = this.jwtEncoder.encode(JwtEncoderParameters.from(claimsRefresh));

        if (accessTokenJwt == null || refreshTokenJwt == null) {
            log.error("Falha ao gerar tokens para o usuário: {}", userId);
            throw new JwtEncodingException("Falha ao gerar tokens para o usuário");
        }

        var accessToken = accessTokenJwt.getTokenValue();
        var refreshToken = refreshTokenJwt.getTokenValue();

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofDays(30))
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        log.debug("Tokens gerados com sucesso para o usuário: {}", userId);
        return new ResponseTokens(accessToken);
    }
    // ================================================================================================================

    // ====================================== LOGOUT =================================================================

    public void logout(HttpServletResponse response) {
        log.info("Realizando logout");

        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        log.debug("Logout realizado com sucesso, cookie de refresh removido");
    }

    // ======================================== REFRESH TOKENS ========================================================

    public ResponseTokens refreshTokens(String refreshToken, HttpServletResponse response) {
        log.info("Tentativa de refresh de tokens");

        var jwt = this.jwtDecoder.decode(refreshToken);

        var user = this.userRepository.findByUserId(jwt.getSubject())
                .orElseThrow(() -> {
                    log.warn("Usuário não encontrado no refresh: {}", jwt.getSubject());
                    return new UserNotFoundException("Usuário não encontrado: " + jwt.getSubject());
                });

        if (!user.getActive()) {
            log.warn("Tentativa de refresh para usuário inativo: {}", user.getUserId());
            throw new UserInactiveException("Usuário inativo: " + user.getUserId());
        }

        var unitId = jwt.getClaimAsString("UNIT_ID");

        log.info("Refresh bem-sucedido para usuário: {}", user.getUserId());
        return this.generateTokens(
                user.getUserId(),
                user.getRole().name(),
                unitId,
                response
        );
    }
}