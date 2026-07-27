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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAccessCodeService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    private final AccessCodeRepository repo;
    private final AcademyMapper mapper;
    private final PasswordEncoder encoder;

    public AdminAccessCodeService(
            AccessCodeRepository repo, AcademyMapper mapper, PasswordEncoder encoder) {
        this.repo = repo;
        this.mapper = mapper;
        this.encoder = encoder;
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
        String rawCode = generateUniqueRawCode();
        AccessCode c = new AccessCode();
        c.setCode(preview(rawCode));
        c.setCodeHash(encoder.encode(rawCode));
        c.setLabel(r.label() == null || r.label().isBlank() ? "Code classe" : r.label().trim());
        c.setExpiresAt(r.expiresAt());
        c.setMaxUses(r.maxUses());
        c.setActive(true);
        User creator = details == null ? null : details.user();
        c.setCreatedBy(creator);
        AccessCode saved = repo.save(c);
        return mapper.toAccessCodeCreationResponse(saved, rawCode);
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

    private String generateUniqueRawCode() {
        byte[] bytes = new byte[24];
        String code;
        do {
            RANDOM.nextBytes(bytes);
            code = "WSA-" + ENCODER.encodeToString(bytes);
        } while (repo.existsByCode(preview(code)));
        return code;
    }

    private String preview(String rawCode) {
        return rawCode.substring(0, Math.min(12, rawCode.length())) + "…";
    }

    private AccessCode get(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Code d’accès introuvable"));
    }
}
