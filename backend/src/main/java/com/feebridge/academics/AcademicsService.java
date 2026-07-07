package com.feebridge.academics;

import com.feebridge.academics.domain.AcademicSession;
import com.feebridge.academics.domain.SchoolClass;
import com.feebridge.academics.domain.Term;
import com.feebridge.academics.dto.AcademicsDtos.ClassDto;
import com.feebridge.academics.dto.AcademicsDtos.CreateClassRequest;
import com.feebridge.academics.dto.AcademicsDtos.CreateSessionRequest;
import com.feebridge.academics.dto.AcademicsDtos.SessionDto;
import com.feebridge.academics.dto.AcademicsDtos.TermDto;
import com.feebridge.academics.repo.AcademicSessionRepository;
import com.feebridge.academics.repo.SchoolClassRepository;
import com.feebridge.academics.repo.TermRepository;
import com.feebridge.common.exception.ConflictException;
import com.feebridge.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AcademicsService {

    private static final String[] TERM_NAMES = {"FIRST", "SECOND", "THIRD"};

    private final AcademicSessionRepository sessionRepository;
    private final TermRepository termRepository;
    private final SchoolClassRepository classRepository;

    public AcademicsService(AcademicSessionRepository sessionRepository, TermRepository termRepository,
                            SchoolClassRepository classRepository) {
        this.sessionRepository = sessionRepository;
        this.termRepository = termRepository;
        this.classRepository = classRepository;
    }

    // ---- Sessions & terms -------------------------------------------------

    @Transactional
    public SessionDto createSession(Long schoolId, CreateSessionRequest req) {
        if (sessionRepository.existsBySchoolIdAndName(schoolId, req.name())) {
            throw new ConflictException("Session " + req.name() + " already exists");
        }
        AcademicSession session = new AcademicSession();
        session.setSchoolId(schoolId);
        session.setName(req.name());
        session.setStartDate(req.startDate());
        session.setEndDate(req.endDate());
        if (req.makeCurrent()) {
            clearCurrentSession(schoolId);
            session.setCurrent(true);
        }
        session = sessionRepository.save(session);

        List<Term> terms = new java.util.ArrayList<>();
        for (int i = 0; i < TERM_NAMES.length; i++) {
            Term term = new Term();
            term.setSchoolId(schoolId);
            term.setSessionId(session.getId());
            term.setName(TERM_NAMES[i]);
            term.setSequence(i + 1);
            if (req.makeCurrent() && i == 0) {
                clearCurrentTerm(schoolId);
                term.setCurrent(true);
            }
            terms.add(termRepository.save(term));
        }
        return toSessionDto(session, terms);
    }

    @Transactional(readOnly = true)
    public List<SessionDto> listSessions(Long schoolId) {
        return sessionRepository.findBySchoolIdOrderByNameDesc(schoolId).stream()
                .map(s -> toSessionDto(s, termRepository.findBySessionIdOrderBySequenceAsc(s.getId())))
                .toList();
    }

    @Transactional
    public SessionDto activateSession(Long schoolId, Long sessionId) {
        AcademicSession session = requireSession(schoolId, sessionId);
        clearCurrentSession(schoolId);
        session.setCurrent(true);
        return toSessionDto(session, termRepository.findBySessionIdOrderBySequenceAsc(session.getId()));
    }

    @Transactional
    public TermDto activateTerm(Long schoolId, Long termId) {
        Term term = termRepository.findByIdAndSchoolId(termId, schoolId)
                .orElseThrow(() -> NotFoundException.of("Term", termId));
        clearCurrentTerm(schoolId);
        term.setCurrent(true);
        // Activating a term also makes its session the current session.
        clearCurrentSession(schoolId);
        requireSession(schoolId, term.getSessionId()).setCurrent(true);
        return toTermDto(term);
    }

    // ---- Classes ----------------------------------------------------------

    @Transactional
    public ClassDto createClass(Long schoolId, CreateClassRequest req) {
        if (classRepository.existsBySchoolIdAndName(schoolId, req.name())) {
            throw new ConflictException("Class " + req.name() + " already exists");
        }
        SchoolClass sc = new SchoolClass();
        sc.setSchoolId(schoolId);
        sc.setName(req.name());
        sc.setLevelOrder(req.levelOrder());
        sc = classRepository.save(sc);
        return toClassDto(sc);
    }

    @Transactional(readOnly = true)
    public List<ClassDto> listClasses(Long schoolId) {
        return classRepository.findBySchoolIdOrderByLevelOrderAscNameAsc(schoolId).stream()
                .map(this::toClassDto).toList();
    }

    // ---- helpers ----------------------------------------------------------

    private AcademicSession requireSession(Long schoolId, Long sessionId) {
        return sessionRepository.findByIdAndSchoolId(sessionId, schoolId)
                .orElseThrow(() -> NotFoundException.of("Session", sessionId));
    }

    private void clearCurrentSession(Long schoolId) {
        sessionRepository.findBySchoolIdAndCurrentTrue(schoolId).ifPresent(s -> s.setCurrent(false));
    }

    private void clearCurrentTerm(Long schoolId) {
        termRepository.findBySchoolIdAndCurrentTrue(schoolId).ifPresent(t -> t.setCurrent(false));
    }

    private SessionDto toSessionDto(AcademicSession s, List<Term> terms) {
        return new SessionDto(s.getId(), s.getName(), s.getStartDate(), s.getEndDate(), s.isCurrent(),
                terms.stream().map(this::toTermDto).toList());
    }

    private TermDto toTermDto(Term t) {
        return new TermDto(t.getId(), t.getName(), t.getSequence(), t.getStartDate(), t.getEndDate(), t.isCurrent());
    }

    private ClassDto toClassDto(SchoolClass c) {
        return new ClassDto(c.getId(), c.getName(), c.getLevelOrder());
    }
}
