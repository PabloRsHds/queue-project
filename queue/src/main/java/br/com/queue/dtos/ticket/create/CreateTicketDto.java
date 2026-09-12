package br.com.queue.dtos.ticket.create;

public record CreateTicketDto(
        String customerId,
        String serviceManagementId,
        String priority,
        String scheduleId
) {
}
