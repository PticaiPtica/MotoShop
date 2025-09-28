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

        // Пропускаем запросы logout без обработки
        if (requestURI.equals("/api/auth/logout")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = getJwtFromRequest(request);

            logger.debug("Processing request to: {}, JWT present: {}", requestURI, jwt != null);

            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("Set authentication for user: {} on URI: {}", username, requestURI);
            } else if (requiresAuthentication(requestURI) && jwt == null) {
                logger.debug("No JWT token found for protected URI: {}", requestURI);
            } else if (requiresAuthentication(requestURI) && jwt != null) {
                logger.warn("Invalid JWT token for protected URI: {}", requestURI);
            }

        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        // Проверяем заголовок Authorization
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        // Проверяем cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwtToken".equals(cookie.getName())) {
                    String jwt = cookie.getValue();
                    if (StringUtils.hasText(jwt)) {
                        logger.debug("Found JWT token in cookie, length: {}", jwt.length());
                        return jwt;
                    }
                }
            }
        }

        // Проверяем параметр token
        String jwtParam = request.getParameter("token");
        if (StringUtils.hasText(jwtParam)) {
            return jwtParam;
        }

        return null;
    }

    private boolean requiresAuthentication(String uri) {
        return uri.startsWith("/admin/") ||
                uri.startsWith("/moderator/") ||
                uri.startsWith("/profile") ||
                uri.startsWith("/api/admin/") ||
                uri.startsWith("/api/moderator/") ||
                uri.equals("/api/auth/validate");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/login") ||
                path.startsWith("/api/auth/register") ||
                path.startsWith("/api/auth/logout") || // ВАЖНО: исключаем logout
                path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/images/") ||
                path.equals("/favicon.ico") ||
                path.startsWith("/error");
    }
    private String getJwtFromRequest(HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwtToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}