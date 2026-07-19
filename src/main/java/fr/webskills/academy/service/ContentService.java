package fr.webskills.academy.service;

import fr.webskills.academy.domain.*;
import fr.webskills.academy.domain.enums.*;
import fr.webskills.academy.dto.LearningDtos.*;
import fr.webskills.academy.exception.*;
import fr.webskills.academy.mapper.AcademyMapper;
import fr.webskills.academy.repository.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
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

    public LearningDomainResponse getDomain(String slug) {
        return mapper.toDomainResponse(
                domains.findBySlug(slug)
                        .filter(d -> d.getStatus() != PublicationStatus.ARCHIVED)
                        .orElseThrow(() -> new ResourceNotFoundException("Domaine introuvable")));
    }

    @Transactional
    public LearningDomainResponse createDomain(LearningDomainRequest r) {
        if (domains.existsBySlug(r.slug())) throw new DuplicateSlugException("Slug déjà utilisé");
        LearningDomain d = new LearningDomain();
        applyDomain(d, r);
        return mapper.toDomainResponse(domains.save(d));
    }

    @Transactional
    public LearningDomainResponse updateDomain(UUID id, LearningDomainRequest r) {
        LearningDomain d = domain(id);
        applyDomain(d, r);
        return mapper.toDomainResponse(d);
    }

    @Transactional
    public void archiveDomain(UUID id) {
        domain(id).setStatus(PublicationStatus.ARCHIVED);
    }

    private void applyDomain(LearningDomain d, LearningDomainRequest r) {
        d.setName(r.name());
        d.setSlug(r.slug());
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

    public LearningSectionResponse getSection(String slug) {
        return mapper.toSectionResponse(
                sections.findBySlug(slug)
                        .filter(s -> s.getStatus() != PublicationStatus.ARCHIVED)
                        .orElseThrow(() -> new ResourceNotFoundException("Section introuvable")));
    }

    @Transactional
    public LearningSectionResponse createSection(UUID domainId, LearningSectionRequest r) {
        LearningSection s = new LearningSection();
        s.setDomain(domain(domainId));
        applySection(s, r);
        return mapper.toSectionResponse(sections.save(s));
    }

    @Transactional
    public LearningSectionResponse updateSection(UUID id, LearningSectionRequest r) {
        LearningSection s = section(id);
        applySection(s, r);
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
        s.setTitle(r.title());
        s.setSlug(r.slug());
        s.setDescription(r.description());
        s.setDisplayOrder(r.displayOrder());
        s.setStatus(r.status() == null ? PublicationStatus.DRAFT : r.status());
    }

    private LearningSection section(UUID id) {
        return sections.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section introuvable"));
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
                        .filter(x -> x.getStatus() != PublicationStatus.ARCHIVED)
                        .orElseThrow(() -> new ResourceNotFoundException("Leçon introuvable"));
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
    public void archiveLesson(UUID id) {
        lesson(id).setStatus(PublicationStatus.ARCHIVED);
    }

    @Transactional
    public void reorderLessons(List<ReorderItem> items) {
        items.forEach(i -> lesson(i.id()).setDisplayOrder(i.displayOrder()));
    }

    public List<LessonSummaryResponse> search(String q, Level level) {
        String query = q == null ? "" : q;
        return lessons
                .findByStatusAndTitleContainingIgnoreCaseOrStatusAndSummaryContainingIgnoreCase(
                        PublicationStatus.PUBLISHED, query, PublicationStatus.PUBLISHED, query)
                .stream()
                .filter(l -> level == null || l.getLevel() == level)
                .map(mapper::toLessonSummary)
                .toList();
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
        if (reqs != null)
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
