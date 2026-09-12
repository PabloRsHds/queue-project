package br.com.queue.dtos.department;

import java.util.List;

public record ResponseDepartmentDto(
        String departmentId,
        String name,
        String description,
        Boolean active,
        String createdAt,
        String updatedAt,
        List<String> services
) {
}
