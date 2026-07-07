package com.feebridge.fee;

import com.feebridge.auth.security.CurrentUser;
import com.feebridge.fee.dto.FeeDtos.FeeChangeDto;
import com.feebridge.fee.dto.FeeDtos.FeeDto;
import com.feebridge.fee.dto.FeeDtos.UpsertFeeRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('SCHOOL_ADMIN','BURSAR')")
public class FeeController {

    private final FeeService feeService;

    public FeeController(FeeService feeService) {
        this.feeService = feeService;
    }

    @PostMapping("/api/fees")
    public FeeDto upsertFee(@Valid @RequestBody UpsertFeeRequest request) {
        return feeService.upsertFee(CurrentUser.schoolId(), CurrentUser.userId(), request);
    }

    @GetMapping("/api/fees")
    public List<FeeDto> listFees(@RequestParam(required = false) Long sessionId) {
        return feeService.listFees(CurrentUser.schoolId(), sessionId);
    }

    @GetMapping("/api/fees/changes")
    public List<FeeChangeDto> feeChanges(@RequestParam(required = false) Long classId,
                                         @RequestParam(required = false) Long termId) {
        return feeService.feeChanges(CurrentUser.schoolId(), classId, termId);
    }
}
