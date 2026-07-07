package com.feebridge.people.repo;

import com.feebridge.people.domain.Guardian;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GuardianRepository extends JpaRepository<Guardian, Long> {

    Optional<Guardian> findByIdAndSchoolId(Long id, Long schoolId);

    Optional<Guardian> findBySchoolIdAndPhone(Long schoolId, String phone);

    Optional<Guardian> findByUserId(Long userId);

    @Query("""
            select g from Guardian g
            where g.schoolId = :schoolId
              and (:q is null or lower(g.firstName) like lower(concat('%', :q, '%'))
                   or lower(g.lastName) like lower(concat('%', :q, '%'))
                   or lower(g.phone) like lower(concat('%', :q, '%'))
                   or lower(g.email) like lower(concat('%', :q, '%')))
            """)
    Page<Guardian> search(@Param("schoolId") Long schoolId, @Param("q") String q, Pageable pageable);
}
