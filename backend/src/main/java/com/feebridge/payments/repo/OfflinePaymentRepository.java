package com.feebridge.payments.repo;

import com.feebridge.payments.domain.OfflinePayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfflinePaymentRepository extends JpaRepository<OfflinePayment, Long> {

    List<OfflinePayment> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    List<OfflinePayment> findBySchoolIdOrderByCreatedAtDesc(Long schoolId);
}
