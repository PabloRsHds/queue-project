package br.com.queue.dtos.serviceManagement.list_service;

public record ResponseServicesForCreatedUser(
        String serviceManagementId,
        String name,
        String departmentName
) {
}
