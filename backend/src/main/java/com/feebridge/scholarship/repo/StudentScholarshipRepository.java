package com.feebridge.scholarship.repo;

import com.feebridge.scholarship.domain.ScholarshipStatus;
import com.feebridge.scholarship.domain.StudentScholarship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentScholarshipRepository extends JpaRepository<StudentScholarship, Long> {

    List<StudentScholarship> findByStudentIdAndStatus(Long studentId, ScholarshipStatus status);

    List<StudentScholarship> findByStudentId(Long studentId);

    Optional<StudentScholarship> findByIdAndSchoolId(Long id, Long schoolId);
}
