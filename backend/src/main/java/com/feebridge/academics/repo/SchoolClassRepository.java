package com.feebridge.academics.repo;

import com.feebridge.academics.domain.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {

    List<SchoolClass> findBySchoolIdOrderByLevelOrderAscNameAsc(Long schoolId);

    Optional<SchoolClass> findByIdAndSchoolId(Long id, Long schoolId);

    boolean existsBySchoolIdAndName(Long schoolId, String name);

    long countBySchoolId(Long schoolId);
}
