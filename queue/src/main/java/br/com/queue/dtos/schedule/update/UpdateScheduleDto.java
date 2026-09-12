package br.com.queue.dtos.schedule.update;

import java.time.LocalDateTime;

public record UpdateScheduleDto(
        String scheduleId,
        String customerId,
        String serviceManagementId,
        String priority,
        LocalDateTime scheduledDate,
        String status
) {
}
