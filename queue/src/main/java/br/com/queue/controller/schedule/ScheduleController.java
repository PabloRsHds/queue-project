package br.com.queue.controller.schedule;

import br.com.queue.dtos.schedule.allSchedules.ResponseAllSchedulesDto;
import br.com.queue.dtos.schedule.create.CreateScheduleDto;
import br.com.queue.dtos.schedule.create.ResponseScheduleDto;
import br.com.queue.dtos.schedule.statistics.ResponseScheduleDashBoardDto;
import br.com.queue.dtos.schedule.update.UpdateScheduleDto;
import br.com.queue.service.schedule.SchedulingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/scheduling")
@RequiredArgsConstructor
public class ScheduleController {

    private final SchedulingService schedulingService;

    @PostMapping
    public ResponseEntity<ResponseScheduleDto> createSchedule(
            JwtAuthenticationToken token,
            @RequestBody CreateScheduleDto dto) {

        return ResponseEntity.status(HttpStatus.CREATED).body(this.schedulingService.createSchedule(token, dto));
    }

    @PatchMapping()
    public ResponseEntity<ResponseScheduleDto> updateSchedule(@RequestBody UpdateScheduleDto dto) {

        return ResponseEntity.ok(this.schedulingService.updateSchedule(dto));
    }

    @GetMapping
    public ResponseEntity<Page<ResponseAllSchedulesDto>> getAllSchedules(
            JwtAuthenticationToken token,
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) LocalDate scheduleDate,
            @RequestParam(required = false) String search
    ) {

        return ResponseEntity.ok(this.schedulingService.getAllSchedules(token, page, size, search, scheduleDate));
    }

    @GetMapping("/{scheduleId}")
    public ResponseEntity<ResponseScheduleDto> getScheduleById(@PathVariable String scheduleId) {

        return ResponseEntity.ok(this.schedulingService.getScheduleById(scheduleId));
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<ResponseScheduleDto> deleteSchedule(@PathVariable String scheduleId) {

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(this.schedulingService.deleteSchedule(scheduleId));
    }

    // Estatisticas
    @GetMapping("/statistics")
    public ResponseEntity<ResponseScheduleDashBoardDto> getScheduleStatistics(
            JwtAuthenticationToken token
    ) {

        return ResponseEntity.ok(this.schedulingService.getScheduleStatistics(token));
    }
}