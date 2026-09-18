package br.com.queue.controller.user;

import br.com.queue.dtos.user.ResponseUserDto;
import br.com.queue.dtos.user.create.CreateUserDto;
import br.com.queue.dtos.user.get_user.ResponseUserInfoDto;
import br.com.queue.dtos.user.metrics.ResponseUserDashBoardDto;
import br.com.queue.dtos.user.update.UpdateUserDto;
import br.com.queue.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Gerenciamento de usuários do sistema")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    @Operation(
            summary = "Criar usuário",
            description = "Cria um novo usuário vinculado à uma unidade. " +
                    "Requer perfil ADMIN ou MANAGER."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou e-mail já cadastrado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para criar usuários")
    })
    public ResponseEntity<ResponseUserDto> create(
            JwtAuthenticationToken token,
            @RequestBody CreateUserDto dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.userService.create(token, dto));
    }

    @PatchMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER', 'SCOPE_RECEPTION', 'SCOPE_ATTENDANT')")
    @Operation(
            summary = "Atualizar usuário",
            description = "Atualiza parcialmente os dados de um usuário existente. " +
                    "O ID do usuário deve ser informado no corpo da requisição."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<ResponseUserDto> update(
            @RequestBody UpdateUserDto dto
    ) {
        return ResponseEntity.ok()
                .body(this.userService.update(dto));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    @Operation(
            summary = "Listar usuários",
            description = "Retorna uma lista paginada de usuários. " +
                    "O parâmetro 'search' filtra por nome ou e-mail."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    public ResponseEntity<Page<ResponseUserDto>> getAll(
            JwtAuthenticationToken token,
            @Parameter(description = "Número da página (inicia em 0)", example = "0")
            @RequestParam int page,
            @Parameter(description = "Quantidade de registros por página", example = "10")
            @RequestParam int size,
            @Parameter(description = "Filtro por nome ou e-mail", required = false)
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok()
                .body(this.userService.getAll(token, page, size, search));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    @Operation(
            summary = "Buscar usuário por ID",
            description = "Retorna os dados detalhados de um usuário específico."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<ResponseUserInfoDto> getUserById(
            @PathVariable String userId
    ) {
        return ResponseEntity.ok()
                .body(this.userService.getUserById(userId));
    }

    @GetMapping("/token")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER', 'SCOPE_RECEPTION', 'SCOPE_ATTENDANT')")
    @Operation(
            summary = "Buscar usuário autenticado",
            description = "Retorna os dados do usuário extraídos do token JWT atual."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário autenticado retornado com sucesso")
    })
    public ResponseEntity<ResponseUserInfoDto> getUserByToken(
            JwtAuthenticationToken token
    ) {
        return ResponseEntity.ok()
                .body(this.userService.getUserByToken(token));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    @Operation(
            summary = "Remover usuário",
            description = "Remove logicamente um usuário pelo ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<ResponseUserDto> delete(
            @PathVariable String userId
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(this.userService.delete(userId));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    @Operation(
            summary = "Estatísticas de usuários",
            description = "Retorna métricas consolidadas (dashboard) dos usuários da empresa do token."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estatísticas retornadas com sucesso")
    })
    public ResponseEntity<ResponseUserDashBoardDto> getStatistics(
            JwtAuthenticationToken token
    ) {
        return ResponseEntity.ok()
                .body(this.userService.getStatistics(token));
    }
}