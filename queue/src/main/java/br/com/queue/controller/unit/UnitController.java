package br.com.queue.controller.unit;

import br.com.queue.dtos.unit.CreateUnitDto;
import br.com.queue.dtos.unit.ResponseUnitDto;
import br.com.queue.dtos.unit.UpdateUnitDto;
import br.com.queue.service.unit.UnitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/units")
@RequiredArgsConstructor
public class UnitController {

    private final UnitService unitService;

    @GetMapping
    public ResponseEntity<Page<ResponseUnitDto>> getAllUnits(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok().body(this.unitService.getAllUnits(page, size, search));
    }

    @GetMapping("/{unitId}")
    public ResponseEntity<ResponseUnitDto> getById(@PathVariable("unitId") String unitId) {
        return ResponseEntity.ok().body(this.unitService.getById(unitId));
    }

    @PostMapping
    public ResponseEntity<ResponseUnitDto> createUnit(@RequestBody @Valid CreateUnitDto dto) {
        return ResponseEntity.ok().body(this.unitService.createUnit(dto));
    }

    @PutMapping
    public ResponseEntity<ResponseUnitDto> updateUnit(@RequestBody @Valid UpdateUnitDto dto) {
        return ResponseEntity.ok().body(this.unitService.updateUnit(dto));
    }

    @DeleteMapping("/{unitId}")
    public ResponseEntity<ResponseUnitDto> deleteUnit(@PathVariable String unitId) {
        return ResponseEntity.ok().body(this.unitService.deleteUnit(unitId));
    }
}
