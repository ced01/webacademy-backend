package fr.webskills.academy.controller;

import fr.webskills.academy.dto.LearningDtos.*;
import fr.webskills.academy.service.AdminAccessCodeService;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/access-codes")
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
    AccessCodeAdminResponse create(@RequestBody @Valid AccessCodeRequest r) {
        return service.create(r);
    }

    @PutMapping("/{id}")
    AccessCodeAdminResponse update(
            @PathVariable UUID id, @RequestBody @Valid AccessCodeUpdateRequest r) {
        return service.update(id, r);
    }

    @PatchMapping("/{id}/status")
    AccessCodeAdminResponse status(@PathVariable UUID id, @RequestBody StatusRequest r) {
        return service.status(id, r.active());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable UUID id) {
        service.disable(id);
    }
}
