package br.com.queue.dtos.unit;

import jakarta.validation.constraints.NotBlank;

public record UpdateUnitDto(

        @NotBlank(message = "O ID da unidade é obrigatório.")
        String unitId,
        String name,
        String address,
        Boolean active
) {
}
