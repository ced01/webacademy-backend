package fr.webskills.academy.service;

import fr.webskills.academy.domain.AccessCode;
import fr.webskills.academy.domain.User;
import fr.webskills.academy.dto.LearningDtos.*;
import fr.webskills.academy.exception.ResourceNotFoundException;
import fr.webskills.academy.mapper.AcademyMapper;
import fr.webskills.academy.repository.AccessCodeRepository;
import fr.webskills.academy.security.AcademyUserDetails;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAccessCodeService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    private final AccessCodeRepository repo;
    private final AcademyMapper mapper;

    public AdminAccessCodeService(AccessCodeRepository repo, AcademyMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<AccessCodeAdminResponse> all() {
        return repo.findAll().stream()
                .sorted(Comparator.comparing(AccessCode::getCreatedAt).reversed())
                .map(mapper::toAccessCodeResponse)
                .toList();
    }

    @Transactional
    public AccessCodeAdminResponse create(AccessCodeRequest r, AcademyUserDetails details) {
        AccessCode c = new AccessCode();
        c.setCode(generateUniqueCode());
        c.setLabel(r.label() == null || r.label().isBlank() ? "Code d’accès" : r.label().trim());
        c.setActive(true);
        User creator = details == null ? null : details.user();
        c.setCreatedBy(creator);
        return mapper.toAccessCodeResponse(repo.save(c));
    }

    @Transactional
    public AccessCodeAdminResponse revoke(UUID id) {
        AccessCode c = get(id);
        c.setActive(false);
        c.setRevokedAt(Instant.now());
        return mapper.toAccessCodeResponse(c);
    }

    @Transactional
    public AccessCodeAdminResponse activate(UUID id) {
        AccessCode c = get(id);
        c.setActive(true);
        c.setRevokedAt(null);
        return mapper.toAccessCodeResponse(c);
    }

    private String generateUniqueCode() {
        byte[] bytes = new byte[24];
        String code;
        do {
            RANDOM.nextBytes(bytes);
            code = "WSA-" + ENCODER.encodeToString(bytes);
        } while (repo.existsByCode(code));
        return code;
    }

    private AccessCode get(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Code d’accès introuvable"));
    }
}
