package com.feebridge.fee.repo;

import com.feebridge.fee.domain.FeeChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeeChangeLogRepository extends JpaRepository<FeeChangeLog, Long> {

    List<FeeChangeLog> findBySchoolIdOrderByCreatedAtDesc(Long schoolId);

    List<FeeChangeLog> findBySchoolIdAndClassIdAndTermIdOrderByCreatedAtDesc(Long schoolId, Long classId, Long termId);
}
