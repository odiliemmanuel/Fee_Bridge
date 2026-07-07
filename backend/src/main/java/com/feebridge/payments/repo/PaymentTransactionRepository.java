package com.feebridge.payments.repo;

import com.feebridge.payments.domain.PaymentTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    boolean existsByProviderAndProviderReference(String provider, String providerReference);

    Page<PaymentTransaction> findBySchoolIdOrderByCreatedAtDesc(Long schoolId, Pageable pageable);

    List<PaymentTransaction> findByMatchedFalseOrderByCreatedAtDesc();
}
