package com.feebridge.reconciliation.repo;

import com.feebridge.reconciliation.domain.ReconciliationRun;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReconciliationRunRepository extends JpaRepository<ReconciliationRun, Long> {
}
