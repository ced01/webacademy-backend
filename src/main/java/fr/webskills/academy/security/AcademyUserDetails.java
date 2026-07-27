package fr.webskills.academy.security;

import fr.webskills.academy.domain.AccessCode;
import fr.webskills.academy.domain.User;
import fr.webskills.academy.domain.enums.Role;
import java.util.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class AcademyUserDetails implements UserDetails {
    private final User user;
    private final UUID accessCodeId;
    private final String accessCodeLabel;
    private final String welcomeMessage;
    private final String recommendedPath;
    private final Role role;
    private final boolean enabled;

    public AcademyUserDetails(User user) {
        this.user = user;
        this.accessCodeId = null;
        this.accessCodeLabel = null;
        this.welcomeMessage = null;
        this.recommendedPath = null;
        this.role = user.getRole();
        this.enabled = user.isEnabled();
    }

    private AcademyUserDetails(AccessCode accessCode) {
        this.user = null;
        this.accessCodeId = accessCode.getId();
        this.accessCodeLabel = accessCode.getLabel();
        this.welcomeMessage = accessCode.getWelcomeMessage();
        this.recommendedPath = accessCode.getRecommendedPath();
        this.role = Role.LEARNER;
        this.enabled = accessCode.isUsable();
    }

    public static AcademyUserDetails forAccessCode(AccessCode accessCode) {
        return new AcademyUserDetails(accessCode);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return user == null ? null : user.getPassword();
    }

    @Override
    public String getUsername() {
        if (user != null) {
            return user.getEmail() != null ? user.getEmail() : user.getId().toString();
        }
        return "access-code:" + accessCodeId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public UUID id() {
        return user == null ? null : user.getId();
    }

    public User user() {
        return user;
    }

    public Role role() {
        return role;
    }

    public UUID accessCodeId() {
        return accessCodeId;
    }

    public String accessCodeLabel() {
        return accessCodeLabel;
    }

    public String welcomeMessage() {
        return welcomeMessage;
    }

    public String recommendedPath() {
        return recommendedPath;
    }
}
