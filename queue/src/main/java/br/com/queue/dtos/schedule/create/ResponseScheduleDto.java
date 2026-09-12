package br.com.queue.dtos.schedule.create;

import java.time.LocalDateTime;

public record ResponseScheduleDto(

        String scheduleId,
        String customerId,
        String customerName,
        String serviceManagementId,
        String serviceManagementName,
        String ticketId,
        String ticketCode,
        String priority,
        LocalDateTime scheduledDate,
        String status,
        String createdAt,
        String updateAt
) {
}
