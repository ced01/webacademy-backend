package fr.webskills.academy.repository;

import fr.webskills.academy.domain.AccessCode;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessCodeRepository extends JpaRepository<AccessCode, UUID> {}
