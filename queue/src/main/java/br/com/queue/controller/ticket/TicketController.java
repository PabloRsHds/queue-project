package br.com.queue.controller.ticket;

import br.com.queue.dtos.ticket.allTickets.ResponseAllTicketsDto;
import br.com.queue.dtos.ticket.attendance.ResponseTicketsForAttendance;
import br.com.queue.dtos.ticket.callTicket.CallTicketDto;
import br.com.queue.dtos.ticket.create.CreateTicketDto;
import br.com.queue.dtos.ticket.ResponseTicketDto;
import br.com.queue.dtos.ticket.finishTicket.FinishTicketDto;
import br.com.queue.service.ticket.TicketService;
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
@RequestMapping("/tickets")
@RequiredArgsConstructor
@Tag(name = "Senhas", description = "Gerenciamento de senhas emitidas, chamadas e finalizadas")
@SecurityRequirement(name = "bearerAuth")
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_RECEPTION')")
    @Operation(
            summary = "Emitir senha",
            description = "Cria uma nova senha para atendimento vinculada à unidade do token. " +
                    "Requer perfil RECEPTION."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Senha emitida com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para emitir senhas")
    })
    public ResponseEntity<ResponseTicketDto> createTicket(
            JwtAuthenticationToken token,
            @RequestBody CreateTicketDto dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.ticketService.createTicket(token, dto));
    }

    @PatchMapping("/finish")
    @PreAuthorize("hasAnyAuthority('SCOPE_ATTENDANT')")
    @Operation(
            summary = "Finalizar senha",
            description = "Finaliza o atendimento de uma senha, encerrando o ciclo de atendimento. " +
                    "O ID da senha deve ser informado no corpo da requisição. " +
                    "Requer perfil ATTENDANT."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Senha finalizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Senha não encontrada")
    })
    public ResponseEntity<ResponseTicketDto> finishTicket(@RequestBody FinishTicketDto dto) {
        return ResponseEntity.ok(this.ticketService.finishTicket(dto));
    }

    @PatchMapping("/call/{ticketId}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ATTENDANT')")
    @Operation(
            summary = "Chamar próxima senha",
            description = "Chama a próxima senha da fila para atendimento pelo atendente autenticado. " +
                    "Requer perfil ATTENDANT."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Senha chamada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Nenhuma senha disponível na fila")
    })
    public ResponseEntity<ResponseTicketDto> callTicket(@PathVariable String ticketId) {
        return ResponseEntity.ok(this.ticketService.callTicket(ticketId));
    }

    @PatchMapping("/call/customer/{ticketId}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ATTENDANT')")
    @Operation(
            summary = "Chamar cliente específico",
            description = "Chama para atendimento um cliente específico, cuja senha já foi emitida. " +
                    "Requer perfil ATTENDANT."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente chamado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Senha não encontrada")
    })
    public ResponseEntity<ResponseTicketDto> callCustomer(@PathVariable String ticketId) {
        return ResponseEntity.ok(this.ticketService.callCustomer(ticketId));
    }

    @GetMapping
    @SecurityRequirements
    @Operation(
            summary = "Listar todas as senhas",
            description = "Retorna uma lista paginada de todas as senhas emitidas. Endpoint público."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    public ResponseEntity<Page<ResponseAllTicketsDto>> getAllTickets(
            @Parameter(description = "Número da página (inicia em 0)", example = "0")
            @RequestParam int page,
            @Parameter(description = "Quantidade de registros por página", example = "10")
            @RequestParam int size
    ) {
        return ResponseEntity.ok(this.ticketService.getAllTickets(page, size));
    }

    @GetMapping("/tickets-for-attendance")
    @PreAuthorize("hasAnyAuthority('SCOPE_ATTENDANT')")
    @Operation(
            summary = "Listar senhas em atendimento",
            description = "Retorna uma lista paginada das senhas que o atendente autenticado " +
                    "está atendendo no momento. Requer perfil ATTENDANT."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    public ResponseEntity<Page<ResponseTicketsForAttendance>> getTicketsForAttendance(
            JwtAuthenticationToken token,
            @Parameter(description = "Número da página (inicia em 0)", example = "0")
            @RequestParam int page,
            @Parameter(description = "Quantidade de registros por página", example = "10")
            @RequestParam int size
    ) {
        return ResponseEntity.ok(this.ticketService.getTicketsByAttendant(token, page, size));
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyAuthority('SCOPE_ATTENDANT')")
    @Operation(
            summary = "Histórico de senhas do atendente",
            description = "Retorna uma lista paginada do histórico de senhas atendidas pelo " +
                    "atendente autenticado. Requer perfil ATTENDANT."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico retornado com sucesso")
    })
    public ResponseEntity<Page<ResponseTicketsForAttendance>> getHistoryTicketsByAttendant(
            JwtAuthenticationToken token,
            @Parameter(description = "Número da página (inicia em 0)", example = "0")
            @RequestParam int page,
            @Parameter(description = "Quantidade de registros por página", example = "10")
            @RequestParam int size
    ) {
        return ResponseEntity.ok(this.ticketService.getHistoryTicketsByAttendant(token, page, size));
    }

    @GetMapping("/{ticketId}")
    @SecurityRequirements
    @Operation(
            summary = "Buscar senha por ID",
            description = "Retorna os dados detalhados de uma senha específica. Endpoint público."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Senha encontrada"),
            @ApiResponse(responseCode = "404", description = "Senha não encontrada")
    })
    public ResponseEntity<ResponseTicketDto> getTicketById(@PathVariable String ticketId) {
        return ResponseEntity.ok(this.ticketService.getTicketById(ticketId));
    }

    @DeleteMapping("/{ticketId}")
    @PreAuthorize("hasAnyAuthority('SCOPE_RECEPTION')")
    @Operation(
            summary = "Remover senha",
            description = "Remove logicamente uma senha pelo ID e retorna os dados atualizados. " +
                    "Requer perfil RECEPTION."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Senha removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Senha não encontrada")
    })
    public ResponseEntity<ResponseTicketDto> deleteTicket(@PathVariable String ticketId) {
        return ResponseEntity.ok(this.ticketService.deleteTicket(ticketId));
    }

    @PatchMapping("/status/{ticketId}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ATTENDANT')")
    @Operation(
            summary = "Cancelar senha",
            description = "Cancela uma senha em atendimento, marcando-a como cancelada. " +
                    "Requer perfil ATTENDANT."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Senha cancelada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Senha não encontrada")
    })
    public ResponseEntity<ResponseTicketDto> cancelTicket(@PathVariable String ticketId) {
        return ResponseEntity.ok(this.ticketService.cancelTicket(ticketId));
    }
}