package com.feebridge.fee;

import com.feebridge.academics.domain.SchoolClass;
import com.feebridge.academics.domain.Term;
import com.feebridge.academics.repo.SchoolClassRepository;
import com.feebridge.academics.repo.TermRepository;
import com.feebridge.common.exception.NotFoundException;
import com.feebridge.common.money.Money;
import com.feebridge.fee.domain.FeeChangeLog;
import com.feebridge.fee.domain.FeeStructure;
import com.feebridge.fee.dto.FeeDtos.FeeChangeDto;
import com.feebridge.fee.dto.FeeDtos.FeeDto;
import com.feebridge.fee.dto.FeeDtos.UpsertFeeRequest;
import com.feebridge.fee.repo.FeeChangeLogRepository;
import com.feebridge.fee.repo.FeeStructureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FeeService {

    private final FeeStructureRepository feeRepository;
    private final FeeChangeLogRepository changeLogRepository;
    private final SchoolClassRepository classRepository;
    private final TermRepository termRepository;

    public FeeService(FeeStructureRepository feeRepository, FeeChangeLogRepository changeLogRepository,
                      SchoolClassRepository classRepository, TermRepository termRepository) {
        this.feeRepository = feeRepository;
        this.changeLogRepository = changeLogRepository;
        this.classRepository = classRepository;
        this.termRepository = termRepository;
    }

    /** Creates or updates the fee for (class, term, residency) and records the change. */
    @Transactional
    public FeeDto upsertFee(Long schoolId, Long userId, UpsertFeeRequest req) {
        SchoolClass schoolClass = classRepository.findByIdAndSchoolId(req.classId(), schoolId)
                .orElseThrow(() -> NotFoundException.of("Class", req.classId()));
        Term term = termRepository.findByIdAndSchoolId(req.termId(), schoolId)
                .orElseThrow(() -> NotFoundException.of("Term", req.termId()));
        Money newAmount = Money.ofNaira(req.amountNaira());

        FeeStructure fee = feeRepository
                .findByClassIdAndTermIdAndResidencyType(req.classId(), req.termId(), req.residencyType())
                .orElse(null);
        Long oldAmountKobo = null;
        if (fee == null) {
            fee = new FeeStructure();
            fee.setSchoolId(schoolId);
            fee.setClassId(req.classId());
            fee.setSessionId(term.getSessionId());
            fee.setTermId(req.termId());
            fee.setResidencyType(req.residencyType());
        } else {
            oldAmountKobo = fee.getAmountKobo();
        }
        fee.setAmountKobo(newAmount.kobo());
        fee.setDescription(req.description());
        fee = feeRepository.save(fee);

        recordChange(schoolId, userId, fee, oldAmountKobo, newAmount.kobo(), req.reason());
        return toFeeDto(fee, schoolClass.getName(), term.getName());
    }

    @Transactional(readOnly = true)
    public List<FeeDto> listFees(Long schoolId, Long sessionId) {
        List<FeeStructure> fees = sessionId != null
                ? feeRepository.findBySchoolIdAndSessionId(schoolId, sessionId)
                : feeRepository.findBySchoolId(schoolId);
        Map<Long, String> classNames = classRepository.findBySchoolIdOrderByLevelOrderAscNameAsc(schoolId).stream()
                .collect(Collectors.toMap(SchoolClass::getId, SchoolClass::getName));
        Map<Long, String> termNames = termNames(fees);
        return fees.stream()
                .map(f -> toFeeDto(f, classNames.get(f.getClassId()), termNames.get(f.getTermId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FeeChangeDto> feeChanges(Long schoolId, Long classId, Long termId) {
        List<FeeChangeLog> logs = (classId != null && termId != null)
                ? changeLogRepository.findBySchoolIdAndClassIdAndTermIdOrderByCreatedAtDesc(schoolId, classId, termId)
                : changeLogRepository.findBySchoolIdOrderByCreatedAtDesc(schoolId);
        return logs.stream().map(this::toChangeDto).toList();
    }

    // ---- helpers ----------------------------------------------------------

    private void recordChange(Long schoolId, Long userId, FeeStructure fee, Long oldKobo, long newKobo, String reason) {
        FeeChangeLog log = new FeeChangeLog();
        log.setSchoolId(schoolId);
        log.setFeeStructureId(fee.getId());
        log.setClassId(fee.getClassId());
        log.setSessionId(fee.getSessionId());
        log.setTermId(fee.getTermId());
        log.setResidencyType(fee.getResidencyType());
        log.setOldAmountKobo(oldKobo);
        log.setNewAmountKobo(newKobo);
        log.setReason(reason);
        log.setChangedByUserId(userId);
        changeLogRepository.save(log);
    }

    private Map<Long, String> termNames(List<FeeStructure> fees) {
        return fees.stream()
                .map(FeeStructure::getTermId)
                .distinct()
                .map(termRepository::findById)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .collect(Collectors.toMap(Term::getId, Term::getName, (a, b) -> a));
    }

    private FeeDto toFeeDto(FeeStructure f, String className, String termName) {
        return new FeeDto(f.getId(), f.getClassId(), className, f.getSessionId(), f.getTermId(), termName,
                f.getResidencyType(), f.getAmount().toNaira(), f.getDescription());
    }

    private FeeChangeDto toChangeDto(FeeChangeLog l) {
        return new FeeChangeDto(l.getId(), l.getClassId(), l.getTermId(), l.getResidencyType(),
                l.getOldAmountKobo() == null ? null : Money.ofKobo(l.getOldAmountKobo()).toNaira(),
                Money.ofKobo(l.getNewAmountKobo()).toNaira(),
                l.getReason(), l.getChangedByUserId(), l.getCreatedAt());
    }
}
