package com.feebridge.analytics;

import com.feebridge.analytics.dto.AnalyticsDtos.DashboardDto;
import com.feebridge.auth.security.CurrentUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@PreAuthorize("hasAnyRole('SCHOOL_ADMIN','BURSAR')")
public class AnalyticsController {

    private final AnalyticsService service;

    public AnalyticsController(AnalyticsService service) {
        this.service = service;
    }

    @GetMapping("/dashboard")
    public DashboardDto dashboard(@RequestParam(required = false) Long sessionId,
                                  @RequestParam(required = false) Long termId) {
        return service.dashboard(CurrentUser.schoolId(), sessionId, termId);
    }
}
