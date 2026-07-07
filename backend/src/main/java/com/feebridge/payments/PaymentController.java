package com.feebridge.payments;

import com.feebridge.auth.security.CurrentUser;
import com.feebridge.billing.dto.BillingDtos.InvoiceDto;
import com.feebridge.common.web.PageResponse;
import com.feebridge.payments.dto.PaymentDtos.CreateOrderRequest;
import com.feebridge.payments.dto.PaymentDtos.OrderDto;
import com.feebridge.payments.dto.PaymentDtos.RecordOfflineRequest;
import com.feebridge.payments.dto.PaymentDtos.TransactionDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /** Create a payment order (one or many children) and get the Nomba account to pay to. */
    @PostMapping("/orders")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','BURSAR','PARENT','GUARDIAN')")
    public OrderDto createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return paymentService.createOrder(CurrentUser.schoolId(), CurrentUser.userId(), request);
    }

    @GetMapping("/orders/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','BURSAR','PARENT','GUARDIAN')")
    public OrderDto getOrder(@PathVariable Long id) {
        return paymentService.getOrder(CurrentUser.schoolId(), id);
    }

    @GetMapping("/orders")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','BURSAR')")
    public PageResponse<OrderDto> listOrders(@PageableDefault(size = 20) Pageable pageable) {
        return paymentService.listOrders(CurrentUser.schoolId(), pageable);
    }

    /** Record a cash / bank-transfer / POS payment; parent is alerted by SMS + email. */
    @PostMapping("/offline")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','BURSAR')")
    public InvoiceDto recordOffline(@Valid @RequestBody RecordOfflineRequest request) {
        return paymentService.recordOffline(CurrentUser.schoolId(), CurrentUser.userId(), request);
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','BURSAR')")
    public PageResponse<TransactionDto> listTransactions(@PageableDefault(size = 20) Pageable pageable) {
        return paymentService.listTransactions(CurrentUser.schoolId(), pageable);
    }
}
