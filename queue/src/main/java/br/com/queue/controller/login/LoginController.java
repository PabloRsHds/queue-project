package br.com.queue.controller.login;

import br.com.queue.dtos.loginDto.RequestLoginDto;
import br.com.queue.dtos.tokenDto.ResponseTokens;
import br.com.queue.service.login.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints de login, logout e renovação de tokens")
@SecurityRequirements
public class LoginController {

    private final LoginService loginService;

    @PostMapping
    @Operation(
            summary = "Autenticar usuário",
            description = "Autentica um usuário com e-mail e senha. " +
                    "Retorna o access token (JWT) no corpo da resposta e o refresh token " +
                    "em um cookie HttpOnly. Endpoint público."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    })
    public ResponseEntity<ResponseTokens> login(
            @RequestBody @Valid RequestLoginDto request,
            HttpServletResponse response
    ) {
        var tokens = loginService.login(request, response);
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Encerrar sessão",
            description = "Invalida o refresh token atual e limpa o cookie de autenticação. " +
                    "Endpoint público (não exige token válido para ser chamado)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logout realizado com sucesso")
    })
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        this.loginService.logout(response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh-tokens")
    @Operation(
            summary = "Renovar tokens",
            description = "Gera um novo par de access + refresh tokens a partir do refresh token " +
                    "enviado via cookie HttpOnly. Endpoint público."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens renovados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido ou expirado")
    })
    public ResponseEntity<ResponseTokens> refreshTokens(
            @Parameter(
                    description = "Refresh token armazenado no cookie HttpOnly",
                    required = true
            )
            @CookieValue("refreshToken") String refreshToken,
            HttpServletResponse response
    ) {
        var tokens = this.loginService.refreshTokens(refreshToken, response);
        return ResponseEntity.ok().body(tokens);
    }
}