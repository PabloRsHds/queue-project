package br.com.queue.controller.unit;

import br.com.queue.dtos.unit.CreateUnitDto;
import br.com.queue.dtos.unit.ResponseUnitDto;
import br.com.queue.dtos.unit.UpdateUnitDto;
import br.com.queue.service.unit.UnitService;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/units")
@RequiredArgsConstructor
@Tag(name = "Unidades", description = "Gerenciamento de unidades do sistema")
@SecurityRequirement(name = "bearerAuth")
public class UnitController {

    private final UnitService unitService;

    @GetMapping
    @Operation(
            summary = "Listar unidades",
            description = "Retorna uma lista paginada de unidades. " +
                    "O parâmetro 'search' filtra por nome ou endereço."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    public ResponseEntity<Page<ResponseUnitDto>> getAllUnits(
            @Parameter(description = "Número da página (inicia em 0)", example = "0")
            @RequestParam int page,
            @Parameter(description = "Quantidade de registros por página", example = "10")
            @RequestParam int size,
            @Parameter(description = "Filtro por nome ou endereço", required = false)
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok().body(this.unitService.getAllUnits(page, size, search));
    }

    @GetMapping("/{unitId}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @Operation(
            summary = "Buscar unidade por ID",
            description = "Retorna os dados detalhados de uma unidade específica. " +
                    "Requer perfil ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unidade encontrada"),
            @ApiResponse(responseCode = "404", description = "Unidade não encontrada")
    })
    public ResponseEntity<ResponseUnitDto> getById(@PathVariable("unitId") String unitId) {
        return ResponseEntity.ok().body(this.unitService.getById(unitId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @Operation(
            summary = "Criar unidade",
            description = "Cria uma nova unidade no sistema. " +
                    "Requer perfil ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Unidade criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para criar unidades")
    })
    public ResponseEntity<ResponseUnitDto> createUnit(@RequestBody @Valid CreateUnitDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.unitService.createUnit(dto));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @Operation(
            summary = "Atualizar unidade",
            description = "Atualiza os dados de uma unidade existente. " +
                    "O ID da unidade deve ser informado no corpo da requisição. " +
                    "Requer perfil ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unidade atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Unidade não encontrada")
    })
    public ResponseEntity<ResponseUnitDto> updateUnit(@RequestBody @Valid UpdateUnitDto dto) {
        return ResponseEntity.ok().body(this.unitService.updateUnit(dto));
    }

    @DeleteMapping("/{unitId}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @Operation(
            summary = "Remover unidade",
            description = "Remove logicamente uma unidade pelo ID. " +
                    "Requer perfil ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unidade removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Unidade não encontrada")
    })
    public ResponseEntity<ResponseUnitDto> deleteUnit(@PathVariable String unitId) {
        return ResponseEntity.ok().body(this.unitService.deleteUnit(unitId));
    }
}