package fr.webskills.academy.controller;

import fr.webskills.academy.domain.enums.Level;
import fr.webskills.academy.dto.LearningDtos.*;
import fr.webskills.academy.security.AcademyUserDetails;
import fr.webskills.academy.service.ClassPathService;
import fr.webskills.academy.service.ContentService;
import java.util.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class LearningController {
    private final ContentService content;
    private final ClassPathService classPaths;

    public LearningController(ContentService content, ClassPathService classPaths) {
        this.content = content;
        this.classPaths = classPaths;
    }

    @GetMapping({"/domains", "/learning-domains"})
    List<LearningDomainResponse> domains() {
        return content.publicDomains();
    }

    @GetMapping({"/domains/{slug}", "/learning-domains/{slug}"})
    LearningDomainResponse domain(@PathVariable String slug) {
        return content.getDomain(slug);
    }

    @GetMapping("/domains/{domainId}/sections")
    List<LearningSectionResponse> sections(@PathVariable UUID domainId) {
        return content.sectionsForDomain(domainId, false);
    }

    @GetMapping("/learning-domains/{domainSlug}/sections")
    List<LearningSectionResponse> sectionsByDomainSlug(
            @PathVariable String domainSlug,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String q) {
        return content.sectionsForDomainSlug(domainSlug, Level.fromQuery(level), q);
    }

    @GetMapping("/sections/{slug}")
    LearningSectionResponse section(@PathVariable String slug) {
        return content.getSection(slug);
    }

    @GetMapping("/learning-domains/{domainSlug}/sections/{sectionSlug}")
    LearningSectionResponse sectionInDomain(
            @PathVariable String domainSlug, @PathVariable String sectionSlug) {
        return content.getSection(domainSlug, sectionSlug);
    }

    @GetMapping("/sections/{sectionId}/lessons")
    List<LessonSummaryResponse> lessons(@PathVariable UUID sectionId) {
        return content.lessonsForSection(sectionId, false);
    }

    @GetMapping("/lessons/{slug}")
    LessonResponse lesson(@PathVariable String slug) {
        return content.getLesson(slug);
    }

    @GetMapping("/class-path")
    ClassPathResponse classPath(@AuthenticationPrincipal AcademyUserDetails details) {
        if (details == null || details.accessCodeId() == null) {
            throw new fr.webskills.academy.exception.ForbiddenException("Accès classe requis");
        }
        return classPaths.classPathForAccessCode(details.accessCodeId());
    }

    @GetMapping("/search")
    List<LearningSectionResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String domain) {
        return content.searchSections(q, Level.fromQuery(level), domain);
    }
}
