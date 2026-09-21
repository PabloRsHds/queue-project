package br.com.queue.controller.attendance;

import br.com.queue.dtos.attendance.allAttendances.ResponseAllAttendances;
import br.com.queue.dtos.attendance.start.StartAttendanceDto;
import br.com.queue.dtos.attendance.start.FinishAttendanceDto;
import br.com.queue.dtos.attendance.finish.ResponseAttendanceDto;
import br.com.queue.dtos.attendance.finish.ResponseFinishAttendanceDto;
import br.com.queue.dtos.attendance.statistics.ResponseAttendanceDashboardDto;
import br.com.queue.service.attendance.AttendanceService;
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

@RestController
@RequestMapping("/attendances")
@RequiredArgsConstructor
@Tag(name = "Atendimentos", description = "Gerenciamento de atendimentos iniciados, finalizados e estatísticas")
@SecurityRequirement(name = "bearerAuth")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_ATTENDANT')")
    @Operation(
            summary = "Iniciar atendimento",
            description = "Inicia um novo atendimento para uma senha emitida. " +
                    "Requer perfil ATTENDANT."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Atendimento iniciado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou senha já em atendimento"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para iniciar atendimentos")
    })
    public ResponseEntity<ResponseAttendanceDto> startAttendance(
            JwtAuthenticationToken token,
            @RequestBody StartAttendanceDto dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.attendanceService.startAttendance(token, dto));
    }

    @PatchMapping("/finish")
    @PreAuthorize("hasAuthority('SCOPE_ATTENDANT')")
    @Operation(
            summary = "Finalizar atendimento",
            description = "Finaliza um atendimento em andamento. " +
                    "O ID do atendimento deve ser informado no corpo da requisição. " +
                    "Requer perfil ATTENDANT."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Atendimento finalizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Atendimento não encontrado")
    })
    public ResponseEntity<ResponseFinishAttendanceDto> finishAttendance(
            @RequestBody FinishAttendanceDto dto
    ) {
        return ResponseEntity.ok()
                .body(this.attendanceService.finishAttendance(dto));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_ATTENDANT')")
    @Operation(
            summary = "Listar atendimentos",
            description = "Retorna uma lista paginada de todos os atendimentos. Endpoint público."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    public ResponseEntity<Page<ResponseAllAttendances>> getAllAttendances(
            @Parameter(description = "Número da página (inicia em 0)", example = "0")
            @RequestParam int page,
            @Parameter(description = "Quantidade de registros por página", example = "10")
            @RequestParam int size
    ) {
        return ResponseEntity.ok()
                .body(this.attendanceService.getAllAttendances(page, size));
    }

    @DeleteMapping("/{attendanceId}")
    @PreAuthorize("hasAuthority('SCOPE_ATTENDANT')")
    @Operation(
            summary = "Remover atendimento",
            description = "Remove logicamente um atendimento pelo ID. " +
                    "Requer perfil ATTENDANT."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Atendimento removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Atendimento não encontrado")
    })
    public ResponseEntity<Void> deleteAttendance(@PathVariable String attendanceId) {
        this.attendanceService.deleteAttendance(attendanceId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER', 'SCOPE_ATTENDANT')")
    @Operation(
            summary = "Estatísticas de atendimentos",
            description = "Retorna métricas consolidadas (dashboard) dos atendimentos da unidade do token. " +
                    "Requer perfil ADMIN, MANAGER ou ATTENDANT."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estatísticas retornadas com sucesso")
    })
    public ResponseEntity<ResponseAttendanceDashboardDto> getAttendanceStatistics(
            JwtAuthenticationToken token
    ) {
        return ResponseEntity.ok()
                .body(this.attendanceService.getAttendanceStatistics(token));
    }
}