package ru.academy.homework.motoshop.services;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.academy.homework.motoshop.entity.User;

import java.io.Serial;
import java.util.Collection;
import java.util.Collections;

/**
 * Реализация интерфейса {@link UserDetails} для интеграции сущности {@link User}
 * с механизмами аутентификации и авторизации Spring Security.
 *
 * <p>Класс служит адаптером между бизнес-сущностью пользователя и требованиями
 * Spring Security, предоставляя необходимую информацию о пользователе для
 * системы безопасности.</p>
 *
 * <p>Экземпляры этого класса создаются через статический фабричный метод
 * {@link #build(User)}, который преобразует объект {@link User} в {@link UserDetails}.</p>
 */
public class UserDetailsImpl implements UserDetails {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Уникальный идентификатор пользователя
     */
    private final Long id;

    /**
     * Имя пользователя для аутентификации
     */
    private final String username;

    /**
     * Электронная почта пользователя
     */
    private final String email;

    /**
     * Зашифрованный пароль пользователя
     */
    private final String password;

    /**
     * Коллекция прав доступа (ролей) пользователя
     */
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Ссылка на исходный объект пользователя для доступа к бизнес-логике
     */
    private final User user;

    /**
     * Конструктор для создания полного объекта пользователя для Spring Security.
     *
     * @param id          уникальный идентификатор пользователя
     * @param username    имя пользователя для аутентификации
     * @param email       электронная почта пользователя
     * @param password    зашифрованный пароль пользователя
     * @param authorities коллекция прав доступа пользователя
     * @param user        исходный объект пользователя
     */
    public UserDetailsImpl(Long id, String username, String email, String password,
                           Collection<? extends GrantedAuthority> authorities, User user) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.user = user;
    }

    /**
     * Фабричный метод для преобразования сущности {@link User} в {@link UserDetails}.
     *
     * <p>Создает объект {@link UserDetailsImpl} на основе данных пользователя,
     * включая преобразование роли пользователя в {@link GrantedAuthority}.</p>
     *
     * @param user сущность пользователя для преобразования
     * @return реализацию {@link UserDetails} для использования в Spring Security
     * @throws IllegalArgumentException если пользователь или его роль равны null
     */
    public static UserDetailsImpl build(User user) {
        // Создаем authority на основе одной роли пользователя
        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().getName().name());
        Collection<? extends GrantedAuthority> authorities = Collections.singletonList(authority);

        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                authorities,
                user);
    }

    /**
     * Возвращает уникальный идентификатор пользователя.
     *
     * @return идентификатор пользователя
     */
    public Long getId() {
        return id;
    }

    /**
     * Возвращает электронную почту пользователя.
     *
     * @return email пользователя
     */
    public String getEmail() {
        return email;
    }

    /**
     * Возвращает исходный объект пользователя для доступа к полной бизнес-логике.
     *
     * @return сущность {@link User}
     */
    public User getUser() {
        return user;
    }

    /**
     * Возвращает права доступа (роли) пользователя.
     *
     * @return коллекция {@link GrantedAuthority}, представляющая права пользователя
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * Возвращает зашифрованный пароль пользователя.
     *
     * @return пароль пользователя в зашифрованном виде
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Возвращает имя пользователя для аутентификации.
     *
     * @return имя пользователя
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Показывает, не истек ли срок действия учетной записи.
     *
     * <p>В текущей реализации всегда возвращает true.</p>
     *
     * @return true если учетная запись действительна, false если истекла
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Показывает, не заблокирована ли учетная запись.
     *
     * <p>В текущей реализации всегда возвращает true.</p>
     *
     * @return true если учетная запись не заблокирована, false если заблокирована
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Показывает, не истек ли срок действия учетных данных (пароля).
     *
     * <p>В текущей реализации всегда возвращает true.</p>
     *
     * @return true если учетные данные действительны, false если истекли
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Показывает, активен ли пользователь.
     *
     * <p>В текущей реализации всегда возвращает true.</p>
     *
     * @return true если пользователь активен, false если отключен
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Проверяет равенство объектов на основе идентификатора пользователя.
     *
     * @param obj объект для сравнения
     * @return true если объекты равны, false в противном случае
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl) obj;
        return id.equals(user.id);
    }

    /**
     * Возвращает хэш-код на основе идентификатора пользователя.
     *
     * @return хэш-код объекта
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}