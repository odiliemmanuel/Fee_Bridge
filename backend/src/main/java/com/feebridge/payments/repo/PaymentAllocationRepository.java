package com.feebridge.payments.repo;

import com.feebridge.payments.domain.PaymentAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentAllocationRepository extends JpaRepository<PaymentAllocation, Long> {

    List<PaymentAllocation> findByOrderIdOrderByIdAsc(Long orderId);
}
