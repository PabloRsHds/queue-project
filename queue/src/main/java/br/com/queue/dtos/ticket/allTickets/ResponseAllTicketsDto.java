package br.com.queue.dtos.ticket.allTickets;

import java.time.LocalDateTime;

public record ResponseAllTicketsDto(

        String ticketId,
        String code,
        String customerName,
        String serviceManagementName,
        String priority,
        String status,
        LocalDateTime createdAt
) {
}
