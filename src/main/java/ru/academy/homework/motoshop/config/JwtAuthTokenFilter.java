package ru.academy.homework.motoshop.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.academy.homework.motoshop.services.UserDetailsServiceImpl;

import java.io.IOException;

@Component
public class JwtAuthTokenFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthTokenFilter.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        try {
            String jwt = parseJwt(request);

            if (jwt != null) {
                logger.debug("JWT token found for URI: {}", requestURI);

                if (jwtUtils.validateJwtToken(jwt)) {
                    String username = jwtUtils.getUserNameFromJwtToken(jwt);
                    logger.debug("JWT token valid for user: {}", username);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    context.setAuthentication(authentication);
                    SecurityContextHolder.setContext(context);

                    logger.debug("Authentication set for user: {} on URI: {}", username, requestURI);
                } else {
                    logger.warn("JWT token is invalid for URI: {}", requestURI);
                    // Очищаем контекст только если путь требует аутентификации
                    if (requiresAuthentication(requestURI)) {
                        SecurityContextHolder.clearContext();
                        logger.debug("Cleared security context for protected URI: {}", requestURI);
                    }
                }
            } else {
                logger.debug("No JWT token found for URI: {}", requestURI);
                // Очищаем контекст только для защищенных путей, где ожидается аутентификация
                if (requiresAuthentication(requestURI)) {
                    SecurityContextHolder.clearContext();
                    logger.debug("Cleared security context - no token for protected URI: {}", requestURI);
                }
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication for URI: {} - Error: {}", requestURI, e.getMessage());
            // В случае ошибки очищаем контекст только для защищенных путей
            if (requiresAuthentication(requestURI)) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String uri) {
        // Более точное определение публичных путей
        return uri.startsWith("/api/auth/login") ||
                uri.startsWith("/api/auth/register") ||
                uri.startsWith("/api/auth/logout") ||
                uri.equals("/login") ||
                uri.startsWith("/css/") ||
                uri.startsWith("/js/") ||
                uri.startsWith("/images/") ||
                uri.equals("/favicon.ico") ||
                uri.startsWith("/error") ||
                uri.startsWith("/.well-known/");
    }

    private boolean requiresAuthentication(String uri) {
        // Все пути админки и API требуют аутентификации
        return uri.startsWith("/admin/") ||
                uri.startsWith("/api/admin/") ||
                uri.startsWith("/api/auth/validate") ||  // validate требует аутентификации!
                uri.startsWith("/moderator/") ||
                uri.startsWith("/profile");
    }

    private String parseJwt(HttpServletRequest request) {
        // 1. Проверяем заголовок Authorization (приоритет для API запросов)
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            String jwt = headerAuth.substring(7);
            if (StringUtils.hasText(jwt)) {
                logger.debug("Found JWT token in Authorization header for URI: {}", request.getRequestURI());
                return jwt;
            }
        }

        // 2. Проверяем токен из cookies (для браузерных запросов)
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwtToken".equals(cookie.getName())) {
                    String jwt = cookie.getValue();
                    if (StringUtils.hasText(jwt)) {
                        logger.debug("Found JWT token in cookie for URI: {}", request.getRequestURI());
                        return jwt;
                    }
                }
            }
        }

        // 3. Проверяем параметр запроса (для редких случаев)
        String jwtParam = request.getParameter("token");
        if (StringUtils.hasText(jwtParam)) {
            logger.debug("Found JWT token in request parameter for URI: {}", request.getRequestURI());
            return jwtParam;
        }

        return null;
    }
}