package br.com.queue.repositories.serviceManagement;

import br.com.queue.dtos.serviceManagement.ResponseServiceManagementDto;
import br.com.queue.dtos.serviceManagement.statistics.*;
import br.com.queue.entities.serviceManagement.ServiceManagement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ServiceManagementRepository extends JpaRepository<ServiceManagement, String> {

    // Importacao de extensao para acentuacoes no sql
    // CREATE EXTENSION IF NOT EXISTS unaccent;

    Optional<ServiceManagement> findByServiceManagementId(String serviceManagementId);

    Set<ServiceManagement> findAllByServiceManagementIdIn(
            Set<String> serviceManagementIds);

    Optional<ServiceManagement> findByName(String name);

    @Modifying
    @Query(value = """
    DELETE FROM tb_user_services
    WHERE service_management_id = :id
    """, nativeQuery = true)
    void deleteUserServicesByServiceId(@Param("id") String id);

    @Query(value = """
        SELECT
            s.service_management_id AS serviceManagementId,
            s.name AS name,
            s.code AS code,
            s.description AS description,
            d.department_id AS departmentId,
            d.name AS departmentName,
            s.active AS active
        FROM tb_service_management s
        INNER JOIN tb_departments d
            ON d.department_id = s.department_id
            AND d.unit_id = :unitId
        WHERE s.unit_id = :unitId
        AND (
            :search IS NULL
            OR :search = ''
            OR unaccent(LOWER(s.name))
                LIKE unaccent(LOWER(CONCAT('%', :search, '%')))
            OR unaccent(LOWER(s.code))
                LIKE unaccent(LOWER(CONCAT('%', :search, '%')))
            OR unaccent(LOWER(d.name))
                LIKE unaccent(LOWER(CONCAT('%', :search, '%')))
        )
        ORDER BY COALESCE(s.updated_at, s.created_at) DESC
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM tb_service_management s
        INNER JOIN tb_departments d
            ON d.department_id = s.department_id
            AND d.unit_id = :unitId
        WHERE s.unit_id = :unitId
        AND (
            :search IS NULL
            OR :search = ''
            OR unaccent(LOWER(s.name))
                LIKE unaccent(LOWER(CONCAT('%', :search, '%')))
            OR unaccent(LOWER(s.code))
                LIKE unaccent(LOWER(CONCAT('%', :search, '%')))
            OR unaccent(LOWER(d.name))
                LIKE unaccent(LOWER(CONCAT('%', :search, '%')))
        )
        """,
            nativeQuery = true
    )
    Page<ResponseServiceManagementDto> findAllWithSearch(
            @Param("unitId") String unitId,
            @Param("search") String search,
            Pageable pageable
    );

    // ============================================================
    // STATISTICS
    // ============================================================

    @Query(value = """
        SELECT
            COUNT(*) AS totalElements,
            COUNT(*) FILTER (WHERE active = true) AS totalElementsActive,
            COUNT(*) FILTER (WHERE active = false) AS totalElementsInactive
        FROM tb_service_management s
        WHERE s.unit_id = :unitId
        """,
            nativeQuery = true)
    ResponseCountTotalServicesStatisticsDto countTotalServicesStatisticsDto(@Param("unitId") String unitId);

    @Query(value = """
        SELECT
            ROUND(
                (
                    COUNT(*) FILTER (WHERE active = true)::numeric
                    / NULLIF(COUNT(*), 0)
                ) * 100,
                2
            ) AS percentageActive,
            ROUND(
                (
                    COUNT(*) FILTER (WHERE active = false)::numeric
                    / NULLIF(COUNT(*), 0)
                ) * 100,
                2
            ) AS percentageInactive
        FROM tb_service_management s
        WHERE s.unit_id = :unitId
        """,
            nativeQuery = true
    )
    ResponseServicePercentagesStatisticsDto getServicePercentagesStatisticsDto(@Param("unitId") String unitId);

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
    
            COALESCE(COUNT(s.service_management_id), 0) AS totalServices
    
        FROM months m
    
        LEFT JOIN tb_service_management s
            ON EXTRACT(MONTH FROM s.created_at) = m.month
            AND EXTRACT(YEAR FROM s.created_at) = EXTRACT(YEAR FROM CURRENT_DATE)
            AND s.unit_id = :unitId
    
        GROUP BY
            m.month
    
        ORDER BY
            m.month
        """,
            nativeQuery = true)
    List<ResponseServicesCreatedByMonthStatisticsDto> countServicesCreatedByMonth(@Param("unitId") String unitId);

    @Query(value = """
        SELECT
            d.name AS departmentName,

            COUNT(s.service_management_id) AS totalServices,

            ROUND(
                (
                    COUNT(s.service_management_id)::numeric
                    /
                    NULLIF(SUM(COUNT(s.service_management_id)) OVER (), 0)
                ) * 100,
                2
            ) AS percentage

        FROM tb_departments d

        LEFT JOIN tb_service_management s
            ON s.department_id = d.department_id
            AND s.unit_id = :unitId

        WHERE d.unit_id = :unitId

        GROUP BY
            d.department_id,
            d.name

        ORDER BY totalServices DESC
        """,
            nativeQuery = true)
    List<ResponseServicesByDepartmentStatisticsDto> countServicesByDepartmentStatistics(@Param("unitId") String unitId);

    @Query(value = """
        SELECT

            s.name AS serviceName,

            COUNT(us.user_id) AS totalUsers,

            ROUND(
                (
                    COUNT(us.user_id)::numeric
                    /
                    NULLIF(SUM(COUNT(us.user_id)) OVER (), 0)
                ) * 100,
                2
            ) AS percentage

        FROM tb_service_management s

        LEFT JOIN tb_user_services us
            ON us.service_management_id = s.service_management_id

        WHERE s.unit_id = :unitId

        GROUP BY

            s.service_management_id,
            s.name

        ORDER BY totalUsers DESC
        """,
            nativeQuery = true)
    List<ResponseUsersByServiceStatisticsDto> countUsersByServiceStatistics(@Param("unitId") String unitId);

    @Query(value = """
        SELECT

            s.name AS serviceName,

            COUNT(sc.schedule_id) AS totalSchedules,

            ROUND(
                (
                    COUNT(sc.schedule_id)::numeric
                    /
                    NULLIF(SUM(COUNT(sc.schedule_id)) OVER (), 0)
                ) * 100,
                2
            ) AS percentage

        FROM tb_service_management s

        LEFT JOIN tb_schedules sc
            ON sc.service_management_id = s.service_management_id

        WHERE s.unit_id = :unitId

        GROUP BY

            s.service_management_id,
            s.name

        ORDER BY totalSchedules DESC
        """,
            nativeQuery = true)
    List<ResponseSchedulesByServiceStatisticsDto> countSchedulesByServiceStatistics(@Param("unitId") String unitId);

    @Query(value = """
        SELECT

            s.name AS serviceName,

            COUNT(t.ticket_id) AS totalTickets,

            ROUND(
                (
                    COUNT(t.ticket_id)::numeric
                    /
                    NULLIF(SUM(COUNT(t.ticket_id)) OVER (), 0)
                ) * 100,
                2
            ) AS percentage

        FROM tb_service_management s

        LEFT JOIN tb_tickets t
            ON t.service_management_id = s.service_management_id

        WHERE s.unit_id = :unitId

        GROUP BY

            s.service_management_id,
            s.name

        ORDER BY totalTickets DESC
        """,
            nativeQuery = true)
    List<ResponseTicketsByServiceStatisticsDto> countTicketsByServiceStatistics(@Param("unitId") String unitId);
}