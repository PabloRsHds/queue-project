package br.com.queue.repositories.schedule;

import br.com.queue.dtos.schedule.allSchedules.ResponseAllSchedulesDto;
import br.com.queue.dtos.schedule.statistics.*;
import br.com.queue.entities.schedule.Schedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, String> {

    Optional<Schedule> findByScheduleId(String scheduleId);

    @Query(value = """
    SELECT
        s.schedule_id AS scheduleId,
        c.customer_id AS customerId,
        c.name AS customerName,
        c.cpf AS customerCpf,
        c.rg AS customerRg,
        c.phone AS customerPhone,
        c.email AS customerEmail,
        sm.service_management_id AS serviceManagementId,
        sm.name AS serviceManagementName,
        s.scheduled_date AS scheduleDate,
        s.status AS status
    FROM tb_schedules s
    INNER JOIN tb_customers c
        ON c.customer_id = s.customer_id
        AND c.unit_id = :unitId
    INNER JOIN tb_service_management sm
        ON sm.service_management_id = s.service_management_id
        AND sm.unit_id = :unitId
    WHERE (
        :search IS NULL
        OR :search = ''
        OR UNACCENT(LOWER(c.name))
            LIKE UNACCENT(LOWER(CONCAT('%', :search, '%')))
        OR UNACCENT(LOWER(c.cpf))
            LIKE UNACCENT(LOWER(CONCAT('%', :search, '%')))
        OR UNACCENT(LOWER(c.rg))
            LIKE UNACCENT(LOWER(CONCAT('%', :search, '%')))
        OR UNACCENT(LOWER(c.phone))
            LIKE UNACCENT(LOWER(CONCAT('%', :search, '%')))
        OR UNACCENT(LOWER(c.email))
            LIKE UNACCENT(LOWER(CONCAT('%', :search, '%')))
        OR UNACCENT(LOWER(sm.name))
            LIKE UNACCENT(LOWER(CONCAT('%', :search, '%')))
    )
    AND (
        CAST(:scheduleDate AS DATE) IS NULL
        OR DATE(s.scheduled_date) = CAST(:scheduleDate AS DATE)
    )
    ORDER BY COALESCE(s.updated_at, s.created_at) DESC
    """,
            countQuery = """
    SELECT COUNT(*)
    FROM tb_schedules s
    INNER JOIN tb_customers c
        ON c.customer_id = s.customer_id
        AND c.unit_id = :unitId
    INNER JOIN tb_service_management sm
        ON sm.service_management_id = s.service_management_id
        AND sm.unit_id = :unitId
    WHERE (
        :search IS NULL
        OR :search = ''
        OR UNACCENT(LOWER(c.name))
            LIKE UNACCENT(LOWER(CONCAT('%', :search, '%')))
        OR UNACCENT(LOWER(c.cpf))
            LIKE UNACCENT(LOWER(CONCAT('%', :search, '%')))
        OR UNACCENT(LOWER(c.rg))
            LIKE UNACCENT(LOWER(CONCAT('%', :search, '%')))
        OR UNACCENT(LOWER(c.phone))
            LIKE UNACCENT(LOWER(CONCAT('%', :search, '%')))
        OR UNACCENT(LOWER(c.email))
            LIKE UNACCENT(LOWER(CONCAT('%', :search, '%')))
        OR UNACCENT(LOWER(sm.name))
            LIKE UNACCENT(LOWER(CONCAT('%', :search, '%')))
    )
    AND (
        CAST(:scheduleDate AS DATE) IS NULL
        OR DATE(s.scheduled_date) = CAST(:scheduleDate AS DATE)
    )
    """,
            nativeQuery = true
    )
    Page<ResponseAllSchedulesDto> findAllWithSearch(
            @Param("unitId") String unitId,
            @Param("search") String search,
            @Param("scheduleDate") LocalDate scheduleDate,
            Pageable pageable
    );

    // ============================================================
    // STATISTICS
    // ============================================================

    @Query(value = """
    SELECT

        COUNT(*) AS totalElements,

        COUNT(*) FILTER (
            WHERE status = 'SCHEDULED'
        ) AS scheduled,

        COUNT(*) FILTER (
            WHERE status = 'PRESENT'
        ) AS present,

        COUNT(*) FILTER (
            WHERE status = 'CANCELED'
        ) AS canceled,

        COUNT(*) FILTER (
            WHERE status = 'ABSENT'
        ) AS absent

    FROM tb_schedules s
    INNER JOIN tb_service_management sm
        ON sm.service_management_id = s.service_management_id
        AND sm.unit_id = :unitId
    """,
            nativeQuery = true)
    ResponseCountTotalSchedulesStatisticsDto countTotalSchedulesStatisticsDto(@Param("unitId") String unitId);

    @Query(value = """
    SELECT

        ROUND(
            COUNT(*) FILTER (WHERE status = 'SCHEDULED')::numeric
            / NULLIF(COUNT(*),0) * 100,
            2
        ) AS scheduled,

        ROUND(
            COUNT(*) FILTER (WHERE status = 'PRESENT')::numeric
            / NULLIF(COUNT(*),0) * 100,
            2
        ) AS present,

        ROUND(
            COUNT(*) FILTER (WHERE status = 'CANCELED')::numeric
            / NULLIF(COUNT(*),0) * 100,
            2
        ) AS canceled,

        ROUND(
            COUNT(*) FILTER (WHERE status = 'ABSENT')::numeric
            / NULLIF(COUNT(*),0) * 100,
            2
        ) AS absent

    FROM tb_schedules s
    INNER JOIN tb_service_management sm
        ON sm.service_management_id = s.service_management_id
        AND sm.unit_id = :unitId
    """,
            nativeQuery = true)
    ResponseSchedulePercentagesStatisticsDto getSchedulePercentagesStatisticsDto(@Param("unitId") String unitId);

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

            COALESCE(
                COUNT(s.schedule_id),
                0
            ) AS totalSchedules

        FROM months m

        LEFT JOIN tb_schedules s
            ON EXTRACT(MONTH FROM s.created_at) = m.month
            AND EXTRACT(YEAR FROM s.created_at) = EXTRACT(YEAR FROM CURRENT_DATE)

        LEFT JOIN tb_service_management sm
            ON sm.service_management_id = s.service_management_id
            AND sm.unit_id = :unitId

        GROUP BY
            m.month

        ORDER BY
            m.month
        """,
            nativeQuery = true)
    List<ResponseSchedulesCreatedByMonthStatisticsDto> countSchedulesCreatedByMonth(@Param("unitId") String unitId);

    @Query(value = """
    WITH week_days AS (

        SELECT generate_series(
            date_trunc('week', CURRENT_DATE)::date,
            (date_trunc('week', CURRENT_DATE) + interval '6 days')::date,
            interval '1 day'
        )::date AS day

    )

    SELECT

        EXTRACT(DAY FROM w.day)::int AS day,

        CASE EXTRACT(ISODOW FROM w.day)
            WHEN 1 THEN 'Seg'
            WHEN 2 THEN 'Ter'
            WHEN 3 THEN 'Qua'
            WHEN 4 THEN 'Qui'
            WHEN 5 THEN 'Sex'
            WHEN 6 THEN 'Sáb'
            WHEN 7 THEN 'Dom'
        END AS weekDay,

        COALESCE(
            COUNT(s.schedule_id),
            0
        ) AS totalSchedules

    FROM week_days w

    LEFT JOIN tb_schedules s
        ON DATE(s.created_at) = w.day

    LEFT JOIN tb_service_management sm
        ON sm.service_management_id = s.service_management_id
        AND sm.unit_id = :unitId

    GROUP BY
        w.day

    ORDER BY
        w.day
    """,
            nativeQuery = true)
    List<ResponseSchedulesCreatedByWeekStatisticsDto> countSchedulesCreatedByWeek(@Param("unitId") String unitId);

    @Query(value = """
        SELECT
            COUNT(*) FILTER (WHERE s.status = 'ABSENT' AND DATE(s.scheduled_date) = CURRENT_DATE) AS absentCustomer,
            COUNT(*) FILTER (WHERE s.status = 'PRESENT' AND DATE(s.scheduled_date) = CURRENT_DATE) AS customerPresent,
            COUNT(*) FILTER (WHERE s.status = 'CANCELED' AND DATE(s.scheduled_date) = CURRENT_DATE) AS schedulingCanceled,
            COUNT(*) FILTER (WHERE DATE(s.scheduled_date) = CURRENT_DATE) AS schedulingOfDay
   
        FROM tb_schedules s
        INNER JOIN tb_service_management sm
            ON sm.service_management_id = s.service_management_id
            AND sm.unit_id = :unitId
        """,
            nativeQuery = true)
    ResponseSchedulesCreatedByDayStatisticsDto countSchedulesCreatedByDay(@Param("unitId") String unitId);

    @Query(value = """
    SELECT

        d.name AS departmentName,

        COUNT(s.schedule_id) AS totalSchedules,

        ROUND(
            (
                COUNT(s.schedule_id)::numeric
                /
                NULLIF(SUM(COUNT(s.schedule_id)) OVER (), 0)
            ) * 100,
            2
        ) AS percentage

    FROM tb_departments d

    LEFT JOIN tb_service_management sm
        ON sm.department_id = d.department_id
        AND sm.unit_id = :unitId

    LEFT JOIN tb_schedules s
        ON s.service_management_id = sm.service_management_id

    WHERE d.unit_id = :unitId

    GROUP BY
        d.department_id,
        d.name

    ORDER BY
        totalSchedules DESC
    """,
            nativeQuery = true)
    List<ResponseSchedulesByDepartmentStatisticsDto> countSchedulesByDepartment(@Param("unitId") String unitId);

    @Query(value = """
    SELECT

        sm.name AS serviceName,

        COUNT(s.schedule_id) AS totalSchedules,

        ROUND(
            (
                COUNT(s.schedule_id)::numeric
                /
                NULLIF(SUM(COUNT(s.schedule_id)) OVER (), 0)
            ) * 100,
            2
        ) AS percentage

    FROM tb_service_management sm

    LEFT JOIN tb_schedules s
        ON s.service_management_id = sm.service_management_id

    WHERE sm.unit_id = :unitId

    GROUP BY
        sm.service_management_id,
        sm.name

    ORDER BY
        totalSchedules DESC
    """,
            nativeQuery = true)
    List<ResponseSchedulesByServiceStatisticsDto> countSchedulesByService(@Param("unitId") String unitId);

    @Query(value = """
    SELECT

        priority,

        COUNT(*) AS totalSchedules,

        ROUND(
            (
                COUNT(*)::numeric
                /
                NULLIF(SUM(COUNT(*)) OVER (), 0)
            ) * 100,
            2
        ) AS percentage

    FROM tb_schedules s
    INNER JOIN tb_service_management sm
        ON sm.service_management_id = s.service_management_id
        AND sm.unit_id = :unitId

    GROUP BY
        priority

    ORDER BY
        totalSchedules DESC
    """,
            nativeQuery = true)
    List<ResponseSchedulesByPriorityStatisticsDto> countSchedulesByPriority(@Param("unitId") String unitId);

    @Query(value = """
    WITH hours AS (
        SELECT generate_series(0, 23) AS hour
    )

    SELECT
        h.hour,
        COALESCE(
            COUNT(s.schedule_id),
            0
        ) AS totalSchedules

    FROM hours h

    LEFT JOIN tb_schedules s
        ON EXTRACT(HOUR FROM s.scheduled_date) = h.hour

    LEFT JOIN tb_service_management sm
        ON sm.service_management_id = s.service_management_id
        AND sm.unit_id = :unitId

    GROUP BY
        h.hour

    ORDER BY
        h.hour
    """,
            nativeQuery = true)
    List<ResponseSchedulesByHourStatisticsDto> countSchedulesByHour(@Param("unitId") String unitId);
}