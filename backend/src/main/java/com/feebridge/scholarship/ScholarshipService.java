package com.feebridge.scholarship;

import com.feebridge.common.exception.BadRequestException;
import com.feebridge.common.exception.NotFoundException;
import com.feebridge.common.money.Money;
import com.feebridge.people.repo.StudentRepository;
import com.feebridge.scholarship.domain.Scholarship;
import com.feebridge.scholarship.domain.ScholarshipStatus;
import com.feebridge.scholarship.domain.ScholarshipType;
import com.feebridge.scholarship.domain.StudentScholarship;
import com.feebridge.scholarship.dto.ScholarshipDtos.AwardScholarshipRequest;
import com.feebridge.scholarship.dto.ScholarshipDtos.CreateScholarshipRequest;
import com.feebridge.scholarship.dto.ScholarshipDtos.ScholarshipDto;
import com.feebridge.scholarship.dto.ScholarshipDtos.StudentScholarshipDto;
import com.feebridge.scholarship.repo.ScholarshipRepository;
import com.feebridge.scholarship.repo.StudentScholarshipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ScholarshipService {

    private final ScholarshipRepository scholarshipRepository;
    private final StudentScholarshipRepository awardRepository;
    private final StudentRepository studentRepository;

    public ScholarshipService(ScholarshipRepository scholarshipRepository,
                              StudentScholarshipRepository awardRepository,
                              StudentRepository studentRepository) {
        this.scholarshipRepository = scholarshipRepository;
        this.awardRepository = awardRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional
    public ScholarshipDto create(Long schoolId, CreateScholarshipRequest req) {
        validateValue(req.type(), req.percentage(), req.amountNaira());
        Scholarship s = new Scholarship();
        s.setSchoolId(schoolId);
        s.setName(req.name());
        s.setType(req.type());
        s.setPercentage(req.percentage());
        s.setAmountKobo(req.amountNaira() == null ? null : Money.ofNaira(req.amountNaira()).kobo());
        s.setSponsor(req.sponsor());
        s.setDescription(req.description());
        return toDto(scholarshipRepository.save(s));
    }

    @Transactional(readOnly = true)
    public List<ScholarshipDto> list(Long schoolId) {
        return scholarshipRepository.findBySchoolIdOrderByNameAsc(schoolId).stream().map(this::toDto).toList();
    }

    @Transactional
    public StudentScholarshipDto award(Long schoolId, Long userId, AwardScholarshipRequest req) {
        studentRepository.findByIdAndSchoolId(req.studentId(), schoolId)
                .orElseThrow(() -> NotFoundException.of("Student", req.studentId()));

        ScholarshipType type = req.type();
        BigDecimal percentage = req.percentage();
        Long amountKobo = req.amountNaira() == null ? null : Money.ofNaira(req.amountNaira()).kobo();
        if (req.scholarshipId() != null) {
            Scholarship template = scholarshipRepository.findByIdAndSchoolId(req.scholarshipId(), schoolId)
                    .orElseThrow(() -> NotFoundException.of("Scholarship", req.scholarshipId()));
            type = template.getType();
            percentage = template.getPercentage();
            amountKobo = template.getAmountKobo();
        }
        if (type == null) {
            throw new BadRequestException("Award requires a scholarshipId or an explicit type");
        }
        validateValue(type, percentage, amountKobo == null ? null : Money.ofKobo(amountKobo).toNaira());

        StudentScholarship award = new StudentScholarship();
        award.setSchoolId(schoolId);
        award.setStudentId(req.studentId());
        award.setScholarshipId(req.scholarshipId());
        award.setSessionId(req.sessionId());
        award.setTermId(req.termId());
        award.setType(type);
        award.setPercentage(percentage);
        award.setAmountKobo(amountKobo);
        award.setAwardedByUserId(userId);
        award.setNote(req.note());
        return toAwardDto(awardRepository.save(award));
    }

    @Transactional
    public void revoke(Long schoolId, Long awardId) {
        StudentScholarship award = awardRepository.findByIdAndSchoolId(awardId, schoolId)
                .orElseThrow(() -> NotFoundException.of("Award", awardId));
        award.setStatus(ScholarshipStatus.REVOKED);
    }

    @Transactional(readOnly = true)
    public List<StudentScholarshipDto> studentAwards(Long schoolId, Long studentId) {
        return awardRepository.findByStudentId(studentId).stream().map(this::toAwardDto).toList();
    }

    /**
     * Total scholarship discount for a student for a given term, capped at the gross fee.
     * Used by the assessment engine when generating invoices.
     */
    @Transactional(readOnly = true)
    public Money discountFor(Long studentId, Long sessionId, Long termId, Money gross) {
        Money total = Money.ZERO;
        for (StudentScholarship a : awardRepository.findByStudentIdAndStatus(studentId, ScholarshipStatus.ACTIVE)) {
            if (a.appliesTo(sessionId, termId)) {
                total = total.plus(discount(a.getType(), a.getPercentage(), a.getAmountKobo(), gross));
            }
        }
        return total.gt(gross) ? gross : total;
    }

    private Money discount(ScholarshipType type, BigDecimal percentage, Long amountKobo, Money gross) {
        if (type == ScholarshipType.PERCENTAGE) {
            return percentage == null ? Money.ZERO : gross.percentage(percentage).min(gross);
        }
        return amountKobo == null ? Money.ZERO : Money.ofKobo(amountKobo).min(gross);
    }

    private void validateValue(ScholarshipType type, BigDecimal percentage, BigDecimal amountNaira) {
        if (type == ScholarshipType.PERCENTAGE) {
            if (percentage == null || percentage.signum() < 0 || percentage.compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new BadRequestException("Percentage scholarships need a percentage between 0 and 100");
            }
        } else if (type == ScholarshipType.FIXED) {
            if (amountNaira == null || amountNaira.signum() < 0) {
                throw new BadRequestException("Fixed scholarships need a non-negative amount");
            }
        }
    }

    private ScholarshipDto toDto(Scholarship s) {
        return new ScholarshipDto(s.getId(), s.getName(), s.getType(), s.getPercentage(),
                s.getAmountKobo() == null ? null : Money.ofKobo(s.getAmountKobo()).toNaira(),
                s.getSponsor(), s.getDescription(), s.isActive());
    }

    private StudentScholarshipDto toAwardDto(StudentScholarship a) {
        return new StudentScholarshipDto(a.getId(), a.getStudentId(), a.getScholarshipId(), a.getType(),
                a.getPercentage(), a.getAmountKobo() == null ? null : Money.ofKobo(a.getAmountKobo()).toNaira(),
                a.getSessionId(), a.getTermId(), a.getStatus(), a.getNote());
    }
}
