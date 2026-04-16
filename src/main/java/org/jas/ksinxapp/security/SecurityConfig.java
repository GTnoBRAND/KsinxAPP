package org.jas.ksinxapp.security;

import org.jas.ksinxapp.jwt.AuthEntryPointJwt;
import org.jas.ksinxapp.jwt.AuthTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity //allows to @PreAuthorize on controller
public class SecurityConfig {

    private final AuthEntryPointJwt authEntryPointJwt;
    private final AuthTokenFilter authTokenFilter;

    public SecurityConfig(AuthEntryPointJwt authEntryPointJwt,  AuthTokenFilter authTokenFilter)
    {
    this.authEntryPointJwt = authEntryPointJwt;
    this.authTokenFilter = authTokenFilter;
    }

    // Expose the PasswordEncoder as a Bean.
    // Spring Security will automatically pair this with your @Service MyUserDetailService
    // to instantly create a working DaoAuthenticationProvider.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @Bean
    public AuthenticationManager authenticationManagerBean(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .oauth2Login(Customizer.withDefaults())
                .exceptionHandling(e->
                        e.authenticationEntryPoint(authEntryPointJwt))
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/", "/register.html","/index.html", "/style.css", "/app.js","/dashboard.html", "/register.html", "/dashboard.js").permitAll()
                        // Publicly accessible paths
                        .requestMatchers("/api/course/all","/api/v1/users/register",
                                "/api/v1/users/login").permitAll()

                        // Role-based restrictions
                        .requestMatchers("/api/v1/users/delete/").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/v1/users/update/", "/api/v1/users/all", "/api/v1/users/auth/token").hasAuthority("ROLE_USER")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}