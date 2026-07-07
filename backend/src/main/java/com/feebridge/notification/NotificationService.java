package com.feebridge.notification;

import com.feebridge.common.money.Money;
import com.feebridge.people.domain.Guardian;
import com.feebridge.people.domain.Student;
import com.feebridge.people.repo.GuardianRepository;
import com.feebridge.people.repo.StudentGuardianRepository;
import com.feebridge.people.repo.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Sends payment notifications to a student's payer guardians. Phase 5 plugs real email (SMTP)
 * and SMS transports into {@link #dispatch}; this base resolves recipients and builds messages.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final StudentRepository studentRepository;
    private final StudentGuardianRepository studentGuardianRepository;
    private final GuardianRepository guardianRepository;
    private final NotificationSender sender;

    public NotificationService(StudentRepository studentRepository,
                               StudentGuardianRepository studentGuardianRepository,
                               GuardianRepository guardianRepository,
                               NotificationSender sender) {
        this.studentRepository = studentRepository;
        this.studentGuardianRepository = studentGuardianRepository;
        this.guardianRepository = guardianRepository;
        this.sender = sender;
    }

    public void paymentReceived(Long schoolId, Long studentId, Money amount, String reference, boolean offline) {
        Student student = studentRepository.findById(studentId).orElse(null);
        if (student == null) {
            return;
        }
        String subject = "Payment received for " + student.fullName();
        String channelNote = offline ? "cash/offline payment" : "online payment";
        String body = String.format(
                "Dear parent/guardian, a %s of NGN %s has been received for %s (ref %s). Thank you, FeeBridge.",
                channelNote, amount.toNaira().toPlainString(), student.fullName(), reference);
        for (Guardian g : payerGuardians(studentId)) {
            if (g.getEmail() != null && !g.getEmail().isBlank()) {
                sender.email(schoolId, g.getEmail(), subject, body);
            }
            if (g.getPhone() != null && !g.getPhone().isBlank()) {
                sender.sms(schoolId, g.getPhone(), body);
            }
        }
        log.info("Payment notification dispatched for student {} ref {}", studentId, reference);
    }

    private List<Guardian> payerGuardians(Long studentId) {
        return studentGuardianRepository.findByStudentId(studentId).stream()
                .filter(sg -> sg.isPayer() || sg.isPrimary())
                .map(sg -> guardianRepository.findById(sg.getGuardianId()).orElse(null))
                .filter(g -> g != null)
                .toList();
    }
}
