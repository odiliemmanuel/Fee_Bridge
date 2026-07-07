package com.feebridge.billing;

import com.feebridge.auth.security.CurrentUser;
import com.feebridge.billing.domain.InvoiceStatus;
import com.feebridge.billing.dto.BillingDtos.AssessmentResultDto;
import com.feebridge.billing.dto.BillingDtos.GenerateInvoicesRequest;
import com.feebridge.billing.dto.BillingDtos.InvoiceDto;
import com.feebridge.billing.dto.BillingDtos.ReconcileResultDto;
import com.feebridge.billing.dto.BillingDtos.StudentStatementDto;
import com.feebridge.common.web.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasAnyRole('SCHOOL_ADMIN','BURSAR')")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @PostMapping("/api/billing/assessments")
    public AssessmentResultDto generate(@Valid @RequestBody GenerateInvoicesRequest request) {
        return billingService.generateInvoices(CurrentUser.schoolId(), request.sessionId(), request.termId(),
                CurrentUser.userId());
    }

    @GetMapping("/api/invoices")
    public PageResponse<InvoiceDto> list(@RequestParam(required = false) Long sessionId,
                                         @RequestParam(required = false) Long termId,
                                         @RequestParam(required = false) Long classId,
                                         @RequestParam(required = false) InvoiceStatus status,
                                         @PageableDefault(size = 20) Pageable pageable) {
        return billingService.listInvoices(CurrentUser.schoolId(), sessionId, termId, classId, status, pageable);
    }

    @GetMapping("/api/invoices/{id}")
    public InvoiceDto get(@PathVariable Long id) {
        return billingService.getInvoice(CurrentUser.schoolId(), id);
    }

    @GetMapping("/api/students/{studentId}/statement")
    public StudentStatementDto statement(@PathVariable Long studentId) {
        return billingService.studentStatement(CurrentUser.schoolId(), studentId);
    }

    @GetMapping("/api/students/{studentId}/reconcile")
    public ReconcileResultDto reconcile(@PathVariable Long studentId) {
        return billingService.reconcileStudent(CurrentUser.schoolId(), studentId);
    }
}
