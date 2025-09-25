package ru.academy.homework.motoshop.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import ru.academy.homework.motoshop.services.UserDetailsImpl;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;

/**
 * Утилитный класс для работы с JWT (JSON Web Token) токенами.
 *
 * <p>Обеспечивает функциональность для генерации, валидации и парсинга JWT токенов
 * с использованием алгоритма HMAC-SHA512. Класс автоматически настраивается через
 * свойства приложения и обрабатывает различные форматы секретных ключей.</p>
 *
 * <p>Основные возможности:</p>
 * <ul>
 *   <li>Генерация JWT токена на основе аутентификации пользователя</li>
 *   <li>Валидация JWT токена и проверка срока действия</li>
 *   <li>Извлечение информации о пользователе из токена</li>
 *   <li>Автоматическая обработка Base64 и строковых секретных ключей</li>
 * </ul>
 *
 */
@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    /** Секретный ключ для подписи JWT токенов */
    @Value("${motoshop.app.jwtSecret}")
    private String jwtSecret;

    /** Время жизни JWT токена в миллисекундах */
    @Value("${motoshop.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    /**
     * Создает и возвращает секретный ключ для подписи JWT токенов.
     *
     * <p>Метод поддерживает два формата секретного ключа:</p>
     * <ul>
     *   <li>Base64-encoded строка (рекомендуемый формат)</li>
     *   <li>Обычная текстовая строка (с проверкой минимальной длины)</li>
     * </ul>
     *
     * <p>Для алгоритма HS512 требуется ключ длиной не менее 64 байт.</p>
     *
     * @return секретный ключ типа {@link SecretKey}
     * @throws IllegalArgumentException если секретный ключ не настроен или слишком короткий
     * @throws RuntimeException если произошла ошибка при создании ключа
     * @see #isValidBase64(String)
     */
    private SecretKey getSigningKey() {
        try {
            if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
                throw new IllegalArgumentException("JWT secret is not configured");
            }

            // Проверяем, является ли строка валидным Base64
            if (isValidBase64(jwtSecret)) {
                // Декодируем Base64
                byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
                return Keys.hmacShaKeyFor(keyBytes);
            } else {
                // Если не Base64, используем как обычную строку (UTF-8)
                logger.warn("JWT secret is not valid Base64. Using as plain string.");
                byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);

                // Проверяем длину для HS512 (минимум 64 байта)
                if (keyBytes.length < 64) {
                    throw new IllegalArgumentException(
                            "JWT secret is too short. Must be at least 64 bytes for HS512. " +
                                    "Current: " + keyBytes.length + " bytes. " +
                                    "Consider generating a proper Base64 key with: openssl rand -base64 64"
                    );
                }

                return Keys.hmacShaKeyFor(keyBytes);
            }

        } catch (Exception e) {
            logger.error("Error creating signing key: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create JWT signing key", e);
        }
    }

    /**
     * Проверяет, является ли строка валидной Base64-encoded строкой.
     *
     * @param str строка для проверки
     * @return true если строка является валидным Base64, false в противном случае
     */
    private boolean isValidBase64(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }

        try {
            Decoders.BASE64.decode(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Генерирует JWT токен на основе данных аутентификации пользователя.
     *
     * <p>Токен содержит следующие claims (поля):</p>
     * <ul>
     *   <li>subject - имя пользователя</li>
     *   <li>id - идентификатор пользователя</li>
     *   <li>email - электронная почта пользователя</li>
     *   <li>roles - список ролей пользователя</li>
     *   <li>issuedAt - время создания токена</li>
     *   <li>expiration - время истечения токена</li>
     * </ul>
     *
     * @param authentication объект аутентификации Spring Security
     * @return JWT токен в виде строки
     * @throws RuntimeException если произошла ошибка при генерации токена
     * @throws ClassCastException если principal не является экземпляром UserDetailsImpl
     */
    public String generateJwtToken(Authentication authentication) {
        try {
            UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

            // Добавляем кастомные claims с пользовательскими данными
            return Jwts.builder()
                    .setSubject(userPrincipal.getUsername())
                    .claim("id", userPrincipal.getId())
                    .claim("email", userPrincipal.getEmail())
                    .claim("roles", userPrincipal.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toList()))
                    .setIssuedAt(new Date())
                    .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                    .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                    .compact();

        } catch (Exception e) {
            logger.error("Error generating JWT token: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating JWT token: " + e.getMessage(), e);
        }
    }

    /**
     * Извлекает имя пользователя из JWT токена.
     *
     * <p>Метод парсит токен и возвращает значение subject claim.</p>
     *
     * @param token JWT токен для парсинга
     * @return имя пользователя или null если токен невалиден
     * @see Claims#getSubject()
     */
    public String getUserNameFromJwtToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            logger.error("Error extracting username from JWT: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Проверяет валидность JWT токена.
     *
     * <p>Выполняет следующие проверки:</p>
     * <ul>
     *   <li>Корректность подписи</li>
     *   <li>Формат токена</li>
     *   <li>Срок действия токена</li>
     *   <li>Поддержка алгоритма</li>
     *   <li>Наличие claims</li>
     * </ul>
     *
     * @param authToken JWT токен для проверки
     * @return true если токен валиден, false в противном случае
     * @see Jwts#parserBuilder()
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(authToken);
            return true;

        } catch (SecurityException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        }
        return false;
    }
}