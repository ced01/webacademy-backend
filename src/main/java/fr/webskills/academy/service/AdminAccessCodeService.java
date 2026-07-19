package fr.webskills.academy.service;

import fr.webskills.academy.domain.AccessCode;
import fr.webskills.academy.dto.LearningDtos.*;
import fr.webskills.academy.exception.ResourceNotFoundException;
import fr.webskills.academy.mapper.AcademyMapper;
import fr.webskills.academy.repository.AccessCodeRepository;
import java.util.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAccessCodeService {
    private final AccessCodeRepository repo;
    private final PasswordEncoder encoder;
    private final AcademyMapper mapper;

    public AdminAccessCodeService(
            AccessCodeRepository repo, PasswordEncoder encoder, AcademyMapper mapper) {
        this.repo = repo;
        this.encoder = encoder;
        this.mapper = mapper;
    }

    public List<AccessCodeAdminResponse> all() {
        return repo.findAll().stream().map(mapper::toAccessCodeResponse).toList();
    }

    @Transactional
    public AccessCodeAdminResponse create(AccessCodeRequest r) {
        AccessCode c = new AccessCode();
        c.setLabel(r.label());
        c.setCodeHash(encoder.encode(r.code()));
        c.setActive(r.active());
        c.setExpiresAt(r.expiresAt());
        c.setMaxUses(r.maxUses());
        return mapper.toAccessCodeResponse(repo.save(c));
    }

    @Transactional
    public AccessCodeAdminResponse update(UUID id, AccessCodeUpdateRequest r) {
        AccessCode c = get(id);
        c.setLabel(r.label());
        if (r.active() != null) c.setActive(r.active());
        c.setExpiresAt(r.expiresAt());
        c.setMaxUses(r.maxUses());
        return mapper.toAccessCodeResponse(c);
    }

    @Transactional
    public AccessCodeAdminResponse status(UUID id, boolean active) {
        AccessCode c = get(id);
        c.setActive(active);
        return mapper.toAccessCodeResponse(c);
    }

    @Transactional
    public void disable(UUID id) {
        get(id).setActive(false);
    }

    private AccessCode get(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Code d’accès introuvable"));
    }
}
