package br.com.queue.repositories.ticket;

import br.com.queue.entities.ticket.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, String> {

    Optional<Ticket> findByTicketId(String ticketId);

    Optional<Ticket> findTicketByScheduleScheduleId(String scheduleId);

    @Query(value = """
        SELECT *
        FROM tb_tickets t
        WHERE t.ticket_id = :ticketId
        AND t.unit_id = :unitId
        """,
            nativeQuery = true)
    Optional<Ticket> findByTicketIdAndUnitId(
            @Param("ticketId") String ticketId,
            @Param("unitId") String unitId
    );

    @Query(value = """
        SELECT t.*
        FROM tb_tickets t
        WHERE t.unit_id = :unitId
        AND t.service_management_id IN (
            SELECT us.service_management_id
            FROM tb_user_services us
            WHERE us.user_id = :userId
        )
        AND t.status IN ('WAITING', 'CALLED', 'IN_PROGRESS')
        ORDER BY
            CASE
                WHEN t.priority = 'PRIORITY' THEN 0
                ELSE 1
            END,
            t.created_at ASC
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM tb_tickets t
        WHERE t.unit_id = :unitId
        AND t.service_management_id IN (
            SELECT us.service_management_id
            FROM tb_user_services us
            WHERE us.user_id = :userId
        )
        AND t.status IN ('WAITING', 'CALLED', 'IN_PROGRESS')
        """,
            nativeQuery = true)
    Page<Ticket> getTicketsByAttendant(
            @Param("unitId") String unitId,
            @Param("userId") String userId,
            Pageable pageable
    );

    @Query(value = """
        SELECT t.*
        FROM tb_tickets t
        LEFT JOIN tb_attendances a
            ON a.ticket_id = t.ticket_id
        WHERE t.unit_id = :unitId
        AND t.service_management_id IN (
            SELECT us.service_management_id
            FROM tb_user_services us
            WHERE us.user_id = :userId
        )
        AND (
            (
                t.status = 'FINISHED'
                AND DATE(a.finished_at) = CURRENT_DATE
            )
            OR
            (
                t.status = 'CANCELED'
                AND DATE(t.created_at) = CURRENT_DATE
            )
        )
        ORDER BY
            CASE
                WHEN t.status = 'FINISHED' THEN a.finished_at
                ELSE t.created_at
            END DESC
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM tb_tickets t
        LEFT JOIN tb_attendances a
            ON a.ticket_id = t.ticket_id
        WHERE t.unit_id = :unitId
        AND t.service_management_id IN (
            SELECT us.service_management_id
            FROM tb_user_services us
            WHERE us.user_id = :userId
        )
        AND (
            (
                t.status = 'FINISHED'
                AND DATE(a.finished_at) = CURRENT_DATE
            )
            OR
            (
                t.status = 'CANCELED'
                AND DATE(t.created_at) = CURRENT_DATE
            )
        )
        """,
            nativeQuery = true)
    Page<Ticket> getHistoryTicketsByAttendant(
            @Param("unitId") String unitId,
            @Param("userId") String userId,
            Pageable pageable
    );
}