package br.com.queue.controller.serviceManagement;

import br.com.queue.dtos.serviceManagement.create.CreateServiceManagementDto;
import br.com.queue.dtos.serviceManagement.ResponseServiceManagementDto;
import br.com.queue.dtos.serviceManagement.getServiceDto.ResponseGetServiceByIdDto;
import br.com.queue.dtos.serviceManagement.list_service.ResponseServicesForCreatedUser;
import br.com.queue.dtos.serviceManagement.statistics.ResponseServiceDashBoardDto;
import br.com.queue.dtos.serviceManagement.update.UpdateServiceManagementDto;
import br.com.queue.service.serviceManagement.ServiceManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
@Tag(name = "Serviços", description = "Gerenciamento de serviços oferecidos pelas unidades")
@SecurityRequirement(name = "bearerAuth")
public class ServiceManagementController {

    private final ServiceManagementService serviceManagementService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    @Operation(
            summary = "Criar serviço",
            description = "Cria um novo serviço vinculado à unidade do token autenticado. " +
                    "Requer perfil ADMIN ou MANAGER."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Serviço criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para criar serviços")
    })
    public ResponseEntity<ResponseServiceManagementDto> create(
            JwtAuthenticationToken token,
            @RequestBody CreateServiceManagementDto dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.serviceManagementService.create(token, dto));
    }

    @PatchMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    @Operation(
            summary = "Atualizar serviço",
            description = "Atualiza parcialmente os dados de um serviço existente. " +
                    "O ID do serviço deve ser informado no corpo da requisição. " +
                    "Requer perfil ADMIN ou MANAGER."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Serviço atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    })
    public ResponseEntity<ResponseServiceManagementDto> update(
            @RequestBody UpdateServiceManagementDto dto
    ) {
        return ResponseEntity.ok()
                .body(this.serviceManagementService.update(dto));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    @Operation(
            summary = "Listar serviços",
            description = "Retorna uma lista paginada de serviços da unidade do token. " +
                    "O parâmetro 'search' filtra por nome ou descrição."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    public ResponseEntity<Page<ResponseServiceManagementDto>> getAll(
            JwtAuthenticationToken token,
            @Parameter(description = "Número da página (inicia em 0)", example = "0")
            @RequestParam int page,
            @Parameter(description = "Quantidade de registros por página", example = "10")
            @RequestParam int size,
            @Parameter(description = "Filtro por nome ou descrição", required = false)
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok()
                .body(this.serviceManagementService.getAll(token, page, size, search));
    }

    @GetMapping("/{serviceManagementId}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    @Operation(
            summary = "Buscar serviço por ID",
            description = "Retorna os dados detalhados de um serviço específico. " +
                    "Requer perfil ADMIN ou MANAGER."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Serviço encontrado"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    })
    public ResponseEntity<ResponseGetServiceByIdDto> getById(
            @PathVariable String serviceManagementId
    ) {
        return ResponseEntity.ok()
                .body(this.serviceManagementService.getById(serviceManagementId));
    }

    @GetMapping("/service-for-created-user")
    @SecurityRequirements  // 👈 sobrescreve: este endpoint é público
    @Operation(
            summary = "Listar serviços para criação de usuário",
            description = "Retorna uma lista simplificada de serviços usada no formulário " +
                    "de criação de usuário. Endpoint público."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    public ResponseEntity<List<ResponseServicesForCreatedUser>> servicesForCreatedUser() {
        return ResponseEntity.ok()
                .body(this.serviceManagementService.servicesForCreatedUser());
    }

    @DeleteMapping("/{serviceManagementId}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    @Operation(
            summary = "Remover serviço",
            description = "Remove logicamente um serviço pelo ID e retorna os dados atualizados. " +
                    "Requer perfil ADMIN ou MANAGER."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Serviço removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    })
    public ResponseEntity<ResponseServiceManagementDto> delete(
            @PathVariable String serviceManagementId
    ) {
        return ResponseEntity.ok()
                .body(this.serviceManagementService.delete(serviceManagementId));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    @Operation(
            summary = "Estatísticas de serviços",
            description = "Retorna métricas consolidadas (dashboard) dos serviços da unidade do token. " +
                    "Requer perfil ADMIN ou MANAGER."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estatísticas retornadas com sucesso")
    })
    public ResponseEntity<ResponseServiceDashBoardDto> getStatistics(
            JwtAuthenticationToken token
    ) {
        return ResponseEntity.ok()
                .body(this.serviceManagementService.getStatistics(token));
    }
}