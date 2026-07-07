package com.feebridge.academics.repo;

import com.feebridge.academics.domain.AcademicSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AcademicSessionRepository extends JpaRepository<AcademicSession, Long> {

    List<AcademicSession> findBySchoolIdOrderByNameDesc(Long schoolId);

    Optional<AcademicSession> findBySchoolIdAndCurrentTrue(Long schoolId);

    Optional<AcademicSession> findByIdAndSchoolId(Long id, Long schoolId);

    boolean existsBySchoolIdAndName(Long schoolId, String name);
}
