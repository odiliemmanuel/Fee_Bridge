package com.feebridge.billing.repo;

import com.feebridge.billing.domain.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

    List<LedgerEntry> findByStudentIdOrderByIdAsc(Long studentId);

    List<LedgerEntry> findByInvoiceIdOrderByIdAsc(Long invoiceId);

    /** Signed sum of all a student's ledger movements (used to reconcile "verify with balance"). */
    @Query("select coalesce(sum(l.amountKobo), 0) from LedgerEntry l where l.studentId = :studentId")
    long balanceForStudent(@Param("studentId") Long studentId);
}
