package fr.webskills.academy.controller;

import fr.webskills.academy.dto.LearningDtos.*;
import fr.webskills.academy.security.AcademyUserDetails;
import fr.webskills.academy.service.AdminAccessCodeService;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/access-codes")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAccessCodeController {
    private final AdminAccessCodeService service;

    public AdminAccessCodeController(AdminAccessCodeService service) {
        this.service = service;
    }

    @GetMapping
    List<AccessCodeAdminResponse> all() {
        return service.all();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    AccessCodeAdminResponse create(
            @RequestBody(required = false) @Valid AccessCodeRequest r,
            @AuthenticationPrincipal AcademyUserDetails details) {
        return service.create(
                r == null
                        ? new AccessCodeRequest(null, null, null, null, null, null, List.of())
                        : r,
                details);
    }

    @PatchMapping("/{id}/revoke")
    AccessCodeAdminResponse revoke(@PathVariable UUID id) {
        return service.revoke(id);
    }

    @PatchMapping("/{id}/activate")
    AccessCodeAdminResponse activate(@PathVariable UUID id) {
        return service.activate(id);
    }

    @DeleteMapping("/{id}/delete")
    void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
