package com.feebridge.reconciliation;

import com.feebridge.auth.security.CurrentUser;
import com.feebridge.billing.dto.BillingDtos.ReconcileResultDto;
import com.feebridge.reconciliation.dto.ReconciliationDtos.ExceptionDto;
import com.feebridge.reconciliation.dto.ReconciliationDtos.RunResultDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reconciliation")
@PreAuthorize("hasAnyRole('SCHOOL_ADMIN','BURSAR')")
public class ReconciliationController {

    private final ReconciliationService service;

    public ReconciliationController(ReconciliationService service) {
        this.service = service;
    }

    @GetMapping("/exceptions")
    public List<ExceptionDto> exceptions() {
        return service.listExceptions(CurrentUser.schoolId());
    }

    @PostMapping("/run")
    public RunResultDto run() {
        return service.scanUnmatched();
    }

    /** Verify every student's ledger equals their invoice balances (should return empty). */
    @GetMapping("/verify-ledger")
    public List<ReconcileResultDto> verifyLedger() {
        return service.verifyLedger(CurrentUser.schoolId());
    }

    @PostMapping("/exceptions/{id}/resolve")
    public void resolve(@PathVariable Long id) {
        service.resolve(CurrentUser.schoolId(), id);
    }
}
