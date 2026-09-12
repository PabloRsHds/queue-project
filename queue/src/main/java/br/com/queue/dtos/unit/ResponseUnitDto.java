package br.com.queue.dtos.unit;

public record ResponseUnitDto(

        String unitId,
        String name,
        String address,
        Boolean active,
        String createdAt,
        String updatedAt
) {
}
