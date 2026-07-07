package com.feebridge.reporting;

import com.feebridge.auth.security.CurrentUser;
import com.feebridge.billing.domain.InvoiceStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasAnyRole('SCHOOL_ADMIN','BURSAR')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/invoices.csv")
    public ResponseEntity<byte[]> csv(@RequestParam(required = false) Long sessionId,
                                      @RequestParam(required = false) Long termId,
                                      @RequestParam(required = false) Long classId,
                                      @RequestParam(required = false) InvoiceStatus status) {
        byte[] body = reportService.csv(CurrentUser.schoolId(), sessionId, termId, classId, status);
        return download(body, "invoices.csv", "text/csv");
    }

    @GetMapping("/invoices.xlsx")
    public ResponseEntity<byte[]> excel(@RequestParam(required = false) Long sessionId,
                                        @RequestParam(required = false) Long termId,
                                        @RequestParam(required = false) Long classId,
                                        @RequestParam(required = false) InvoiceStatus status) {
        byte[] body = reportService.excel(CurrentUser.schoolId(), sessionId, termId, classId, status);
        return download(body, "invoices.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @GetMapping("/invoices.pdf")
    public ResponseEntity<byte[]> pdf(@RequestParam(required = false) Long sessionId,
                                      @RequestParam(required = false) Long termId,
                                      @RequestParam(required = false) Long classId,
                                      @RequestParam(required = false) InvoiceStatus status) {
        byte[] body = reportService.pdf(CurrentUser.schoolId(), sessionId, termId, classId, status);
        return download(body, "invoices.pdf", MediaType.APPLICATION_PDF_VALUE);
    }

    private ResponseEntity<byte[]> download(byte[] body, String filename, String contentType) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(body);
    }
}
