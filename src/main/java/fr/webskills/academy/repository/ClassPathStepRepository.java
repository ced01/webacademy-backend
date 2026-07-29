package fr.webskills.academy.repository;

import fr.webskills.academy.domain.ClassPathStep;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassPathStepRepository extends JpaRepository<ClassPathStep, UUID> {
    List<ClassPathStep> findByAccessCodeIdOrderByDisplayOrderAsc(UUID accessCodeId);

    void deleteByAccessCodeId(UUID accessCodeId);
}
