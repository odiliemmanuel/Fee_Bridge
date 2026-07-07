package com.feebridge.people.repo;

import com.feebridge.people.domain.Student;
import com.feebridge.people.domain.StudentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByIdAndSchoolId(Long id, Long schoolId);

    boolean existsBySchoolIdAndAdmissionNo(Long schoolId, String admissionNo);

    long countBySchoolId(Long schoolId);

    List<Student> findBySchoolIdAndClassId(Long schoolId, Long classId);

    /** Search students by name or admission number, filtered by class and status. */
    @Query("""
            select s from Student s
            where s.schoolId = :schoolId
              and (:q is null or lower(s.firstName) like lower(concat('%', :q, '%'))
                   or lower(s.lastName) like lower(concat('%', :q, '%'))
                   or lower(s.admissionNo) like lower(concat('%', :q, '%')))
              and (:classId is null or s.classId = :classId)
              and (:status is null or s.status = :status)
            """)
    Page<Student> search(@Param("schoolId") Long schoolId,
                         @Param("q") String q,
                         @Param("classId") Long classId,
                         @Param("status") StudentStatus status,
                         Pageable pageable);
}
