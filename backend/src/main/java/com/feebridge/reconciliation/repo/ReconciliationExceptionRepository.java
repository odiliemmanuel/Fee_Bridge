package com.feebridge.reconciliation.repo;

import com.feebridge.reconciliation.domain.ReconciliationException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReconciliationExceptionRepository extends JpaRepository<ReconciliationException, Long> {

    List<ReconciliationException> findBySchoolIdOrderByCreatedAtDesc(Long schoolId);

    List<ReconciliationException> findByResolvedFalseOrderByCreatedAtDesc();

    boolean existsByTransactionId(Long transactionId);

    Optional<ReconciliationException> findByIdAndSchoolId(Long id, Long schoolId);
}
