package br.com.queue.dtos.schedule.create;

import java.time.LocalDateTime;

public record CreateScheduleDto(
        String customerId,
        String serviceManagementId,
        String priority,
        LocalDateTime scheduledDate
) {
}
