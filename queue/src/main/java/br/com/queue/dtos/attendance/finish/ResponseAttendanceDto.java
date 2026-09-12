package br.com.queue.dtos.attendance.finish;

import java.time.LocalDateTime;

public record ResponseAttendanceDto(
        String ticketId,
        String ticketCode,
        LocalDateTime startedAt) {
}
