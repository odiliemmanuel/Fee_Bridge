package com.feebridge.fee.repo;

import com.feebridge.common.domain.ResidencyType;
import com.feebridge.fee.domain.FeeStructure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeeStructureRepository extends JpaRepository<FeeStructure, Long> {

    Optional<FeeStructure> findByClassIdAndTermIdAndResidencyType(Long classId, Long termId, ResidencyType residencyType);

    Optional<FeeStructure> findByIdAndSchoolId(Long id, Long schoolId);

    List<FeeStructure> findBySchoolIdAndSessionId(Long schoolId, Long sessionId);

    List<FeeStructure> findBySchoolId(Long schoolId);

    List<FeeStructure> findByClassIdAndTermId(Long classId, Long termId);
}
