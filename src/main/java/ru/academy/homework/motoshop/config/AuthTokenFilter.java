package ru.academy.homework.motoshop.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.academy.homework.motoshop.services.UserDetailsServiceImpl;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * JWT-фильтр аутентификации, который обрабатывает каждый запрос и извлекает JWT токен
 * для аутентификации пользователя.
 */
@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    // Список публичных путей, которые не требуют аутентификации
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/",
            "/index.html",
            "/auth/**",
            "/register",
            "/api/auth/**",
            "/css/**",
            "/js/**",
            "/images/**",
            "/error",
            "/favicon.ico",
            "/api/products.html",
            "/api/product.html"
    );

    /**
     * Обрабатывает каждый HTTP-запрос для извлечения и валидации JWT токена.
     *
     * @param request HTTP-запрос
     * @param response HTTP-ответ
     * @param filterChain цепочка фильтров
     * @throws ServletException если возникает ошибка сервлета
     * @throws IOException если возникает ошибка ввода/вывода
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getServletPath();
        String requestMethod = request.getMethod();

        logger.debug("Processing request: {} {}", requestMethod, requestPath);

        // Пропускаем публичные пути без проверки аутентификации
        if (isPublicPath(requestPath)) {
            logger.debug("Skipping JWT validation for public path: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = parseJwt(request);

            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                logger.debug("Valid JWT found for user: {}", username);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails,
                                null,
                                userDetails.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.debug("Authenticated user: {}", username);
            } else if (jwt != null) {
                logger.warn("Invalid JWT token provided");
            } else {
                logger.debug("No JWT token provided, continuing as anonymous user");
            }
        } catch (UsernameNotFoundException e) {
            logger.error("User not found for JWT token: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Проверяет, является ли путь публичным (не требующим аутентификации).
     *
     * @param path путь запроса
     * @return true если путь публичный, иначе false
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * Извлекает JWT токен из заголовка Authorization.
     *
     * @param request HTTP-запрос
     * @return JWT токен или null, если токен не найден
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}