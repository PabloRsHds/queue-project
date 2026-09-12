package br.com.queue.dtos.attendance.allAttendances;

import java.time.LocalDateTime;

public record ResponseAllAttendances(
        String ticketId,
        String ticketCode,
        String resolution,
        LocalDateTime startedAt,
        LocalDateTime finishedAt
) {
}
