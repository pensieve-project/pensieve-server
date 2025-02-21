package ru.hse.pensieve.authorization.config;

import ru.hse.pensieve.authorization.service.JwtService;
import ru.hse.pensieve.authorization.model.JwtAuthentication;
import ru.hse.pensieve.authorization.model.Role;
import lombok.RequiredArgsConstructor;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtFilter extends GenericFilterBean {

    private static final String AUTHORIZATION = "Authorization";

    private final JwtService jwtService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain fc) throws IOException, ServletException {
        final String token = getTokenFromRequest((HttpServletRequest) request);
        if (token != null && jwtService.validateAccessToken(token)) {
            final Claims claims = jwtService.getAccessClaims(token);
            SecurityContextHolder.getContext().setAuthentication(getInfoToken(claims));
        }
        fc.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        final String bearer = request.getHeader(AUTHORIZATION);
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    private JwtAuthentication getInfoToken(Claims claims) {
        final JwtAuthentication jwtInfoToken = new JwtAuthentication();
        final List<String> roles = claims.get("roles", List.class);
        jwtInfoToken.setRoles(roles.stream().map(Role::valueOf).collect(Collectors.toSet()));
        jwtInfoToken.setUsername(claims.getSubject());
        jwtInfoToken.setAuthenticated(true);
        return jwtInfoToken;
    }
}