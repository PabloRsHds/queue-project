package br.com.queue.controller.schedule;

import br.com.queue.dtos.schedule.allSchedules.ResponseAllSchedulesDto;
import br.com.queue.dtos.schedule.create.CreateScheduleDto;
import br.com.queue.dtos.schedule.create.ResponseScheduleDto;
import br.com.queue.dtos.schedule.statistics.ResponseScheduleDashBoardDto;
import br.com.queue.dtos.schedule.update.UpdateScheduleDto;
import br.com.queue.service.schedule.SchedulingService;
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

import java.time.LocalDate;

@RestController
@RequestMapping("/scheduling")
@RequiredArgsConstructor
@Tag(name = "Agendamentos", description = "Gerenciamento de agendamentos de atendimento")
@SecurityRequirement(name = "bearerAuth")
public class ScheduleController {

    private final SchedulingService schedulingService;

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_RECEPTION')")
    @Operation(
            summary = "Criar agendamento",
            description = "Cria um novo agendamento de atendimento vinculado à unidade do token. " +
                    "Requer perfil RECEPTION."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Agendamento criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou horário indisponível"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para criar agendamentos")
    })
    public ResponseEntity<ResponseScheduleDto> createSchedule(
            JwtAuthenticationToken token,
            @RequestBody CreateScheduleDto dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.schedulingService.createSchedule(token, dto));
    }

    @PatchMapping
    @PreAuthorize("hasAuthority('SCOPE_RECEPTION')")
    @Operation(
            summary = "Atualizar agendamento",
            description = "Atualiza parcialmente os dados de um agendamento existente. " +
                    "O ID do agendamento deve ser informado no corpo da requisição. " +
                    "Requer perfil RECEPTION."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agendamento atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    })
    public ResponseEntity<ResponseScheduleDto> updateSchedule(@RequestBody UpdateScheduleDto dto) {
        return ResponseEntity.ok(this.schedulingService.updateSchedule(dto));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_RECEPTION')")
    @Operation(
            summary = "Listar agendamentos",
            description = "Retorna uma lista paginada de agendamentos da unidade do token. " +
                    "Permite filtrar por data específica (`scheduleDate`) e por texto (`search`). " +
                    "Requer perfil RECEPTION."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    public ResponseEntity<Page<ResponseAllSchedulesDto>> getAllSchedules(
            JwtAuthenticationToken token,
            @Parameter(description = "Número da página (inicia em 0)", example = "0")
            @RequestParam int page,
            @Parameter(description = "Quantidade de registros por página", example = "10")
            @RequestParam int size,
            @Parameter(
                    description = "Filtra agendamentos por data específica (formato yyyy-MM-dd)",
                    example = "2026-09-20",
                    required = false
            )
            @RequestParam(required = false) LocalDate scheduleDate,
            @Parameter(
                    description = "Filtro por nome do cliente ou descrição",
                    required = false
            )
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(
                this.schedulingService.getAllSchedules(token, page, size, search, scheduleDate)
        );
    }

    @GetMapping("/{scheduleId}")
    @PreAuthorize("hasAuthority('SCOPE_RECEPTION')")
    @Operation(
            summary = "Buscar agendamento por ID",
            description = "Retorna os dados detalhados de um agendamento específico. " +
                    "Requer perfil RECEPTION."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agendamento encontrado"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    })
    public ResponseEntity<ResponseScheduleDto> getScheduleById(@PathVariable String scheduleId) {
        return ResponseEntity.ok(this.schedulingService.getScheduleById(scheduleId));
    }

    @DeleteMapping("/{scheduleId}")
    @PreAuthorize("hasAuthority('SCOPE_RECEPTION')")
    @Operation(
            summary = "Remover agendamento",
            description = "Remove logicamente um agendamento pelo ID e retorna os dados atualizados. " +
                    "Requer perfil RECEPTION."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agendamento removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    })
    public ResponseEntity<ResponseScheduleDto> deleteSchedule(@PathVariable String scheduleId) {
        return ResponseEntity.ok(this.schedulingService.deleteSchedule(scheduleId));
    }

    @GetMapping("/statistics")
    @Operation(
            summary = "Estatísticas de agendamentos",
            description = "Retorna métricas consolidadas (dashboard) dos agendamentos da unidade do token. " +
                    "Requer perfil ADMIN, MANAGER ou RECEPTION."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estatísticas retornadas com sucesso")
    })
    public ResponseEntity<ResponseScheduleDashBoardDto> getScheduleStatistics(
            JwtAuthenticationToken token
    ) {
        return ResponseEntity.ok(this.schedulingService.getScheduleStatistics(token));
    }
}