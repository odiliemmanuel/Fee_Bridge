package com.feebridge.scholarship.repo;

import com.feebridge.scholarship.domain.Scholarship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScholarshipRepository extends JpaRepository<Scholarship, Long> {

    List<Scholarship> findBySchoolIdOrderByNameAsc(Long schoolId);

    Optional<Scholarship> findByIdAndSchoolId(Long id, Long schoolId);
}
