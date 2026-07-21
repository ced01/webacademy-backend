package fr.webskills.academy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import fr.webskills.academy.domain.LearningDomain;
import fr.webskills.academy.domain.LearningSection;
import fr.webskills.academy.domain.Lesson;
import fr.webskills.academy.domain.enums.Level;
import fr.webskills.academy.domain.enums.PublicationStatus;
import fr.webskills.academy.dto.LearningDtos.LessonResponse;
import fr.webskills.academy.dto.LearningDtos.UpdateLessonRequest;
import fr.webskills.academy.mapper.AcademyMapper;
import fr.webskills.academy.repository.LearningDomainRepository;
import fr.webskills.academy.repository.LearningSectionRepository;
import fr.webskills.academy.repository.LessonRepository;
import fr.webskills.academy.repository.LessonResourceRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContentServiceTest {
    @Mock LearningDomainRepository domains;
    @Mock LearningSectionRepository sections;
    @Mock LessonRepository lessons;
    @Mock LessonResourceRepository resources;
    @Mock AcademyMapper mapper;
    @InjectMocks ContentService service;

    @Test
    void update_lesson_saves_the_lesson_and_preserves_existing_slug() {
        UUID lessonId = UUID.randomUUID();
        UUID sectionId = UUID.randomUUID();
        LearningDomain domain = new LearningDomain();
        domain.setId(UUID.randomUUID());
        LearningSection section = new LearningSection();
        section.setId(sectionId);
        section.setDomain(domain);
        Lesson lesson = new Lesson();
        lesson.setId(lessonId);
        lesson.setSection(section);
        lesson.setTitle("Ancien titre");
        lesson.setSlug("slug-stable");

        UpdateLessonRequest request =
                new UpdateLessonRequest(
                        "Nouveau titre",
                        "Résumé",
                        "Contenu",
                        Level.INTERMEDIATE,
                        sectionId,
                        30,
                        3,
                        PublicationStatus.PUBLISHED,
                        List.of());
        LessonResponse response =
                new LessonResponse(
                        lessonId,
                        sectionId,
                        "Nouveau titre",
                        "slug-stable",
                        "Résumé",
                        "Contenu",
                        Level.INTERMEDIATE,
                        30,
                        3,
                        PublicationStatus.PUBLISHED,
                        List.of());

        when(lessons.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(sections.findById(sectionId)).thenReturn(Optional.of(section));
        when(lessons.save(lesson)).thenReturn(lesson);
        when(resources.findByLessonIdOrderByDisplayOrderAsc(lessonId)).thenReturn(List.of());
        when(mapper.toLessonResponse(lesson, List.of())).thenReturn(response);

        LessonResponse result = service.updateLesson(lessonId, request);

        ArgumentCaptor<Lesson> saved = ArgumentCaptor.forClass(Lesson.class);
        verify(lessons).save(saved.capture());
        assertThat(saved.getValue().getSlug()).isEqualTo("slug-stable");
        assertThat(saved.getValue().getTitle()).isEqualTo("Nouveau titre");
        assertThat(result.title()).isEqualTo("Nouveau titre");
    }
}
