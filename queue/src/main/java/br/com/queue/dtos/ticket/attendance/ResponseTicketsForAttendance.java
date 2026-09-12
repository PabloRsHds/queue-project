package br.com.queue.dtos.ticket.attendance;

import java.time.LocalDateTime;

public record ResponseTicketsForAttendance(
        String ticketId,
        String code,
        String status,
        String priority,
        String customerName,
        String serviceManagementName,
        LocalDateTime arrivalTime,
        LocalDateTime startAttendance,
        LocalDateTime finishAttendance,
        String attendanceTime
) {
}
