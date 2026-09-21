package br.com.queue.controller.customer;

import br.com.queue.dtos.customer.allCustomer.ResponseAllCustomersDto;
import br.com.queue.dtos.customer.create.CreateCustomerDto;
import br.com.queue.dtos.customer.create.ResponseCustomerDto;
import br.com.queue.dtos.customer.getCustomer.ResponseCustomerById;
import br.com.queue.dtos.customer.getCustomer.ResponseGetCustomerIdsAndNames;
import br.com.queue.dtos.customer.statistics.ResponseCustomerDashBoardDto;
import br.com.queue.dtos.customer.update.UpdateCustomerDto;
import br.com.queue.service.customer.CustomerService;
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

import java.util.List;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Gerenciamento de clientes atendidos na recepção")
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_RECEPTION')")
    @Operation(
            summary = "Criar cliente",
            description = "Cria um novo cliente vinculado à unidade do token autenticado. " +
                    "Requer perfil RECEPTION."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou CPF/documento já cadastrado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para criar clientes")
    })
    public ResponseEntity<ResponseCustomerDto> create(
            JwtAuthenticationToken token,
            @RequestBody CreateCustomerDto dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.customerService.create(token, dto));
    }

    @PatchMapping
    @PreAuthorize("hasAuthority('SCOPE_RECEPTION')")
    @Operation(
            summary = "Atualizar cliente",
            description = "Atualiza parcialmente os dados de um cliente existente. " +
                    "O ID do cliente deve ser informado no corpo da requisição. " +
                    "Requer perfil RECEPTION."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<ResponseCustomerDto> update(@RequestBody UpdateCustomerDto dto) {
        return ResponseEntity.ok()
                .body(this.customerService.update(dto));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_RECEPTION')")
    @Operation(
            summary = "Listar clientes",
            description = "Retorna uma lista paginada de clientes da unidade do token. " +
                    "O parâmetro 'search' filtra por nome, CPF ou telefone. " +
                    "Requer perfil RECEPTION."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    public ResponseEntity<Page<ResponseAllCustomersDto>> getAll(
            JwtAuthenticationToken token,
            @Parameter(description = "Número da página (inicia em 0)", example = "0")
            @RequestParam int page,
            @Parameter(description = "Quantidade de registros por página", example = "10")
            @RequestParam int size,
            @Parameter(description = "Filtro por nome, CPF ou telefone", required = false)
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok()
                .body(this.customerService.getAll(token, page, size, search));
    }

    @GetMapping("/ids-and-names")
    @PreAuthorize("hasAuthority('SCOPE_RECEPTION')")
    @Operation(
            summary = "Listar IDs e nomes de clientes",
            description = "Retorna uma lista simplificada (ID + nome) de todos os clientes, " +
                    "usada em seletores e autocompletes. Requer perfil RECEPTION."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    public ResponseEntity<List<ResponseGetCustomerIdsAndNames>> getCustomerIdsAndNames() {
        return ResponseEntity.ok()
                .body(this.customerService.getCustomerIdsAndNames());
    }

    @GetMapping("/{customerId}")
    @PreAuthorize("hasAuthority('SCOPE_RECEPTION')")
    @Operation(
            summary = "Buscar cliente por ID",
            description = "Retorna os dados detalhados de um cliente específico. " +
                    "Requer perfil RECEPTION."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<ResponseCustomerById> getById(@PathVariable String customerId) {
        return ResponseEntity.ok()
                .body(this.customerService.getById(customerId));
    }

    @DeleteMapping("/{customerId}")
    @PreAuthorize("hasAuthority('SCOPE_RECEPTION')")
    @Operation(
            summary = "Remover cliente",
            description = "Remove logicamente um cliente pelo ID e retorna os dados atualizados. " +
                    "Requer perfil RECEPTION."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<ResponseCustomerDto> delete(@PathVariable String customerId) {
        return ResponseEntity.ok()
                .body(this.customerService.delete(customerId));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER', 'SCOPE_RECEPTION')")
    @Operation(
            summary = "Estatísticas de clientes",
            description = "Retorna métricas consolidadas (dashboard) dos clientes da unidade do token. " +
                    "Requer perfil ADMIN, MANAGER ou RECEPTION."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estatísticas retornadas com sucesso")
    })
    public ResponseEntity<ResponseCustomerDashBoardDto> getStatistics(
            JwtAuthenticationToken token
    ) {
        return ResponseEntity.ok()
                .body(this.customerService.getStatistics(token));
    }
}