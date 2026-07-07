package com.feebridge.billing.repo;

import com.feebridge.billing.domain.Invoice;
import com.feebridge.billing.domain.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByStudentIdAndTermId(Long studentId, Long termId);

    Optional<Invoice> findByIdAndSchoolId(Long id, Long schoolId);

    List<Invoice> findByStudentIdOrderByTermIdAsc(Long studentId);

    List<Invoice> findByStudentIdAndSessionId(Long studentId, Long sessionId);

    /** Filter invoices by session, term, class and status (drives the school's list views). */
    @Query("""
            select i from Invoice i
            where i.schoolId = :schoolId
              and (:sessionId is null or i.sessionId = :sessionId)
              and (:termId is null or i.termId = :termId)
              and (:classId is null or i.classId = :classId)
              and (:status is null or i.status = :status)
            """)
    Page<Invoice> filter(@Param("schoolId") Long schoolId,
                         @Param("sessionId") Long sessionId,
                         @Param("termId") Long termId,
                         @Param("classId") Long classId,
                         @Param("status") InvoiceStatus status,
                         Pageable pageable);

    @Query("""
            select i from Invoice i
            where i.schoolId = :schoolId
              and (:sessionId is null or i.sessionId = :sessionId)
              and (:termId is null or i.termId = :termId)
              and (:classId is null or i.classId = :classId)
              and (:status is null or i.status = :status)
            """)
    List<Invoice> filterList(@Param("schoolId") Long schoolId,
                             @Param("sessionId") Long sessionId,
                             @Param("termId") Long termId,
                             @Param("classId") Long classId,
                             @Param("status") InvoiceStatus status);
}
