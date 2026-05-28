package org.jas.ksinxapp.security;

import lombok.RequiredArgsConstructor;
import org.jas.ksinxapp.jwt.AuthEntryPointJwt;
import org.jas.ksinxapp.jwt.AuthTokenFilter;
import org.jas.ksinxapp.oauth2.OAuth2SuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                // Sessions needed for the OAuth2 state parameter exchange; JWT endpoints are stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .oauth2Login(oauth -> oauth
                        .successHandler(oAuth2SuccessHandler)
                )
                .exceptionHandling(e -> e.authenticationEntryPoint(authEntryPointJwt))
                .authorizeHttpRequests(auth -> auth
                        // Static frontend
                        .requestMatchers("/", "/*.html", "/css/**", "/js/**", "/img/**").permitAll()
                        // OAuth2 authorization redirect (Spring Security's own endpoint)
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        // Public API
                        .requestMatchers(
                                "/api/course/all", "/api/course/find/**",
                                "/api/v1/users/register", "/api/v1/users/login",
                                "/api/v1/modules/course/**"
                        ).permitAll()
                        // Role-based
                        .requestMatchers("/api/v1/users/delete/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/users/*/role").hasRole("ADMIN")
                        .requestMatchers("/api/v1/users/update/**").hasRole("STUDENT")
                        .requestMatchers("/api/vi/enrollments/progress", "/api/vi/enrollments/my").hasRole("STUDENT")
                        .requestMatchers("/api/v1/users/auth/token").authenticated()
                        .requestMatchers("/api/v1/submission/submit").hasRole("STUDENT")
                        .requestMatchers(
                                "/api/course/update/**", "/api/course/add", "/api/course/delete/**",
                                "/api/v1/tasks", "/api/v1/submission/**", "/api/v1/modules/update/**"
                        ).hasRole("TEACHER")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
