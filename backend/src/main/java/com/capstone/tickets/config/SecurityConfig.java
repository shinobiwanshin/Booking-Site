package com.capstone.tickets.config;

import com.capstone.tickets.filters.UserProvisioningFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {

        @Bean
        public SecurityFilterChain filterChain(
                        HttpSecurity http,
                        UserProvisioningFilter userProvisioningFilter,
                        JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
                http
                                .authorizeHttpRequests(authorize -> authorize
                                                // Allow OPTIONS for CORS preflight
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/v1/published-events/**")
                                                .permitAll()
                                                // Allow unauthenticated access to authentication endpoints
                                                .requestMatchers(HttpMethod.POST, "/api/auth/register")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/auth/login")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/auth/forgot-password")
                                                .permitAll()
                                                // Allow temporary admin endpoints for role management
                                                .requestMatchers("/api/admin/**")
                                                .permitAll()
                                                // Allow attendees (any authenticated user) to purchase tickets
                                                .requestMatchers(
                                                                new AntPathRequestMatcher(
                                                                                "/api/v1/events/*/ticket-types/*/tickets",
                                                                                HttpMethod.POST.name()))
                                                .authenticated()
                                                .requestMatchers("/api/v1/events/**").hasRole("ORGANIZER")
                                                .requestMatchers("/api/v1/dashboard/**").hasRole("ORGANIZER")
                                                // Catch all rule
                                                .anyRequest().authenticated())
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .oauth2ResourceServer(
                                                oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(
                                                                jwtAuthenticationConverter)))
                                .addFilterAfter(userProvisioningFilter, BearerTokenAuthenticationFilter.class);

                return http.build();
        }

}
