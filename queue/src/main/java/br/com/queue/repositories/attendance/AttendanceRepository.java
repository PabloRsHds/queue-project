package br.com.queue.repositories.attendance;

import br.com.queue.dtos.attendance.statistics.*;
import br.com.queue.entities.attendance.Attendance;
import br.com.queue.entities.ticket.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, String> {

    Optional<Attendance> findByAttendanceId(String attendanceId);
    Optional<Attendance> findByTicket(Ticket ticket);

    // ============================================================
    // 1. COUNT TOTAL ATTENDANCES
    // ============================================================

    @Query(value = """
        SELECT
            COUNT(*) FILTER (WHERE t.status = 'WAITING') AS countAttendancesWaiting,
            COUNT(*) FILTER (WHERE t.status = 'IN_PROGRESS') AS countAttendancesInProgress,
            COUNT(*) FILTER (
                WHERE t.status = 'FINISHED'
                AND DATE(a.started_at) = CURRENT_DATE
            ) AS attendancesToday,

            COUNT(*) FILTER (
                WHERE t.status = 'FINISHED'
                AND DATE(a.started_at) >= DATE_TRUNC('week', CURRENT_DATE)
            ) AS attendancesThisWeek,

            COUNT(*) FILTER (
                WHERE t.status = 'FINISHED'
                AND DATE_TRUNC('month', a.started_at) = DATE_TRUNC('month', CURRENT_DATE)
            ) AS attendancesThisMonth,

            COUNT(*) FILTER (
                WHERE t.status = 'FINISHED'
            ) AS totalAttendances

        FROM tb_attendances a
        RIGHT JOIN tb_tickets t ON a.ticket_id = t.ticket_id
        WHERE t.unit_id = :unitId
    """, nativeQuery = true)
    ResponseCountTotalAttendancesStatisticsDto countTotalAttendances(@Param("unitId") String unitId);

    // ============================================================
    // 2. AVERAGE WAITING TIME
    // ============================================================

    @Query(value = """
        SELECT
            COALESCE(
                TO_CHAR(
                    (AVG(a.started_at - t.created_at)),
                    'HH24:MI:SS'
                ),
                '00:00:00'
            ) AS averageWaitingTime
        FROM tb_attendances a
        INNER JOIN tb_tickets t
            ON t.ticket_id = a.ticket_id
        WHERE
            a.started_at IS NOT NULL
            AND t.created_at IS NOT NULL
            AND t.unit_id = :unitId
    """, nativeQuery = true)
    ResponseAverageWaitingTimeStatisticsDto getAverageWaitingTime(@Param("unitId") String unitId);

    // ============================================================
    // 3. AVERAGE SERVICE TIME
    // ============================================================

    @Query(value = """
        SELECT
            COALESCE(
                TO_CHAR(
                    AVG(a.finished_at - a.started_at),
                    'HH24:MI:SS'
                ),
                '00:00:00'
            ) AS averageServiceTime
        FROM tb_attendances a
        INNER JOIN tb_tickets t
            ON t.ticket_id = a.ticket_id
        WHERE
            a.started_at IS NOT NULL
            AND a.finished_at IS NOT NULL
            AND t.unit_id = :unitId
    """, nativeQuery = true)
    ResponseAverageServiceTimeStatisticsDto getAverageServiceTime(@Param("unitId") String unitId);

    // ============================================================
    // 4. AVERAGE ATTENDANCE BY USER
    // ============================================================

    @Query(value = """
        SELECT
            u.username AS username,

            TO_CHAR(
                MAKE_INTERVAL(
                    secs => AVG(EXTRACT(EPOCH FROM (a.finished_at - a.started_at)))::int
                ),
                'MI:SS'
            ) AS averageTime

        FROM tb_attendances a

        INNER JOIN tb_users u
            ON u.user_id = a.user_id

        INNER JOIN tb_tickets t
            ON t.ticket_id = a.ticket_id

        WHERE
            a.started_at IS NOT NULL
            AND a.finished_at IS NOT NULL
            AND t.unit_id = :unitId

        GROUP BY
            u.user_id,
            u.username

        ORDER BY
            AVG(EXTRACT(EPOCH FROM (a.finished_at - a.started_at))) ASC
    """,
            nativeQuery = true)
    List<ResponseAverageAttendanceByUserStatisticsDto> averageAttendanceByUser(@Param("unitId") String unitId);

    // ============================================================
    // 5. ATTENDANCES CREATED BY MONTH
    // ============================================================

    @Query(value = """
        WITH months AS (
           SELECT generate_series(1, 12) AS month
       )

       SELECT
           m.month,

           CASE m.month
               WHEN 1 THEN 'Jan'
               WHEN 2 THEN 'Fev'
               WHEN 3 THEN 'Mar'
               WHEN 4 THEN 'Abr'
               WHEN 5 THEN 'Mai'
               WHEN 6 THEN 'Jun'
               WHEN 7 THEN 'Jul'
               WHEN 8 THEN 'Ago'
               WHEN 9 THEN 'Set'
               WHEN 10 THEN 'Out'
               WHEN 11 THEN 'Nov'
               WHEN 12 THEN 'Dez'
           END AS monthName,

           COALESCE(COUNT(a.attendance_id), 0) AS totalAttendances

       FROM months m

       LEFT JOIN tb_attendances a
           ON EXTRACT(MONTH FROM a.started_at) = m.month
           AND EXTRACT(YEAR FROM a.started_at) = EXTRACT(YEAR FROM CURRENT_DATE)

       LEFT JOIN tb_tickets t
           ON t.ticket_id = a.ticket_id
           AND t.unit_id = :unitId

       GROUP BY
           m.month

       ORDER BY
           m.month
    """,
            nativeQuery = true)
    List<ResponseAttendancesCreatedByMonthStatisticsDto> countAttendancesCreatedByMonth(@Param("unitId") String unitId);

    // ============================================================
    // 6. ATTENDANCES BY WEEK
    // ============================================================

    @Query(value = """
        WITH days AS (
            SELECT generate_series(1, 7) AS day_of_week
        )

        SELECT
            d.day_of_week AS dayOfWeek,

            CASE d.day_of_week
                WHEN 1 THEN 'Seg'
                WHEN 2 THEN 'Ter'
                WHEN 3 THEN 'Qua'
                WHEN 4 THEN 'Qui'
                WHEN 5 THEN 'Sex'
                WHEN 6 THEN 'Sáb'
                WHEN 7 THEN 'Dom'
            END AS dayName,

            COALESCE(COUNT(t.ticket_id), 0) AS totalAttendances

        FROM days d

        LEFT JOIN tb_attendances a
            ON EXTRACT(ISODOW FROM a.started_at) = d.day_of_week
            AND DATE(a.started_at) >= DATE_TRUNC('week', CURRENT_DATE)
            AND DATE(a.started_at) < DATE_TRUNC('week', CURRENT_DATE) + INTERVAL '7 days'

        LEFT JOIN tb_tickets t
            ON t.ticket_id = a.ticket_id
            AND t.unit_id = :unitId

        GROUP BY
            d.day_of_week

        ORDER BY
            d.day_of_week
    """,
            nativeQuery = true)
    List<ResponseAttendancesByWeekStatisticsDto> countAttendancesByWeek(@Param("unitId") String unitId);

    // ============================================================
    // 7. ATTENDANCES BY SERVICE
    // ============================================================

    @Query(value = """
        SELECT
            s.name AS serviceName,

            COUNT(a.attendance_id) AS totalAttendances,

            ROUND(
                (
                    COUNT(a.attendance_id)::numeric
                    /
                    NULLIF(SUM(COUNT(a.attendance_id)) OVER (), 0)
                ) * 100,
                2
            ) AS percentage

        FROM tb_service_management s

        LEFT JOIN tb_tickets t
            ON t.service_management_id = s.service_management_id
            AND t.unit_id = :unitId

        LEFT JOIN tb_attendances a
            ON a.ticket_id = t.ticket_id

        WHERE s.unit_id = :unitId

        GROUP BY
            s.service_management_id,
            s.name

        ORDER BY
            totalAttendances DESC
    """,
            nativeQuery = true)
    List<ResponseAttendancesByServiceStatisticsDto> countAttendancesByService(@Param("unitId") String unitId);

    // ============================================================
    // 8. ATTENDANCES BY HOUR
    // ============================================================

    @Query(value = """
        WITH hours AS (
            SELECT generate_series(0, 23) AS hour
        )

        SELECT
            h.hour,

            COALESCE(COUNT(t.ticket_id), 0) AS totalAttendances

        FROM hours h

        LEFT JOIN tb_tickets t
            ON t.unit_id = :unitId

        LEFT JOIN tb_attendances a
            ON a.ticket_id = t.ticket_id
            AND EXTRACT(HOUR FROM a.started_at) = h.hour

        GROUP BY
            h.hour

        ORDER BY
            h.hour
    """,
            nativeQuery = true)
    List<ResponseAttendancesByHourStatisticsDto> countAttendancesByHour(@Param("unitId") String unitId);

    // ============================================================
    // 9. ATTENDANCES BY DEPARTMENT
    // ============================================================

    @Query(value = """
        SELECT
            d.name AS departmentName,

            COUNT(a.attendance_id) AS totalAttendances,

            ROUND(
                (
                    COUNT(a.attendance_id)::numeric
                    /
                    NULLIF(SUM(COUNT(a.attendance_id)) OVER (), 0)
                ) * 100,
                2
            ) AS percentage

        FROM tb_departments d

        LEFT JOIN tb_service_management s
            ON s.department_id = d.department_id
            AND s.unit_id = :unitId

        LEFT JOIN tb_tickets t
            ON t.service_management_id = s.service_management_id
            AND t.unit_id = :unitId

        LEFT JOIN tb_attendances a
            ON a.ticket_id = t.ticket_id

        WHERE d.unit_id = :unitId

        GROUP BY
            d.department_id,
            d.name

        ORDER BY
            totalAttendances DESC
    """,
            nativeQuery = true)
    List<ResponseAttendancesByDepartmentStatisticsDto> countAttendancesByDepartment(@Param("unitId") String unitId);

    // ============================================================
    // 10. ATTENDANCES BY ATTENDANT
    // ============================================================

    @Query(value = """
        SELECT
            u.username,

            COUNT(t.ticket_id) AS totalAttendances,

            ROUND(
                (
                    COUNT(t.ticket_id)::numeric
                    /
                    NULLIF(SUM(COUNT(t.ticket_id)) OVER (), 0)
                ) * 100,
                2
            ) AS percentage

        FROM tb_users u

        LEFT JOIN tb_attendances a
            ON a.user_id = u.user_id

        LEFT JOIN tb_tickets t
            ON t.ticket_id = a.ticket_id
            AND t.unit_id = :unitId

        WHERE
            u.role = 'ATTENDANT'
            AND u.unit_id = :unitId

        GROUP BY
            u.user_id,
            u.username

        ORDER BY
            totalAttendances DESC
    """,
            nativeQuery = true)
    List<ResponseAttendancesByCustomerStatisticsDto> countAttendancesByCustomer(@Param("unitId") String unitId);
}