package com.feebridge.people.repo;

import com.feebridge.people.domain.StudentGuardian;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentGuardianRepository extends JpaRepository<StudentGuardian, Long> {

    List<StudentGuardian> findByStudentId(Long studentId);

    List<StudentGuardian> findByGuardianId(Long guardianId);

    Optional<StudentGuardian> findByStudentIdAndGuardianId(Long studentId, Long guardianId);

    boolean existsByStudentIdAndGuardianId(Long studentId, Long guardianId);
}
