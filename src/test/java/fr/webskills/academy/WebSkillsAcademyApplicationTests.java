package fr.webskills.academy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.webskills.academy.domain.AccessCode;
import fr.webskills.academy.domain.Lesson;
import fr.webskills.academy.domain.enums.PublicationStatus;
import fr.webskills.academy.repository.*;
import java.time.Instant;
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
    @Autowired LearnerProgressRepository progress;

    @BeforeEach
    void cleanAccessCodes() {
        accessCodes.deleteAll();
    }

    @Test
    void admin_generates_lists_and_revokes_access_code() throws Exception {
        String token = adminToken();
        String createdBody =
                mvc.perform(
                                post("/api/v1/admin/access-codes")
                                        .header("Authorization", "Bearer " + token)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("{\"label\":\"Promotion\"}"))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.code").isNotEmpty())
                        .andExpect(jsonPath("$.active").value(true))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        JsonNode created = json.readTree(createdBody);
        assertThat(created.get("code").asText()).startsWith("WSA-");

        mvc.perform(get("/api/v1/admin/access-codes").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].label").value("Promotion"));

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
    void register_requires_active_access_code() throws Exception {
        String code = "REGISTER-" + UUID.randomUUID();
        accessCode(code, true, null);
        mvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"email":"new-user@test.local","password":"Password123!","firstName":"Ada","lastName":"Lovelace","accessCode":"%s"}
                                """
                                                .formatted(code)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.role").value("LEARNER"));
    }

    @Test
    void learner_cannot_generate_access_code() throws Exception {
        String learnerToken = learnerToken("LEARNER-FORBID-" + UUID.randomUUID());
        mvc.perform(
                        post("/api/v1/admin/access-codes")
                                .header("Authorization", "Bearer " + learnerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                .andExpect(status().isForbidden());
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
                .andExpect(status().isBadRequest());
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
    void public_learning_filters_published_content() throws Exception {
        mvc.perform(get("/api/v1/learning-domains"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slug").exists());
        assertThat(domains.findByStatusOrderByDisplayOrderAsc(PublicationStatus.PUBLISHED))
                .isNotEmpty();
        mvc.perform(get("/api/v1/learning-domains/html/sections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PUBLISHED"));
    }

    @Test
    void update_progress_still_works() throws Exception {
        String token = learnerToken("PROGRESS-" + UUID.randomUUID());
        Lesson lesson = lessons.findAll().getFirst();
        mvc.perform(
                        put("/api/v1/progress/lessons/" + lesson.getId())
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"progressPercentage\":40,\"completed\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.progressPercentage").value(40));
        assertThat(progress.findAll()).isNotEmpty();
    }

    private AccessCode accessCode(String raw, boolean active, Instant revokedAt) {
        AccessCode code = new AccessCode();
        code.setLabel("Test");
        code.setCode(raw);
        code.setActive(active);
        code.setRevokedAt(revokedAt);
        return accessCodes.save(code);
    }

    private String adminToken() throws Exception {
        String body =
                mvc.perform(
                                post("/api/v1/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                """
                                {"email":"admin@test.local","password":"Admin123!"}
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
