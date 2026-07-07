package com.feebridge.parent;

import com.feebridge.auth.security.CurrentUser;
import com.feebridge.billing.dto.BillingDtos.StudentStatementDto;
import com.feebridge.parent.dto.ParentDtos.ChildDto;
import com.feebridge.parent.dto.ParentDtos.GuardianProfileDto;
import com.feebridge.payments.dto.PaymentDtos.CreateOrderRequest;
import com.feebridge.payments.dto.PaymentDtos.OrderDto;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/parent")
@PreAuthorize("hasAnyRole('PARENT','GUARDIAN')")
public class ParentController {

    private final ParentService parentService;

    public ParentController(ParentService parentService) {
        this.parentService = parentService;
    }

    @GetMapping("/me")
    public GuardianProfileDto me() {
        return parentService.profile(CurrentUser.schoolId(), CurrentUser.userId());
    }

    @GetMapping("/children")
    public List<ChildDto> children() {
        return parentService.children(CurrentUser.schoolId(), CurrentUser.userId());
    }

    @GetMapping("/children/{studentId}/statement")
    public StudentStatementDto statement(@PathVariable Long studentId) {
        return parentService.statement(CurrentUser.schoolId(), CurrentUser.userId(), studentId);
    }

    @PostMapping("/orders")
    public OrderDto pay(@Valid @RequestBody CreateOrderRequest request) {
        return parentService.pay(CurrentUser.schoolId(), CurrentUser.userId(), request);
    }

    @GetMapping("/orders")
    public List<OrderDto> myOrders() {
        return parentService.myOrders(CurrentUser.schoolId(), CurrentUser.userId());
    }
}
