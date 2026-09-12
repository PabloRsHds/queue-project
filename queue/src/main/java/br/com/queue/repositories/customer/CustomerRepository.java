package br.com.queue.repositories.customer;

import br.com.queue.dtos.customer.allCustomer.ResponseAllCustomersDto;
import br.com.queue.dtos.customer.statistics.ResponseCountTotalCustomersStatisticsDto;
import br.com.queue.dtos.customer.statistics.ResponseCustomersCreatedByMonthStatisticsDto;
import br.com.queue.entities.customer.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, String> {

    Optional<Customer> findByCustomerId(String customerId);

    boolean existsByCpf(String cpf);
    boolean existsByRg(String rg);
    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);

    @Modifying
    void deleteByCustomerId(String customerId);

    @Query(value = """
        SELECT
            c.customer_id AS customerId,
            c.name AS name,
            c.cpf AS cpf,
            c.rg AS rg,
            c.phone AS phone,
            c.email AS email
        FROM tb_customers c
        WHERE c.unit_id = :unitId
        AND (
            :search IS NULL
            OR :search = ''
            OR unaccent(LOWER(c.name))
                LIKE unaccent(LOWER(CONCAT('%', :search, '%')))
            OR unaccent(LOWER(c.cpf))
                LIKE unaccent(LOWER(CONCAT('%', :search, '%')))
            OR unaccent(LOWER(c.rg))
                LIKE unaccent(LOWER(CONCAT('%', :search, '%')))
            OR unaccent(LOWER(c.phone))
                LIKE unaccent(LOWER(CONCAT('%', :search, '%')))
            OR unaccent(LOWER(c.email))
                LIKE unaccent(LOWER(CONCAT('%', :search, '%')))
        )
        ORDER BY COALESCE(c.updated_at, c.created_at) DESC
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM tb_customers c
        WHERE c.unit_id = :unitId
        AND (
            :search IS NULL
            OR :search = ''
            OR unaccent(LOWER(c.name))
                LIKE unaccent(LOWER(CONCAT('%', :search, '%')))
            OR unaccent(LOWER(c.cpf))
                LIKE unaccent(LOWER(CONCAT('%', :search, '%')))
            OR unaccent(LOWER(c.rg))
                LIKE unaccent(LOWER(CONCAT('%', :search, '%')))
            OR unaccent(LOWER(c.phone))
                LIKE unaccent(LOWER(CONCAT('%', :search, '%')))
            OR unaccent(LOWER(c.email))
                LIKE unaccent(LOWER(CONCAT('%', :search, '%')))
        )
        """,
            nativeQuery = true
    )
    Page<ResponseAllCustomersDto> findAllWithSearch(
            @Param("unitId") String unitId,
            @Param("search") String search,
            Pageable pageable
    );

    // ============================================================
    // STATISTICS
    // ============================================================

    @Query(value = """
        SELECT
            COUNT(*) AS totalElements
        FROM tb_customers c
        WHERE c.unit_id = :unitId
        """,
            nativeQuery = true)
    ResponseCountTotalCustomersStatisticsDto countTotalCustomerStatisticsDto(@Param("unitId") String unitId);

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
    
            COALESCE(COUNT(c.customer_id), 0) AS totalCustomers
    
        FROM months m
    
        LEFT JOIN tb_customers c
            ON EXTRACT(MONTH FROM c.created_at) = m.month
            AND EXTRACT(YEAR FROM c.created_at) = EXTRACT(YEAR FROM CURRENT_DATE)
            AND c.unit_id = :unitId
    
        GROUP BY
            m.month
    
        ORDER BY
            m.month
        """,
            nativeQuery = true)
    List<ResponseCustomersCreatedByMonthStatisticsDto> countCustomersCreatedByMonth(@Param("unitId") String unitId);
}