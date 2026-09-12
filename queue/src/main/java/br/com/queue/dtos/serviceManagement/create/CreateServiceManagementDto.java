package br.com.queue.dtos.serviceManagement.create;

public record CreateServiceManagementDto(

        String name,
        String code,
        String description,
        String departmentName
) {
}
