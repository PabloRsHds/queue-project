package br.com.queue.dtos.department.update;

public record UpdateDepartmentDto(
        String departmentId,
        String name,
        String description,
        Boolean active
) {
}
