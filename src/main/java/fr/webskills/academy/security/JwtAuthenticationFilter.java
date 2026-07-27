package fr.webskills.academy.security;

import fr.webskills.academy.repository.AccessCodeRepository;
import fr.webskills.academy.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AccessCodeRepository accessCodeRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository,
            AccessCodeRepository accessCodeRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.accessCodeRepository = accessCodeRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }
        String token = header.substring(7);
        try {
            if (!jwtService.valid(token)) {
                unauthorized(response);
                return;
            }
            Claims claims = jwtService.claims(token);
            AcademyUserDetails details = detailsFromClaims(claims);
            if (details == null || !details.isEnabled()) {
                unauthorized(response);
                return;
            }
            var auth =
                    new UsernamePasswordAuthenticationToken(
                            details, token, details.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            unauthorized(response);
            return;
        }
        chain.doFilter(request, response);
    }

    private AcademyUserDetails detailsFromClaims(Claims claims) {
        Object accessCodeId = claims.get("accessCodeId");
        if (accessCodeId != null) {
            return accessCodeRepository
                    .findById(UUID.fromString(String.valueOf(accessCodeId)))
                    .filter(code -> code.isUsable())
                    .map(AcademyUserDetails::forAccessCode)
                    .orElse(null);
        }
        UUID userId = UUID.fromString(String.valueOf(claims.get("uid")));
        return userRepository
                .findById(userId)
                .filter(user -> user.isEnabled())
                .map(AcademyUserDetails::new)
                .orElse(null);
    }

    private void unauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"invalid_token\"}");
    }
}
