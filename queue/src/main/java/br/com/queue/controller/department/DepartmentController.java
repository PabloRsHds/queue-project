package br.com.queue.controller.department;

import br.com.queue.dtos.department.ResponseDepartmentDto;
import br.com.queue.dtos.department.create.CreateDepartmentDto;
import br.com.queue.dtos.department.statistics.ResponseDepartmentDashBoardDto;
import br.com.queue.dtos.department.update.UpdateDepartmentDto;
import br.com.queue.service.department.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/departments")
@RequiredArgsConstructor
@Tag(name = "Departamentos", description = "Gerenciamento de departamentos das unidades")
@SecurityRequirement(name = "bearerAuth")
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    @Operation(
            summary = "Criar departamento",
            description = "Cria um novo departamento vinculado à unidade do token autenticado. " +
                    "Requer perfil ADMIN ou MANAGER."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Departamento criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para criar departamentos")
    })
    public ResponseEntity<ResponseDepartmentDto> create(
            JwtAuthenticationToken token,
            @RequestBody @Valid CreateDepartmentDto dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.departmentService.create(token, dto));
    }

    @PatchMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    @Operation(
            summary = "Atualizar departamento",
            description = "Atualiza parcialmente os dados de um departamento existente. " +
                    "O ID do departamento deve ser informado no corpo da requisição. " +
                    "Requer perfil ADMIN ou MANAGER."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Departamento atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Departamento não encontrado")
    })
    public ResponseEntity<ResponseDepartmentDto> update(
            @RequestBody UpdateDepartmentDto dto
    ) {
        return ResponseEntity.ok()
                .body(this.departmentService.update(dto));
    }

    @DeleteMapping("/{departmentId}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    @Operation(
            summary = "Remover departamento",
            description = "Remove logicamente um departamento pelo ID e retorna os dados atualizados. " +
                    "Requer perfil ADMIN ou MANAGER."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Departamento removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Departamento não encontrado")
    })
    public ResponseEntity<ResponseDepartmentDto> delete(@PathVariable String departmentId) {
        return ResponseEntity.ok()
                .body(this.departmentService.delete(departmentId));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    @Operation(
            summary = "Listar departamentos",
            description = "Retorna uma lista paginada de departamentos da unidade do token. " +
                    "O parâmetro 'search' filtra por nome ou descrição."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    public ResponseEntity<Page<ResponseDepartmentDto>> getAll(
            JwtAuthenticationToken token,
            @Parameter(description = "Número da página (inicia em 0)", example = "0")
            @RequestParam int page,
            @Parameter(description = "Quantidade de registros por página", example = "10")
            @RequestParam int size,
            @Parameter(description = "Filtro por nome ou descrição", required = false)
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok()
                .body(this.departmentService.getAll(token, page, size, search));
    }

    @GetMapping("/{departmentId}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    @Operation(
            summary = "Buscar departamento por ID",
            description = "Retorna os dados detalhados de um departamento específico. " +
                    "Requer perfil ADMIN ou MANAGER."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Departamento encontrado"),
            @ApiResponse(responseCode = "404", description = "Departamento não encontrado")
    })
    public ResponseEntity<ResponseDepartmentDto> getById(
            @PathVariable("departmentId") String departmentId
    ) {
        return ResponseEntity.ok()
                .body(this.departmentService.getById(departmentId));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    @Operation(
            summary = "Estatísticas de departamentos",
            description = "Retorna métricas consolidadas (dashboard) dos departamentos da unidade do token. " +
                    "Requer perfil ADMIN ou MANAGER."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estatísticas retornadas com sucesso")
    })
    public ResponseEntity<ResponseDepartmentDashBoardDto> getStatistics(
            JwtAuthenticationToken token
    ) {
        return ResponseEntity.ok()
                .body(this.departmentService.getStatistics(token));
    }
}