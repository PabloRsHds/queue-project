package br.com.queue.controller.attendance;

import br.com.queue.dtos.attendance.allAttendances.ResponseAllAttendances;
import br.com.queue.dtos.attendance.start.StartAttendanceDto;
import br.com.queue.dtos.attendance.start.FinishAttendanceDto;
import br.com.queue.dtos.attendance.finish.ResponseAttendanceDto;
import br.com.queue.dtos.attendance.finish.ResponseFinishAttendanceDto;
import br.com.queue.dtos.attendance.statistics.ResponseAttendanceDashboardDto;
import br.com.queue.service.attendance.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    public ResponseEntity<ResponseAttendanceDto> startAttendance(JwtAuthenticationToken token, @RequestBody StartAttendanceDto dto) {

        return ResponseEntity.status(HttpStatus.CREATED).body(this.attendanceService.startAttendance(token, dto));
    }

    @PatchMapping("/finish")
    public ResponseEntity<ResponseFinishAttendanceDto> finishAttendance(@RequestBody FinishAttendanceDto dto) {

        return ResponseEntity.ok()
                .body(this.attendanceService.finishAttendance(dto));
    }

    @GetMapping
    public ResponseEntity<Page<ResponseAllAttendances>> getAllAttendances(@RequestParam int page, @RequestParam int size) {

        return ResponseEntity.ok()
                .body(this.attendanceService.getAllAttendances(page, size));
    }

    @DeleteMapping("/{attendanceId}")
    public void deleteAttendance(@PathVariable String attendanceId) {
        this.attendanceService.deleteAttendance(attendanceId);
    }

    @GetMapping("/statistics")
    public ResponseEntity<ResponseAttendanceDashboardDto> getAttendanceStatistics(
            JwtAuthenticationToken token
    ) {
        return ResponseEntity.ok()
                .body(this.attendanceService.getAttendanceStatistics(token));
    }
}