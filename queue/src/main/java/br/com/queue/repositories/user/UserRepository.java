package br.com.queue.repositories.user;

import br.com.queue.dtos.user.ResponseUserDto;
import br.com.queue.dtos.user.metrics.*;
import br.com.queue.entities.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUserId(String userId);
    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    boolean existsByCounterNumber(Integer number);

    @Query(value = """
    SELECT
        u.user_id AS userId,
        u.username AS username,
        u.name AS name,
        u.surname AS surname,
        u.phone AS phone,
        u.email AS email,
        u.role::TEXT AS role,
        u.counter_number AS counterNumber,
        u.active AS active,
        u.created_at AS createdAt,
        u.updated_at AS updatedAt
    FROM tb_users u
    WHERE u.unit_id = :unitId
    AND (
        :search IS NULL
        OR :search = ''
        OR unaccent(LOWER(u.username))
            LIKE unaccent(LOWER(CONCAT('%', :search, '%')))
        OR unaccent(LOWER(u.name))
            LIKE unaccent(LOWER(CONCAT('%', :search, '%')))
        OR unaccent(LOWER(u.surname))
            LIKE unaccent(LOWER(CONCAT('%', :search, '%')))
        OR unaccent(LOWER(u.email))
            LIKE unaccent(LOWER(CONCAT('%', :search, '%')))
        OR unaccent(LOWER(u.role::TEXT))
            LIKE unaccent(LOWER(CONCAT('%', :search, '%')))
        OR LOWER(u.active::TEXT)
            LIKE LOWER(CONCAT('%', :search, '%'))
    )
    ORDER BY COALESCE(u.updated_at, u.created_at) DESC
    """,
                countQuery = """
    SELECT COUNT(*)
    FROM tb_users u
    WHERE u.unit_id = :unitId
    AND (
        :search IS NULL
        OR :search = ''
        OR unaccent(LOWER(u.username))
            LIKE unaccent(LOWER(CONCAT('%', :search, '%')))
        OR unaccent(LOWER(u.name))
            LIKE unaccent(LOWER(CONCAT('%', :search, '%')))
        OR unaccent(LOWER(u.surname))
            LIKE unaccent(LOWER(CONCAT('%', :search, '%')))
        OR unaccent(LOWER(u.email))
            LIKE unaccent(LOWER(CONCAT('%', :search, '%')))
        OR unaccent(LOWER(u.role::TEXT))
            LIKE unaccent(LOWER(CONCAT('%', :search, '%')))
        OR LOWER(u.active::TEXT)
            LIKE LOWER(CONCAT('%', :search, '%'))
    )
    """,
                nativeQuery = true
        )
    Page<ResponseUserDto> findAllWithSearch(
            @Param("unitId") String unitId,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("""
        SELECT u FROM User u
        WHERE u.email = :input OR u.username = :input
    """)
    Optional<User> findByEmailOrUsername(@Param("input") String input);

    // ============================================================
    // METRICAS
    // ============================================================

    @Query(value = """
        SELECT
            COUNT(*) AS totalElements,

            COUNT(*) FILTER (
                WHERE active = true
            ) AS totalElementsActive,

            COUNT(*) FILTER (
                WHERE active = false
            ) AS totalElementsInactive,

            COUNT(*) FILTER (
                WHERE role = 'ADMIN'
            ) AS admins

        FROM tb_users u
        WHERE u.unit_id = :unitId
        """,
            nativeQuery = true)
    ResponseCountTotalUsersStatisticsDto countTotalUsersStatisticsDto(@Param("unitId") String unitId);

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
        FROM tb_users u
        WHERE u.unit_id = :unitId
        """,
            nativeQuery = true
    )
    ResponseUserPercentagesStatisticsDto getUserPercentagesStatisticsDto(@Param("unitId") String unitId);

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
    
            COALESCE(COUNT(u.user_id), 0) AS totalUsers
    
        FROM months m
    
        LEFT JOIN tb_users u
            ON EXTRACT(MONTH FROM u.created_at) = m.month
            AND EXTRACT(YEAR FROM u.created_at) = EXTRACT(YEAR FROM CURRENT_DATE)
            AND u.unit_id = :unitId
    
        GROUP BY
            m.month
    
        ORDER BY
            m.month
        """,
            nativeQuery = true)
    List<ResponseUsersCreatedByMonthStatisticsDto> countUsersCreatedByMonth(@Param("unitId") String unitId);

    @Query(value = """
        SELECT

            u.role AS role,

            COUNT(u.user_id) AS totalUsers,

            ROUND(
                (
                    COUNT(u.user_id)::numeric
                    /
                    NULLIF(SUM(COUNT(u.user_id)) OVER (), 0)
                ) * 100,
                2
            ) AS percentage

        FROM tb_users u
        WHERE u.unit_id = :unitId

        GROUP BY
            u.role

        ORDER BY
            totalUsers DESC
        """,
            nativeQuery = true)
    List<ResponseUsersByRoleStatisticsDto> countUsersByRoleStatistics(@Param("unitId") String unitId);

    @Query(value = """
        SELECT

            u.username AS username,

            COUNT(us.service_management_id) AS totalServices

        FROM tb_users u

        LEFT JOIN tb_user_services us
            ON us.user_id = u.user_id

        WHERE u.unit_id = :unitId

        GROUP BY

            u.user_id,
            u.username

        ORDER BY

            totalServices DESC
        """,
            nativeQuery = true)
    List<ResponseServicesByUserStatisticsDto> countServicesByUserStatistics(@Param("unitId") String unitId);
}