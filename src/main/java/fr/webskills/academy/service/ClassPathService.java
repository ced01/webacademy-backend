package fr.webskills.academy.service;

import fr.webskills.academy.domain.AccessCode;
import fr.webskills.academy.dto.LearningDtos.ClassPathResponse;
import fr.webskills.academy.exception.ResourceNotFoundException;
import fr.webskills.academy.mapper.AcademyMapper;
import fr.webskills.academy.repository.AccessCodeRepository;
import fr.webskills.academy.repository.ClassPathStepRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClassPathService {
    private final AccessCodeRepository accessCodes;
    private final ClassPathStepRepository steps;
    private final AcademyMapper mapper;

    public ClassPathService(
            AccessCodeRepository accessCodes, ClassPathStepRepository steps, AcademyMapper mapper) {
        this.accessCodes = accessCodes;
        this.steps = steps;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public ClassPathResponse classPathForAccessCode(UUID accessCodeId) {
        AccessCode accessCode =
                accessCodes
                        .findById(accessCodeId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Code d’accès introuvable"));
        return new ClassPathResponse(
                accessCode.getId(),
                accessCode.getLabel(),
                accessCode.getWelcomeMessage(),
                accessCode.getRecommendedPath(),
                steps.findByAccessCodeIdOrderByDisplayOrderAsc(accessCodeId).stream()
                        .map(mapper::toClassPathStepResponse)
                        .toList());
    }
}
