package org.jas.ksinxapp.security;

import lombok.RequiredArgsConstructor;
import org.jas.ksinxapp.jwt.AuthEntryPointJwt;
import org.jas.ksinxapp.jwt.AuthTokenFilter;
import org.jas.ksinxapp.oauth2.OAuth2SuccessHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthEntryPointJwt authEntryPointJwt;
    private final AuthTokenFilter authTokenFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @Bean
    public AuthenticationManager authenticationManagerBean(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy("ROLE_ADMIN > ROLE_TEACHER > ROLE_STUDENT");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ObjectProvider<ClientRegistrationRepository> clientRegistrationRepository) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                // Sessions needed for the OAuth2 state parameter exchange; JWT endpoints are stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

        // OAuth2 login is only wired up when a provider is actually configured
        // (e.g. GOOGLE_CLIENT_ID/SECRET are set). Otherwise the app runs JWT-only.
        if (clientRegistrationRepository.getIfAvailable() != null) {
            http.oauth2Login(oauth -> oauth.successHandler(oAuth2SuccessHandler));
        }

        return http
                .exceptionHandling(e -> e.authenticationEntryPoint(authEntryPointJwt))
                .authorizeHttpRequests(auth -> auth
                        // Health check (used by Render/Docker) + static frontend
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/", "/*.html", "/css/**", "/js/**", "/img/**").permitAll()
                        // OAuth2 authorization redirect (Spring Security's own endpoint)
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers("/", "/api/v1/courses").permitAll()
                        .requestMatchers("/", "/courses", "/courses/**", "/login", "/register", "/sitemap.xml").permitAll()
                        .requestMatchers("/*.ico", "/*.png", "/*.svg", "/*.webmanifest").permitAll()
                        // Public API
                        .requestMatchers(
                                "/api/course/all", "/api/course/find/**",
                                "/api/v1/users/register", "/api/v1/users/login",
                                "/api/v1/users/verify", "/api/v1/users/resend-verification",
                                "/api/v1/modules/course/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/course/*/rating").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/course/*/rate").hasRole("STUDENT")
                        // Role-based
                        .requestMatchers("/api/v1/users/delete/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/users/*/role").hasRole("ADMIN")
                        .requestMatchers("/api/v1/users/update/**").hasRole("STUDENT")
                        .requestMatchers("/api/vi/enrollments/progress", "/api/vi/enrollments/my").hasRole("STUDENT")
                        .requestMatchers("/api/v1/users/auth/token").authenticated()
                        .requestMatchers("/api/v1/submission/submit").hasRole("STUDENT")
                        // Student listing their own submissions for the Submissions page
                        .requestMatchers(HttpMethod.GET, "/api/v1/submission/my").hasRole("STUDENT")
                        // The submission/{id}/file and /{id}/feedback endpoints enforce their own ownership checks
                        // (student-owner OR teacher), so they just need an authenticated caller.
                        .requestMatchers(HttpMethod.GET, "/api/v1/submission/*/file").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/submission/*/feedback").authenticated()
                        // Direct uploads to MinIO's public bucket back the teacher's course
                        // cover photo / module teaser video flow — restrict to TEACHER.
                        .requestMatchers("/api/v1/files/upload").hasRole("TEACHER")
                        .requestMatchers(
                                "/api/course/update/**", "/api/course/add", "/api/course/delete/**",
                                "/api/course/*/status",
                                "/api/v1/tasks", "/api/v1/submission/**", "/api/v1/modules/update/**"
                        ).hasRole("TEACHER")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
