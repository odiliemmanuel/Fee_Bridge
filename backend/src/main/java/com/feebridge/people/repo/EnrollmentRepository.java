package com.feebridge.people.repo;

import com.feebridge.people.domain.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    Optional<Enrollment> findByStudentIdAndSessionId(Long studentId, Long sessionId);

    List<Enrollment> findByStudentId(Long studentId);

    List<Enrollment> findBySchoolIdAndSessionId(Long schoolId, Long sessionId);

    List<Enrollment> findBySchoolIdAndSessionIdAndClassId(Long schoolId, Long sessionId, Long classId);
}
