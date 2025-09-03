package ru.academy.homework.motoshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/categories", "/images/**", "/css/**", "/js/**").permitAll() // Разрешаем доступ без авторизации
                        .anyRequest().authenticated() // Все остальные запросы требуют авторизации
                )
                .formLogin(form -> form
                        .loginPage("/login") // Страница логина
                        .permitAll() // Разрешаем доступ к странице логина
                )
                .logout(logout -> logout
                        .permitAll() // Разрешаем logout
                )
                .csrf(csrf -> csrf.disable()); // Отключаем CSRF для простоты (для production включите обратно)

        return http.build();
    }
}
