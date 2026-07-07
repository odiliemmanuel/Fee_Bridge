package com.feebridge.auth.security;

import com.feebridge.auth.domain.RoleNames;
import com.feebridge.common.tenant.TenantContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/** Validates the Bearer JWT, populates the SecurityContext and the {@link TenantContext}. */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                Jws<Claims> jws = jwtService.parse(header.substring(7));
                Claims claims = jws.getPayload();

                Long userId = claims.get("uid", Number.class) == null ? null : claims.get("uid", Number.class).longValue();
                Long schoolId = claims.get("sid", Number.class) == null ? null : claims.get("sid", Number.class).longValue();
                @SuppressWarnings("unchecked")
                List<String> roles = claims.get("roles", List.class);
                boolean platformAdmin = roles != null && roles.contains(RoleNames.PLATFORM_ADMIN);

                var authorities = roles == null ? List.<SimpleGrantedAuthority>of()
                        : roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList();

                AuthPrincipal principal = new AuthPrincipal(userId, schoolId, claims.getSubject(), platformAdmin);
                var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                TenantContext.set(schoolId, userId, platformAdmin);
            } catch (Exception ex) {
                SecurityContextHolder.clearContext();
                TenantContext.clear();
            }
        }
        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
