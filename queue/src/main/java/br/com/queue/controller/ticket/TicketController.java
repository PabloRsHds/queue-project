package br.com.queue.controller.ticket;

import br.com.queue.dtos.ticket.allTickets.ResponseAllTicketsDto;
import br.com.queue.dtos.ticket.attendance.ResponseTicketsForAttendance;
import br.com.queue.dtos.ticket.callTicket.CallTicketDto;
import br.com.queue.dtos.ticket.create.CreateTicketDto;
import br.com.queue.dtos.ticket.ResponseTicketDto;
import br.com.queue.dtos.ticket.finishTicket.FinishTicketDto;
import br.com.queue.service.ticket.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<ResponseTicketDto> createTicket(
            JwtAuthenticationToken token,
            @RequestBody CreateTicketDto dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.ticketService.createTicket(token, dto));
    }

    @PatchMapping("/finish")
    public ResponseEntity<ResponseTicketDto> finishTicket(
            @RequestBody FinishTicketDto dto
    ) {
        return ResponseEntity.ok(this.ticketService.finishTicket(dto));
    }

    @PatchMapping("/call/{ticketId}")
    public ResponseEntity<ResponseTicketDto> callTicket(
            @PathVariable String ticketId
    ) {
        return ResponseEntity.ok(this.ticketService.callTicket(ticketId));
    }

    @PatchMapping("/call/customer/{ticketId}")
    public ResponseEntity<ResponseTicketDto> callCustomer(
            @PathVariable String ticketId
    ) {

        return ResponseEntity.ok(this.ticketService.callCustomer(ticketId));
    }

    @GetMapping
    public ResponseEntity<Page<ResponseAllTicketsDto>> getAllTickets(
            @RequestParam int page,
            @RequestParam int size
    ) {
        return ResponseEntity.ok(this.ticketService.getAllTickets(page, size));
    }

    @GetMapping("/tickets-for-attendance")
    public ResponseEntity<Page<ResponseTicketsForAttendance>> getTicketsForAttendance(
            JwtAuthenticationToken token,
            int page,
            int size) {
        return ResponseEntity.ok(this.ticketService.getTicketsByAttendant(token, page, size));
    }

    @GetMapping("/history")
    public ResponseEntity<Page<ResponseTicketsForAttendance>> getHistoryTicketsByAttendant(
            JwtAuthenticationToken token,
            int page,
            int size) {
        return ResponseEntity.ok(this.ticketService.getHistoryTicketsByAttendant(token, page, size));
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<ResponseTicketDto> getTicketById(
            @PathVariable String ticketId
    ) {
        return ResponseEntity.ok(this.ticketService.getTicketById(ticketId));
    }

    @DeleteMapping("/{ticketId}")
    public ResponseEntity<ResponseTicketDto> deleteTicket(@PathVariable String ticketId) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(this.ticketService.deleteTicket(ticketId));
    }

    @PatchMapping("/status/{ticketId}")
    public ResponseEntity<ResponseTicketDto> cancelTicket(@PathVariable String ticketId) {
        return ResponseEntity.ok(this.ticketService.cancelTicket(ticketId));
    }
}