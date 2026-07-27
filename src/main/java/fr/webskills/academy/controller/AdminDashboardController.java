package fr.webskills.academy.controller;

import fr.webskills.academy.dto.LearningDtos.ClassDashboardResponse;
import fr.webskills.academy.service.AdminAccessCodeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {
    private final AdminAccessCodeService accessCodeService;

    public AdminDashboardController(AdminAccessCodeService accessCodeService) {
        this.accessCodeService = accessCodeService;
    }

    @GetMapping
    ClassDashboardResponse dashboard() {
        return accessCodeService.dashboard();
    }
}
