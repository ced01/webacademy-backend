package fr.webskills.academy.controller;

import fr.webskills.academy.dto.LearningDtos.*;
import fr.webskills.academy.security.AcademyUserDetails;
import fr.webskills.academy.service.ProgressService;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/progress")
public class ProgressController {
    private final ProgressService service;

    public ProgressController(ProgressService service) {
        this.service = service;
    }

    @GetMapping
    List<ProgressResponse> all(@AuthenticationPrincipal AcademyUserDetails user) {
        return service.all(user.id());
    }

    @GetMapping("/domains/{domainId}")
    Map<String, Object> domain(
            @AuthenticationPrincipal AcademyUserDetails user, @PathVariable UUID domainId) {
        return service.domainProgress(user.id(), domainId);
    }

    @PutMapping("/lessons/{lessonId}")
    ProgressResponse update(
            @AuthenticationPrincipal AcademyUserDetails user,
            @PathVariable UUID lessonId,
            @RequestBody @Valid ProgressRequest r) {
        return service.update(user.id(), lessonId, r);
    }

    @PostMapping("/lessons/{lessonId}/complete")
    ProgressResponse complete(
            @AuthenticationPrincipal AcademyUserDetails user, @PathVariable UUID lessonId) {
        return service.complete(user.id(), lessonId);
    }
}
