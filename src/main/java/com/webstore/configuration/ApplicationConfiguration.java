package com.webstore.configuration;

import com.webstore.constant.UserRole;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import com.webstore.security.JwtAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import okhttp3.OkHttpClient;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class ApplicationConfiguration {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return new AuditorAwareImplementation();
    }

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient();
    }

    public static class AuditorAwareImplementation implements AuditorAware<String> {
        @Override
        public Optional<String> getCurrentAuditor() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.of("admin");
            }

            return Optional.of(authentication.getName());
        }
    }

    @Bean
    @Profile("!local")
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Configuration
    @EnableWebSecurity
    @Profile("!local")
    public static class SecurityConfiguration {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        public SecurityConfiguration(JwtAuthenticationFilter jwtAuthenticationFilter) {
            this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(csrf -> csrf.disable())
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(authorize -> authorize
                            // Public endpoints - no authentication required
                            .requestMatchers("/api/auth/**").permitAll()
                            .requestMatchers("/actuator/**").permitAll() // THIS IS THE NEW LINE

                            // Admin-only endpoints - using constant
                            .requestMatchers("/api/admin/**").hasRole(UserRole.ADMIN)

                            // Seller-only endpoints - using constant
                            .requestMatchers("/api/seller/**").hasRole(UserRole.SELLER)

                            // Endpoints accessible by both ADMIN and SELLER - using constants
                            .requestMatchers("/api/products/**").hasAnyRole(UserRole.ADMIN, UserRole.SELLER)
                            .requestMatchers("/api/catalogues/**").hasAnyRole(UserRole.ADMIN, UserRole.SELLER)
                            .requestMatchers("/api/categories/**").hasAnyRole(UserRole.ADMIN, UserRole.SELLER)

                            // All other requests require authentication (any role)
                            .anyRequest().authenticated())
                    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            return http.build();
        }
    }
}