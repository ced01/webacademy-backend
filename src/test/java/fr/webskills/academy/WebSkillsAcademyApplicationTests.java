package fr.webskills.academy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.webskills.academy.domain.AccessCode;
import fr.webskills.academy.domain.Lesson;
import fr.webskills.academy.domain.enums.PublicationStatus;
import fr.webskills.academy.repository.*;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WebSkillsAcademyApplicationTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired PasswordEncoder encoder;
    @Autowired AccessCodeRepository accessCodes;
    @Autowired LearningDomainRepository domains;
    @Autowired LessonRepository lessons;
    @Autowired UserRepository users;
    @Autowired LearnerProgressRepository progress;

    @Test
    void validate_valid_access_code_returns_learner_token() throws Exception {
        accessCode("Valid", "ABC-123", true, null, 5, 0);
        mvc.perform(
                        post("/api/v1/auth/access-code")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"code":"ABC-123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.role").value("LEARNER"));
    }

    @Test
    void reject_unknown_expired_disabled_and_limited_access_codes() throws Exception {
        mvc.perform(
                        post("/api/v1/auth/access-code")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"code":"UNKNOWN"}
                                """))
                .andExpect(status().isBadRequest());
        accessCode("Expired", "EXPIRED", true, Instant.now().minusSeconds(60), null, 0);
        accessCode("Disabled", "DISABLED", false, null, null, 0);
        accessCode("Limited", "LIMITED", true, null, 1, 1);
        assertRejected("EXPIRED");
        assertRejected("DISABLED");
        assertRejected("LIMITED");
    }

    @Test
    void admin_can_create_domain_and_learner_cannot() throws Exception {
        String adminToken = adminToken();
        mvc.perform(
                        post("/api/v1/admin/domains")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"name":"React","slug":"react-test","shortDescription":"UI","description":"React","displayOrder":50,"status":"PUBLISHED"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("react-test"));
        String learnerToken = learnerToken("FORBID-123");
        mvc.perform(
                        post("/api/v1/admin/domains")
                                .header("Authorization", "Bearer " + learnerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"name":"Docker","slug":"docker","displayOrder":60,"status":"PUBLISHED"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void retrieve_published_domains() throws Exception {
        mvc.perform(
                        get("/api/v1/domains")
                                .header("Authorization", "Bearer " + learnerToken("DOMAINS-123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slug").exists());
        assertThat(domains.findByStatusOrderByDisplayOrderAsc(PublicationStatus.PUBLISHED))
                .isNotEmpty();
    }

    @Test
    void update_progress_and_unique_constraint() throws Exception {
        String token = learnerToken("PROGRESS-123");
        Lesson lesson = lessons.findAll().getFirst();
        mvc.perform(
                        put("/api/v1/progress/lessons/" + lesson.getId())
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"progressPercentage":40,"completed":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.progressPercentage").value(40));
        mvc.perform(
                        post("/api/v1/progress/lessons/" + lesson.getId() + "/complete")
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));
        assertThat(
                        progress.findAll().stream()
                                .anyMatch(
                                        p ->
                                                p.getLesson().getId().equals(lesson.getId())
                                                        && p.isCompleted()))
                .isTrue();
    }

    private void assertRejected(String code) throws Exception {
        mvc.perform(
                        post("/api/v1/auth/access-code")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"code\":\"" + code + "\"}"))
                .andExpect(status().isBadRequest());
    }

    private AccessCode accessCode(
            String label,
            String raw,
            boolean active,
            Instant expiresAt,
            Integer maxUses,
            int usageCount) {
        AccessCode code = new AccessCode();
        code.setLabel(label);
        code.setCodeHash(encoder.encode(raw));
        code.setActive(active);
        code.setExpiresAt(expiresAt);
        code.setMaxUses(maxUses);
        code.setUsageCount(usageCount);
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
        accessCode("Learner", code, true, null, 10, 0);
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
