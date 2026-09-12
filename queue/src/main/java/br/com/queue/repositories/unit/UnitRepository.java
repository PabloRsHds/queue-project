package br.com.queue.repositories.unit;

import br.com.queue.dtos.department.ResponseDepartmentDto;
import br.com.queue.dtos.unit.ResponseUnitDto;
import br.com.queue.entities.unit.Unit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UnitRepository extends JpaRepository<Unit, String> {

    boolean existsByName(String name);

    @Query(value = """
        SELECT
            u.unit_id AS unitId,
            u.name AS name,
            u.address AS address,
            u.active AS active,
            TO_CHAR(u.created_at, 'DD/MM/YYYY HH24:MI:SS') AS createdAt,
            TO_CHAR(u.updated_at, 'DD/MM/YYYY HH24:MI:SS') AS updatedAt
        FROM tb_units u
        WHERE (
            :search IS NULL
            OR :search = ''
            OR UNACCENT(LOWER(u.name)) LIKE UNACCENT(LOWER(CONCAT('%', :search, '%')))
            OR UNACCENT(LOWER(u.address)) LIKE UNACCENT(LOWER(CONCAT('%', :search, '%')))
        )
        ORDER BY COALESCE(u.updated_at, u.created_at) DESC
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM tb_units u
        WHERE (
            :search IS NULL
            OR :search = ''
            OR UNACCENT(LOWER(u.name)) LIKE UNACCENT(LOWER(CONCAT('%', :search, '%')))
            OR UNACCENT(LOWER(u.address)) LIKE UNACCENT(LOWER(CONCAT('%', :search, '%')))
        )
        """,
            nativeQuery = true
    )
    Page<ResponseUnitDto> findAllWithSearch(
            @Param("search") String search,
            Pageable pageable
    );

    Optional<Unit> findByName(String name);
}
