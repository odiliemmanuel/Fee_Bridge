package com.feebridge.payments.repo;

import com.feebridge.payments.domain.VirtualAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VirtualAccountRepository extends JpaRepository<VirtualAccount, Long> {

    Optional<VirtualAccount> findByAccountRef(String accountRef);

    Optional<VirtualAccount> findByOrderId(Long orderId);
}
