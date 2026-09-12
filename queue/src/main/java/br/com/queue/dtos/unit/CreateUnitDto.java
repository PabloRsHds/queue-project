package br.com.queue.dtos.unit;

import jakarta.validation.constraints.NotBlank;

public record CreateUnitDto(

        @NotBlank(message = "O nome da unidade é obrigatório.")
        String name,
        String address
) {
}
