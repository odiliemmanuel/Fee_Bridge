package com.feebridge.payments.repo;

import com.feebridge.payments.domain.PaymentOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {

    Optional<PaymentOrder> findByReference(String reference);

    Optional<PaymentOrder> findByIdAndSchoolId(Long id, Long schoolId);

    Page<PaymentOrder> findBySchoolIdOrderByCreatedAtDesc(Long schoolId, Pageable pageable);

    List<PaymentOrder> findByPayerGuardianIdOrderByCreatedAtDesc(Long payerGuardianId);
}
