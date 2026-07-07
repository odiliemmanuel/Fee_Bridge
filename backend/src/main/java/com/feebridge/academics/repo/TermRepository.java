package com.feebridge.academics.repo;

import com.feebridge.academics.domain.Term;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TermRepository extends JpaRepository<Term, Long> {

    List<Term> findBySessionIdOrderBySequenceAsc(Long sessionId);

    Optional<Term> findByIdAndSchoolId(Long id, Long schoolId);

    Optional<Term> findBySchoolIdAndCurrentTrue(Long schoolId);

    Optional<Term> findBySessionIdAndSequence(Long sessionId, int sequence);
}
