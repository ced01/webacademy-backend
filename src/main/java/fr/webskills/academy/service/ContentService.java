package fr.webskills.academy.service;

import fr.webskills.academy.domain.*;
import fr.webskills.academy.domain.enums.*;
import fr.webskills.academy.dto.LearningDtos.*;
import fr.webskills.academy.exception.*;
import fr.webskills.academy.mapper.AcademyMapper;
import fr.webskills.academy.repository.*;
import java.text.Normalizer;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ContentService {
    private final LearningDomainRepository domains;
    private final LearningSectionRepository sections;
    private final LessonRepository lessons;
    private final LessonResourceRepository resources;
    private final AcademyMapper mapper;

    public ContentService(
            LearningDomainRepository domains,
            LearningSectionRepository sections,
            LessonRepository lessons,
            LessonResourceRepository resources,
            AcademyMapper mapper) {
        this.domains = domains;
        this.sections = sections;
        this.lessons = lessons;
        this.resources = resources;
        this.mapper = mapper;
    }

    public List<LearningDomainResponse> publicDomains() {
        return domains.findByStatusOrderByDisplayOrderAsc(PublicationStatus.PUBLISHED).stream()
                .map(mapper::toDomainResponse)
                .toList();
    }

    public List<LearningDomainResponse> adminDomains() {
        return domains.findAllByOrderByDisplayOrderAscNameAsc().stream()
                .map(mapper::toDomainResponse)
                .toList();
    }

    public LearningDomainResponse getDomain(String slug) {
        return mapper.toDomainResponse(
                domains.findBySlug(slug)
                        .filter(d -> d.getStatus() == PublicationStatus.PUBLISHED)
                        .orElseThrow(() -> new ResourceNotFoundException("Domaine introuvable")));
    }

    public LearningDomainResponse getAdminDomain(UUID id) {
        return mapper.toDomainResponse(domain(id));
    }

    @Transactional
    public LearningDomainResponse createDomain(LearningDomainRequest r) {
        LearningDomain d = new LearningDomain();
        applyDomain(d, r);
        if (domains.existsBySlug(d.getSlug())) {
            throw new DuplicateSlugException("Slug déjà utilisé");
        }
        return mapper.toDomainResponse(domains.save(d));
    }

    @Transactional
    public LearningDomainResponse updateDomain(UUID id, LearningDomainRequest r) {
        LearningDomain d = domain(id);
        applyDomain(d, r);
        if (domains.existsBySlugAndIdNot(d.getSlug(), id)) {
            throw new DuplicateSlugException("Slug déjà utilisé");
        }
        return mapper.toDomainResponse(d);
    }

    @Transactional
    public LearningDomainResponse updateDomainStatus(UUID id, PublicationStatus status) {
        LearningDomain d = domain(id);
        d.setStatus(status);
        return mapper.toDomainResponse(d);
    }

    @Transactional
    public void archiveDomain(UUID id) {
        domain(id).setStatus(PublicationStatus.ARCHIVED);
    }

    private void applyDomain(LearningDomain d, LearningDomainRequest r) {
        d.setName(r.name().trim());
        d.setSlug(normalizeSlug(r.slug(), r.name()));
        d.setShortDescription(r.shortDescription());
        d.setDescription(r.description());
        d.setIcon(r.icon());
        d.setCoverImage(r.coverImage());
        d.setDisplayOrder(r.displayOrder());
        d.setStatus(r.status() == null ? PublicationStatus.DRAFT : r.status());
    }

    private LearningDomain domain(UUID id) {
        return domains.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Domaine introuvable"));
    }

    public List<LearningSectionResponse> sectionsForDomain(UUID domainId, boolean admin) {
        var list =
                admin
                        ? sections.findByDomainIdOrderByDisplayOrderAsc(domainId)
                        : sections.findByDomainIdAndStatusOrderByDisplayOrderAsc(
                                domainId, PublicationStatus.PUBLISHED);
        return list.stream().map(mapper::toSectionResponse).toList();
    }

    public List<LearningSectionResponse> sectionsForDomainSlug(String domainSlug) {
        return sectionsForDomainSlug(domainSlug, null, null);
    }

    public List<LearningSectionResponse> sectionsForDomainSlug(
            String domainSlug, Level level, String q) {
        LearningDomain d =
                domains.findBySlug(domainSlug)
                        .filter(domain -> domain.getStatus() == PublicationStatus.PUBLISHED)
                        .orElseThrow(() -> new ResourceNotFoundException("Domaine introuvable"));
        return sections
                .findByDomainIdAndStatusOrderByDisplayOrderAsc(
                        d.getId(), PublicationStatus.PUBLISHED)
                .stream()
                .filter(section -> matchesSection(section, level, q))
                .map(mapper::toSectionResponse)
                .toList();
    }

    public LearningSectionResponse getSection(String slug) {
        LearningSection s =
                sections.findBySlug(slug)
                        .filter(section -> section.getStatus() == PublicationStatus.PUBLISHED)
                        .orElseThrow(() -> new ResourceNotFoundException("Section introuvable"));
        return withNavigation(s, false);
    }

    public LearningSectionResponse getSection(String domainSlug, String sectionSlug) {
        LearningSection s =
                sections.findByDomainSlugAndSlug(domainSlug, sectionSlug)
                        .filter(section -> section.getStatus() == PublicationStatus.PUBLISHED)
                        .orElseThrow(() -> new ResourceNotFoundException("Section introuvable"));
        return withNavigation(s, false);
    }

    @Transactional
    public LearningSectionResponse createSection(UUID domainId, LearningSectionRequest r) {
        LearningSection s = new LearningSection();
        s.setDomain(domain(domainId));
        applySection(s, r);
        if (sections.existsByDomainIdAndSlug(domainId, s.getSlug())) {
            throw new DuplicateSlugException("Slug de section déjà utilisé pour ce domaine");
        }
        return mapper.toSectionResponse(sections.save(s));
    }

    @Transactional
    public LearningSectionResponse updateSection(UUID id, LearningSectionRequest r) {
        LearningSection s = section(id);
        applySection(s, r);
        if (sections.existsByDomainIdAndSlugAndIdNot(s.getDomain().getId(), s.getSlug(), id)) {
            throw new DuplicateSlugException("Slug de section déjà utilisé pour ce domaine");
        }
        return mapper.toSectionResponse(s);
    }

    @Transactional
    public LearningSectionResponse updateSectionStatus(UUID id, PublicationStatus status) {
        LearningSection s = section(id);
        s.setStatus(status);
        return mapper.toSectionResponse(s);
    }

    @Transactional
    public void archiveSection(UUID id) {
        section(id).setStatus(PublicationStatus.ARCHIVED);
    }

    @Transactional
    public void reorderSections(List<ReorderItem> items) {
        items.forEach(i -> section(i.id()).setDisplayOrder(i.displayOrder()));
    }

    private void applySection(LearningSection s, LearningSectionRequest r) {
        s.setTitle(r.title().trim());
        s.setSlug(normalizeSlug(r.slug(), r.title()));
        s.setSummary(r.summary());
        s.setContent(r.content());
        s.setDescription(r.description());
        s.setLevel(r.level());
        s.setDisplayOrder(r.displayOrder());
        s.setVideoUrl(r.videoUrl());
        s.setSourceUrl(r.sourceUrl());
        s.setSourceName(r.sourceName());
        s.setSourceVerifiedAt(r.sourceVerifiedAt());
        s.setStatus(r.status() == null ? PublicationStatus.DRAFT : r.status());
    }

    private LearningSection section(UUID id) {
        return sections.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section introuvable"));
    }

    private LearningSectionResponse withNavigation(LearningSection s, boolean admin) {
        List<LearningSection> ordered =
                admin
                        ? sections.findByDomainIdOrderByDisplayOrderAsc(s.getDomain().getId())
                        : sections.findByDomainIdAndStatusOrderByDisplayOrderAsc(
                                s.getDomain().getId(), PublicationStatus.PUBLISHED);
        int index = ordered.indexOf(s);
        String previous = index > 0 ? ordered.get(index - 1).getSlug() : null;
        String next =
                index >= 0 && index + 1 < ordered.size() ? ordered.get(index + 1).getSlug() : null;
        return mapper.toSectionResponse(s, previous, next);
    }

    public List<LessonSummaryResponse> lessonsForSection(UUID sectionId, boolean admin) {
        var list =
                admin
                        ? lessons.findBySectionIdOrderByDisplayOrderAsc(sectionId)
                        : lessons.findBySectionIdAndStatusOrderByDisplayOrderAsc(
                                sectionId, PublicationStatus.PUBLISHED);
        return list.stream().map(mapper::toLessonSummary).toList();
    }

    public LessonResponse getLesson(String slug) {
        Lesson l =
                lessons.findBySlug(slug)
                        .filter(x -> x.getStatus() == PublicationStatus.PUBLISHED)
                        .orElseThrow(() -> new ResourceNotFoundException("Leçon introuvable"));
        return mapper.toLessonResponse(
                l, resources.findByLessonIdOrderByDisplayOrderAsc(l.getId()));
    }

    public LessonResponse getAdminLesson(UUID id) {
        Lesson l = lesson(id);
        return mapper.toLessonResponse(
                l, resources.findByLessonIdOrderByDisplayOrderAsc(l.getId()));
    }

    @Transactional
    public LessonResponse createLesson(UUID sectionId, LessonRequest r) {
        Lesson l = new Lesson();
        l.setSection(section(sectionId));
        applyLesson(l, r);
        Lesson saved = lessons.save(l);
        replaceResources(saved, r.resources());
        return getLesson(saved.getSlug());
    }

    @Transactional
    public LessonResponse updateLesson(UUID id, LessonRequest r) {
        Lesson l = lesson(id);
        applyLesson(l, r);
        replaceResources(l, r.resources());
        return mapper.toLessonResponse(
                l, resources.findByLessonIdOrderByDisplayOrderAsc(l.getId()));
    }

    @Transactional
    public LessonResponse updateLesson(UUID id, UpdateLessonRequest r) {
        Lesson l = lesson(id);
        LearningSection targetSection = section(r.sectionId());
        l.setSection(targetSection);
        l.setTitle(r.title().trim());
        l.setSummary(r.summary());
        l.setContent(r.content());
        l.setLevel(r.level());
        l.setEstimatedDurationMinutes(r.estimatedDurationMinutes());
        l.setDisplayOrder(r.displayOrder());
        l.setStatus(r.status());
        Lesson saved = lessons.save(l);
        replaceResources(saved, r.resources());
        return mapper.toLessonResponse(
                saved, resources.findByLessonIdOrderByDisplayOrderAsc(saved.getId()));
    }

    @Transactional
    public void archiveLesson(UUID id) {
        lesson(id).setStatus(PublicationStatus.ARCHIVED);
    }

    @Transactional
    public void reorderLessons(List<ReorderItem> items) {
        items.forEach(i -> lesson(i.id()).setDisplayOrder(i.displayOrder()));
    }

    public List<LearningSectionResponse> searchSections(String q, Level level, String domainSlug) {
        return sections.findAll().stream()
                .filter(section -> section.getStatus() == PublicationStatus.PUBLISHED)
                .filter(section -> section.getDomain().getStatus() == PublicationStatus.PUBLISHED)
                .filter(
                        section ->
                                domainSlug == null
                                        || domainSlug.isBlank()
                                        || section.getDomain().getSlug().equals(domainSlug))
                .filter(section -> matchesSection(section, level, q))
                .sorted(
                        Comparator.comparing(
                                        (LearningSection section) ->
                                                section.getDomain().getDisplayOrder())
                                .thenComparing(LearningSection::getDisplayOrder)
                                .thenComparing(LearningSection::getTitle))
                .map(mapper::toSectionResponse)
                .toList();
    }

    private boolean matchesSection(LearningSection section, Level level, String q) {
        if (level != null && section.getLevel() != level) {
            return false;
        }
        if (q == null || q.isBlank()) {
            return true;
        }
        String query = normalizeSearchText(q);
        return normalizeSearchText(section.getTitle()).contains(query)
                || normalizeSearchText(section.getSummary()).contains(query)
                || normalizeSearchText(section.getDescription()).contains(query)
                || normalizeSearchText(section.getContent()).contains(query);
    }

    private String normalizeSearchText(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    private void applyLesson(Lesson l, LessonRequest r) {
        l.setTitle(r.title());
        l.setSlug(r.slug());
        l.setSummary(r.summary());
        l.setContent(r.content());
        l.setLevel(r.level() == null ? Level.BEGINNER : r.level());
        l.setEstimatedDurationMinutes(r.estimatedDurationMinutes());
        l.setDisplayOrder(r.displayOrder());
        l.setStatus(r.status() == null ? PublicationStatus.DRAFT : r.status());
    }

    private Lesson lesson(UUID id) {
        return lessons.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leçon introuvable"));
    }

    private void replaceResources(Lesson lesson, List<LessonResourceRequest> reqs) {
        resources.findByLessonIdOrderByDisplayOrderAsc(lesson.getId()).forEach(resources::delete);
        if (reqs != null) {
            reqs.forEach(
                    r -> {
                        LessonResource lr = new LessonResource();
                        lr.setLesson(lesson);
                        lr.setTitle(r.title());
                        lr.setType(r.type() == null ? ResourceType.LINK : r.type());
                        lr.setUrl(r.url());
                        lr.setDescription(r.description());
                        lr.setDisplayOrder(r.displayOrder());
                        resources.save(lr);
                    });
        }
    }

    private String normalizeSlug(String requested, String fallback) {
        String source = requested == null || requested.isBlank() ? fallback : requested;
        String slug =
                Normalizer.normalize(source, Normalizer.Form.NFD)
                        .replaceAll("\\p{M}", "")
                        .toLowerCase(Locale.ROOT)
                        .replaceAll("[^a-z0-9]+", "-")
                        .replaceAll("(^-|-$)", "");
        if (slug.isBlank()) {
            throw new IllegalArgumentException("Slug invalide");
        }
        return slug;
    }
}
