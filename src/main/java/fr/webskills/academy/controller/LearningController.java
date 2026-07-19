package fr.webskills.academy.controller;

import fr.webskills.academy.domain.enums.Level;
import fr.webskills.academy.dto.LearningDtos.*;
import fr.webskills.academy.service.ContentService;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class LearningController {
    private final ContentService content;

    public LearningController(ContentService content) {
        this.content = content;
    }

    @GetMapping("/domains")
    List<LearningDomainResponse> domains() {
        return content.publicDomains();
    }

    @GetMapping("/domains/{slug}")
    LearningDomainResponse domain(@PathVariable String slug) {
        return content.getDomain(slug);
    }

    @GetMapping("/domains/{domainId}/sections")
    List<LearningSectionResponse> sections(@PathVariable UUID domainId) {
        return content.sectionsForDomain(domainId, false);
    }

    @GetMapping("/sections/{slug}")
    LearningSectionResponse section(@PathVariable String slug) {
        return content.getSection(slug);
    }

    @GetMapping("/sections/{sectionId}/lessons")
    List<LessonSummaryResponse> lessons(@PathVariable UUID sectionId) {
        return content.lessonsForSection(sectionId, false);
    }

    @GetMapping("/lessons/{slug}")
    LessonResponse lesson(@PathVariable String slug) {
        return content.getLesson(slug);
    }

    @GetMapping("/search")
    List<LessonSummaryResponse> search(
            @RequestParam(required = false) String q, @RequestParam(required = false) Level level) {
        return content.search(q, level);
    }
}
