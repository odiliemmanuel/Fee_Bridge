package com.feebridge.billing.repo;

import com.feebridge.billing.domain.StudentCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StudentCreditRepository extends JpaRepository<StudentCredit, Long> {

    Optional<StudentCredit> findByStudentId(Long studentId);

    @Query("select coalesce(sum(c.balanceKobo), 0) from StudentCredit c where c.schoolId = :schoolId")
    long totalCreditForSchool(@Param("schoolId") Long schoolId);
}
