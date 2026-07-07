package com.feebridge.reporting;

import com.feebridge.academics.domain.SchoolClass;
import com.feebridge.academics.repo.SchoolClassRepository;
import com.feebridge.billing.domain.Invoice;
import com.feebridge.billing.domain.InvoiceStatus;
import com.feebridge.billing.repo.InvoiceRepository;
import com.feebridge.common.money.Money;
import com.feebridge.people.domain.Student;
import com.feebridge.people.repo.StudentRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Generates downloadable invoice lists (CSV / Excel / PDF) filtered by class and status. */
@Service
public class ReportService {

    private static final String[] COLUMNS = {
            "Admission No", "Student", "Class", "Residency", "Gross", "Scholarship",
            "Credit", "Net", "Paid", "Balance", "Status"
    };

    private final InvoiceRepository invoiceRepository;
    private final StudentRepository studentRepository;
    private final SchoolClassRepository classRepository;

    public ReportService(InvoiceRepository invoiceRepository, StudentRepository studentRepository,
                         SchoolClassRepository classRepository) {
        this.invoiceRepository = invoiceRepository;
        this.classRepository = classRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional(readOnly = true)
    public byte[] csv(Long schoolId, Long sessionId, Long termId, Long classId, InvoiceStatus status) {
        List<String[]> rows = rows(schoolId, sessionId, termId, classId, status);
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", COLUMNS)).append('\n');
        for (String[] row : rows) {
            sb.append(java.util.Arrays.stream(row).map(this::csvEscape).collect(Collectors.joining(","))).append('\n');
        }
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] excel(Long schoolId, Long sessionId, Long termId, Long classId, InvoiceStatus status) {
        List<String[]> rows = rows(schoolId, sessionId, termId, classId, status);
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Invoices");
            Row header = sheet.createRow(0);
            for (int i = 0; i < COLUMNS.length; i++) {
                header.createCell(i).setCellValue(COLUMNS[i]);
            }
            int r = 1;
            for (String[] row : rows) {
                Row sheetRow = sheet.createRow(r++);
                for (int c = 0; c < row.length; c++) {
                    Cell cell = sheetRow.createCell(c);
                    cell.setCellValue(row[c]);
                }
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to build Excel report", ex);
        }
    }

    @Transactional(readOnly = true)
    public byte[] pdf(Long schoolId, Long sessionId, Long termId, Long classId, InvoiceStatus status) {
        List<String[]> rows = rows(schoolId, sessionId, termId, classId, status);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 24, 24, 36, 24);
            PdfWriter.getInstance(document, out);
            document.open();
            Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            document.add(new Paragraph("FeeBridge — Invoice Report", title));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(COLUMNS.length);
            table.setWidthPercentage(100);
            Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
            for (String col : COLUMNS) {
                PdfPCell cell = new PdfPCell(new Phrase(col, headFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
            for (String[] row : rows) {
                for (String value : row) {
                    table.addCell(new Phrase(value, cellFont));
                }
            }
            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to build PDF report", ex);
        }
    }

    private List<String[]> rows(Long schoolId, Long sessionId, Long termId, Long classId, InvoiceStatus status) {
        List<Invoice> invoices = invoiceRepository.filterList(schoolId, sessionId, termId, classId, status);
        Map<Long, Student> students = studentRepository.findAllById(
                        invoices.stream().map(Invoice::getStudentId).distinct().toList()).stream()
                .collect(Collectors.toMap(Student::getId, Function.identity()));
        Map<Long, String> classes = classRepository.findBySchoolIdOrderByLevelOrderAscNameAsc(schoolId).stream()
                .collect(Collectors.toMap(SchoolClass::getId, SchoolClass::getName));
        return invoices.stream().map(i -> {
            Student s = students.get(i.getStudentId());
            return new String[]{
                    s == null ? "" : s.getAdmissionNo(),
                    s == null ? "" : s.fullName(),
                    classes.getOrDefault(i.getClassId(), ""),
                    i.getResidencyType().name(),
                    naira(i.getGrossAmountKobo()),
                    naira(i.getScholarshipAmountKobo()),
                    naira(i.getCreditAppliedKobo()),
                    naira(i.getNetAmountKobo()),
                    naira(i.getAmountPaidKobo()),
                    naira(i.getBalanceKobo()),
                    i.getStatus().name()
            };
        }).toList();
    }

    private String naira(long kobo) {
        return Money.ofKobo(kobo).toNaira().toPlainString();
    }

    private String csvEscape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }
}
