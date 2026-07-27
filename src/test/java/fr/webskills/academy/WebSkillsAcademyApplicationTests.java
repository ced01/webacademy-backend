package fr.webskills.academy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.webskills.academy.domain.AccessCode;
import fr.webskills.academy.domain.enums.PublicationStatus;
import fr.webskills.academy.repository.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WebSkillsAcademyApplicationTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired AccessCodeRepository accessCodes;
    @Autowired LearningDomainRepository domains;
    @Autowired LearningSectionRepository sections;
    @Autowired LessonRepository lessons;
    @Autowired LessonResourceRepository lessonResources;
    @Autowired UserRepository users;

    @BeforeEach
    void cleanAccessCodes() {
        accessCodes.deleteAll();
    }

    @Test
    void admin_generates_raw_class_code_once_lists_only_metadata_and_revokes_it() throws Exception {
        String token = adminToken();
        String createdBody =
                mvc.perform(
                                post("/api/v1/admin/access-codes")
                                        .header("Authorization", "Bearer " + token)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("{\"label\":\"Classe HTML\"}"))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.code").isNotEmpty())
                        .andExpect(jsonPath("$.codePreview").isNotEmpty())
                        .andExpect(jsonPath("$.active").value(true))
                        .andExpect(jsonPath("$.usageCount").value(0))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        JsonNode created = json.readTree(createdBody);
        String rawCode = created.get("code").asText();
        assertThat(rawCode).startsWith("WSA-");

        AccessCode persisted =
                accessCodes.findById(UUID.fromString(created.get("id").asText())).orElseThrow();
        assertThat(persisted.getCode()).isNotEqualTo(rawCode);
        assertThat(persisted.getCodeHash()).isNotBlank();

        mvc.perform(get("/api/v1/admin/access-codes").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].label").value("Classe HTML"))
                .andExpect(jsonPath("$[0].code").doesNotExist())
                .andExpect(jsonPath("$[0].codePreview").isNotEmpty());

        mvc.perform(
                        patch(
                                        "/api/v1/admin/access-codes/"
                                                + created.get("id").asText()
                                                + "/revoke")
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.revokedAt").isNotEmpty());
    }

    @Test
    void class_code_login_reuses_the_shared_code_without_creating_learner_users() throws Exception {
        String admin = adminToken();
        long usersBefore = userCount();
        String createdBody =
                mvc.perform(
                                post("/api/v1/admin/access-codes")
                                        .header("Authorization", "Bearer " + admin)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("{\"label\":\"Classe CSS\"}"))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        String rawCode = json.readTree(createdBody).get("code").asText();

        for (int i = 0; i < 2; i++) {
            mvc.perform(
                            post("/api/v1/auth/access-code")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"code\":\"" + rawCode + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.role").value("LEARNER"))
                    .andExpect(jsonPath("$.userId").doesNotExist())
                    .andExpect(jsonPath("$.accessCodeLabel").value("Classe CSS"));
        }

        assertThat(userCount()).isEqualTo(usersBefore);
        AccessCode code =
                accessCodes.findAll().stream()
                        .filter(c -> c.getLabel().equals("Classe CSS"))
                        .findFirst()
                        .orElseThrow();
        assertThat(code.getUsageCount()).isEqualTo(2);
        assertThat(code.getLastUsedAt()).isNotNull();
    }

    @Test
    void class_mode_exposes_welcome_recommended_path_access_dates_and_aggregate_dashboard()
            throws Exception {
        String token = adminToken();
        Instant startsAt = Instant.now().minusSeconds(60);
        Instant expiresAt = Instant.now().plusSeconds(3600);
        String createdBody =
                mvc.perform(
                                post("/api/v1/admin/access-codes")
                                        .header("Authorization", "Bearer " + token)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                """
                                {"label":"Promo HTML 2026","welcomeMessage":"Bienvenue la promo HTML","recommendedPath":"Parcours conseillé : HTML puis CSS","startsAt":"%s","expiresAt":"%s"}
                                """
                                                        .formatted(startsAt, expiresAt)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.label").value("Promo HTML 2026"))
                        .andExpect(jsonPath("$.welcomeMessage").value("Bienvenue la promo HTML"))
                        .andExpect(
                                jsonPath("$.recommendedPath")
                                        .value("Parcours conseillé : HTML puis CSS"))
                        .andExpect(jsonPath("$.startsAt").isNotEmpty())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        String rawCode = json.readTree(createdBody).get("code").asText();

        mvc.perform(
                        post("/api/v1/auth/access-code")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"code\":\"" + rawCode + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessCodeLabel").value("Promo HTML 2026"))
                .andExpect(jsonPath("$.welcomeMessage").value("Bienvenue la promo HTML"))
                .andExpect(
                        jsonPath("$.recommendedPath").value("Parcours conseillé : HTML puis CSS"));

        String learnerToken =
                json.readTree(
                                mvc.perform(
                                                post("/api/v1/auth/access-code")
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content("{\"code\":\"" + rawCode + "\"}"))
                                        .andReturn()
                                        .getResponse()
                                        .getContentAsString())
                        .get("accessToken")
                        .asText();
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + learnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessCodeLabel").value("Promo HTML 2026"))
                .andExpect(jsonPath("$.welcomeMessage").value("Bienvenue la promo HTML"))
                .andExpect(
                        jsonPath("$.recommendedPath").value("Parcours conseillé : HTML puis CSS"));

        mvc.perform(get("/api/v1/admin/dashboard").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeClassCodes").value(1))
                .andExpect(jsonPath("$.totalAnonymousClassAccesses").value(2))
                .andExpect(jsonPath("$.classes[0].label").value("Promo HTML 2026"))
                .andExpect(jsonPath("$.classes[0].usageCount").value(2));
    }

    @Test
    void validate_active_code_and_reject_revoked_code() throws Exception {
        String valid = "VALID-" + UUID.randomUUID();
        accessCode(valid, true, null);
        String revoked = "REVOKED-" + UUID.randomUUID();
        accessCode(revoked, false, Instant.now());

        mvc.perform(
                        post("/api/v1/auth/access-code")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"code\":\"" + valid + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("LEARNER"));

        mvc.perform(
                        post("/api/v1/auth/access-code")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"code\":\"" + revoked + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void learner_token_is_required_to_read_course_catalog() throws Exception {
        mvc.perform(get("/api/v1/learning-domains")).andExpect(status().isUnauthorized());

        String token = learnerToken("CATALOG-" + UUID.randomUUID());
        mvc.perform(get("/api/v1/learning-domains").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slug").exists());
    }

    @Test
    void learner_can_read_domains_sections_and_markdown_content() throws Exception {
        String token = learnerToken("READ-" + UUID.randomUUID());
        mvc.perform(get("/api/v1/learning-domains").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.slug == 'html')]").exists());

        mvc.perform(
                        get("/api/v1/learning-domains/html/sections")
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PUBLISHED"))
                .andExpect(
                        jsonPath("$[0].content")
                                .value(
                                        org.hamcrest.Matchers.containsString(
                                                "## Objectifs pédagogiques")));
    }

    @Test
    void public_sections_accept_intermediate_enum_label_and_search_filters() throws Exception {
        String token = learnerToken("INTERMEDIATE-" + UUID.randomUUID());
        mvc.perform(
                        get("/api/v1/learning-domains/html/sections")
                                .header("Authorization", "Bearer " + token)
                                .param("level", "Intermediate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.slug == 'header-nav-main')]").exists())
                .andExpect(jsonPath("$[?(@.level == 'BEGINNER')]").doesNotExist());

        mvc.perform(
                        get("/api/v1/search")
                                .header("Authorization", "Bearer " + token)
                                .param("domain", "html")
                                .param("level", "intermédiaire")
                                .param("q", "landmarks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].domainSlug").value("html"))
                .andExpect(jsonPath("$[0].level").value("INTERMEDIATE"));
    }

    @Test
    void sections_are_sorted_and_slugs_are_unique_inside_domain() {
        var html = domains.findBySlug("html").orElseThrow();
        var htmlSections =
                sections.findByDomainIdAndStatusOrderByDisplayOrderAsc(
                        html.getId(), PublicationStatus.PUBLISHED);
        assertThat(htmlSections)
                .isSortedAccordingTo((a, b) -> a.getDisplayOrder() - b.getDisplayOrder());
        assertThat(htmlSections).hasSizeGreaterThanOrEqualTo(16);
        assertThat(new HashSet<>(htmlSections.stream().map(s -> s.getSlug()).toList()))
                .hasSize(htmlSections.size());
        assertThat(htmlSections.stream().filter(s -> s.getLevel().name().equals("INTERMEDIATE")))
                .hasSizeGreaterThanOrEqualTo(8);
    }

    @Test
    void progress_endpoints_are_removed_and_do_not_store_personal_progress() throws Exception {
        String token = learnerToken("NO-PROGRESS-" + UUID.randomUUID());
        mvc.perform(get("/api/v1/progress").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
        mvc.perform(
                        put("/api/v1/progress/lessons/" + UUID.randomUUID())
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"progressPercentage\":40,\"completed\":false}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void admin_can_create_domain_slug_is_auto_generated_and_duplicate_is_rejected()
            throws Exception {
        String token = adminToken();
        mvc.perform(
                        post("/api/v1/admin/learning-domains")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"name":"Spring Boot Test","slug":"","shortDescription":"Java","description":"Framework Spring","displayOrder":50,"status":"PUBLISHED"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("spring-boot-test"));
        mvc.perform(
                        post("/api/v1/admin/learning-domains")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"name":"Autre","slug":"spring-boot-test","displayOrder":51,"status":"DRAFT"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void admin_can_create_section_with_level_and_url_validation() throws Exception {
        String token = adminToken();
        UUID domainId = domains.findBySlug("html").orElseThrow().getId();
        mvc.perform(
                        post("/api/v1/admin/learning-domains/" + domainId + "/sections")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"title":"Niveau expert","slug":"","summary":"Résumé","content":"Contenu","level":"EXPERT","displayOrder":99,"sourceName":"MDN","sourceUrl":"https://developer.mozilla.org/fr/docs/Web/HTML","videoUrl":"https://www.youtube.com/watch?v=dQw4w9WgXcQ","status":"PUBLISHED"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("niveau-expert"))
                .andExpect(jsonPath("$.levelLabel").value("Expert"));
        mvc.perform(
                        post("/api/v1/admin/learning-domains/" + domainId + "/sections")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"title":"URL KO","level":"BEGINNER","displayOrder":100,"sourceUrl":"ftp://example.test","status":"DRAFT"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void admin_can_update_lesson_and_response_contains_new_values() throws Exception {
        String token = adminToken();
        var lesson = lessons.findAll().get(0);
        var section = lesson.getSection();

        mvc.perform(
                        put("/api/v1/admin/lessons/" + lesson.getId())
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"title":"Leçon modifiée","summary":"Résumé modifié","content":"# Nouveau contenu\\n\\n```html\\n<h1>OK</h1>\\n```","level":"ADVANCED","sectionId":"%s","estimatedDurationMinutes":45,"displayOrder":7,"status":"PUBLISHED","resources":[{"title":"MDN officiel","type":"LINK","url":"https://developer.mozilla.org/fr/docs/Web/HTML","description":"Référence officielle","displayOrder":0}]}
                                """
                                                .formatted(section.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(lesson.getId().toString()))
                .andExpect(jsonPath("$.title").value("Leçon modifiée"))
                .andExpect(jsonPath("$.summary").value("Résumé modifié"))
                .andExpect(
                        jsonPath("$.content")
                                .value(org.hamcrest.Matchers.containsString("```html")))
                .andExpect(jsonPath("$.level").value("ADVANCED"))
                .andExpect(jsonPath("$.sectionId").value(section.getId().toString()))
                .andExpect(jsonPath("$.displayOrder").value(7))
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.resources[0].title").value("MDN officiel"));

        var updated = lessons.findById(lesson.getId()).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("Leçon modifiée");
        assertThat(updated.getSlug()).isEqualTo(lesson.getSlug());
        assertThat(lessonResources.findByLessonIdOrderByDisplayOrderAsc(lesson.getId())).hasSize(1);
    }

    @Test
    void admin_can_update_section_and_response_contains_new_values() throws Exception {
        String token = adminToken();
        var html = domains.findBySlug("html").orElseThrow();
        var section = sections.findByDomainIdOrderByDisplayOrderAsc(html.getId()).get(0);
        String newSlug = "section-admin-update-" + UUID.randomUUID();

        mvc.perform(
                        put("/api/v1/admin/sections/" + section.getId())
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"domainId":"%s","title":"Section modifiée","slug":"%s","summary":"Résumé section","content":"# Nouveau contenu de section","description":"Description section","level":"ADVANCED","displayOrder":12,"videoUrl":"https://www.youtube.com/watch?v=dQw4w9WgXcQ","sourceUrl":"https://developer.mozilla.org/fr/docs/Web/HTML","sourceName":"MDN","status":"PUBLISHED","id":"%s"}
                                """
                                                .formatted(
                                                        html.getId(), newSlug, UUID.randomUUID())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(section.getId().toString()))
                .andExpect(jsonPath("$.domainId").value(html.getId().toString()))
                .andExpect(jsonPath("$.title").value("Section modifiée"))
                .andExpect(jsonPath("$.slug").value(newSlug))
                .andExpect(jsonPath("$.summary").value("Résumé section"))
                .andExpect(jsonPath("$.content").value("# Nouveau contenu de section"))
                .andExpect(jsonPath("$.description").value("Description section"))
                .andExpect(jsonPath("$.level").value("ADVANCED"))
                .andExpect(jsonPath("$.displayOrder").value(12))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        var updated = sections.findById(section.getId()).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("Section modifiée");
        assertThat(updated.getSlug()).isEqualTo(newSlug);
        assertThat(updated.getDomain().getId()).isEqualTo(html.getId());
    }

    @Test
    void update_section_rejects_unknown_missing_invalid_forbidden_and_duplicate_cases()
            throws Exception {
        String admin = adminToken();
        String learner = learnerToken("SECTION-FORBIDDEN-" + UUID.randomUUID());
        var html = domains.findBySlug("html").orElseThrow();
        var orderedSections = sections.findByDomainIdOrderByDisplayOrderAsc(html.getId());
        var target = orderedSections.get(0);
        var other = orderedSections.get(1);

        mvc.perform(
                        put("/api/v1/admin/sections/" + target.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        validSectionUpdateJson(html.getId(), "section-sans-token")))
                .andExpect(status().isUnauthorized());

        mvc.perform(
                        put("/api/v1/admin/sections/" + target.getId())
                                .header("Authorization", "Bearer " + learner)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validSectionUpdateJson(html.getId(), "section-learner")))
                .andExpect(status().isForbidden());

        mvc.perform(
                        put("/api/v1/admin/sections/" + UUID.randomUUID())
                                .header("Authorization", "Bearer " + admin)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        validSectionUpdateJson(
                                                html.getId(), "section-introuvable")))
                .andExpect(status().isNotFound());

        mvc.perform(
                        put("/api/v1/admin/sections/" + target.getId())
                                .header("Authorization", "Bearer " + admin)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"domainId":"%s","title":"   ","slug":"slug invalide","summary":"Résumé","content":"Contenu","description":"Description","level":"BEGINNER","displayOrder":-1,"status":"PUBLISHED"}
                                """
                                                .formatted(html.getId())))
                .andExpect(status().isBadRequest());

        mvc.perform(
                        put("/api/v1/admin/sections/" + target.getId())
                                .header("Authorization", "Bearer " + admin)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        validSectionUpdateJson(
                                                UUID.randomUUID(), "section-domaine-manquant")))
                .andExpect(status().isNotFound());

        mvc.perform(
                        put("/api/v1/admin/sections/" + target.getId())
                                .header("Authorization", "Bearer " + admin)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validSectionUpdateJson(html.getId(), other.getSlug())))
                .andExpect(status().isConflict());
    }

    @Test
    void learner_cannot_update_lesson() throws Exception {
        String token = learnerToken("LESSON-FORBIDDEN-" + UUID.randomUUID());
        UUID lessonId = lessons.findAll().get(0).getId();
        UUID sectionId = sections.findAll().get(0).getId();

        mvc.perform(
                        put("/api/v1/admin/lessons/" + lessonId)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validLessonUpdateJson(sectionId)))
                .andExpect(status().isForbidden());
    }

    @Test
    void update_lesson_returns_404_when_lesson_or_section_does_not_exist() throws Exception {
        String token = adminToken();
        UUID sectionId = sections.findAll().get(0).getId();

        mvc.perform(
                        put("/api/v1/admin/lessons/" + UUID.randomUUID())
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validLessonUpdateJson(sectionId)))
                .andExpect(status().isNotFound());

        UUID lessonId = lessons.findAll().get(0).getId();
        mvc.perform(
                        put("/api/v1/admin/lessons/" + lessonId)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validLessonUpdateJson(UUID.randomUUID())))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_lesson_validates_required_title_content_level_section_and_positive_order()
            throws Exception {
        String token = adminToken();
        UUID lessonId = lessons.findAll().get(0).getId();
        UUID sectionId = sections.findAll().get(0).getId();

        mvc.perform(
                        put("/api/v1/admin/lessons/" + lessonId)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"title":"","summary":"Résumé","content":"Contenu","level":"BEGINNER","sectionId":"%s","displayOrder":0,"status":"PUBLISHED","resources":[]}
                                """
                                                .formatted(sectionId)))
                .andExpect(status().isBadRequest());

        mvc.perform(
                        put("/api/v1/admin/lessons/" + lessonId)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"title":"Titre","summary":"Résumé","content":"","level":"BEGINNER","sectionId":"%s","displayOrder":0,"status":"PUBLISHED","resources":[]}
                                """
                                                .formatted(sectionId)))
                .andExpect(status().isBadRequest());

        mvc.perform(
                        put("/api/v1/admin/lessons/" + lessonId)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"title":"Titre","summary":"Résumé","content":"Contenu","sectionId":"%s","displayOrder":-1,"status":"PUBLISHED","resources":[]}
                                """
                                                .formatted(sectionId)))
                .andExpect(status().isBadRequest());
    }

    private String validLessonUpdateJson(UUID sectionId) {
        return """
                {"title":"Titre","summary":"Résumé","content":"Contenu","level":"BEGINNER","sectionId":"%s","displayOrder":0,"status":"PUBLISHED","resources":[]}
                """
                .formatted(sectionId);
    }

    private String validSectionUpdateJson(UUID domainId, String slug) {
        return """
                {"domainId":"%s","title":"Titre section","slug":"%s","summary":"Résumé","content":"Contenu","description":"Description","level":"BEGINNER","displayOrder":0,"status":"PUBLISHED"}
                """
                .formatted(domainId, slug);
    }

    private AccessCode accessCode(String raw, boolean active, Instant revokedAt) {
        AccessCode code = new AccessCode();
        code.setLabel("Test");
        code.setCode(raw);
        code.setActive(active);
        code.setRevokedAt(revokedAt);
        return accessCodes.save(code);
    }

    private long userCount() {
        return users.count();
    }

    private String adminToken() throws Exception {
        String body =
                mvc.perform(
                                post("/api/v1/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                """
                                {"email":"admin@test.local","password": "Admin123!"}
                                """))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        return json.readTree(body).get("accessToken").asText();
    }

    private String learnerToken(String code) throws Exception {
        accessCode(code, true, null);
        String body =
                mvc.perform(
                                post("/api/v1/auth/access-code")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("{\"code\":\"" + code + "\"}"))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        return json.readTree(body).get("accessToken").asText();
    }
}
