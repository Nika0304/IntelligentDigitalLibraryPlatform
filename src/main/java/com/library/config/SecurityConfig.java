package com.library.config;

import com.library.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig
{
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter)
    {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
    {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/users").permitAll()

                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/users").permitAll()

                        // 🔒 doar utilizatori autentificați pot descărca
                        .requestMatchers("/api/books/*/download").authenticated()

                        .requestMatchers("/api/books/**").permitAll()
                        .requestMatchers("/api/categories/**").permitAll()

                        .requestMatchers("/api/books/**").permitAll()
                        .requestMatchers("/api/categories/**").permitAll()
                        .requestMatchers("/api/authors/**").permitAll()
                        .requestMatchers("/api/stats/**").permitAll()
                        .requestMatchers("/api/external/books/**").permitAll()

                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/fines/**").hasRole("ADMIN")
                        .requestMatchers("/api/book-copies/**").hasRole("ADMIN")

                        .requestMatchers("/api/chat/faq").permitAll()
                        .requestMatchers("/api/chat/match").permitAll()
                        .requestMatchers("/api/chat/questions/pending").hasRole("ADMIN")
                        .requestMatchers("/api/chat/questions/**").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/chat/faq").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/chat/faq/**").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/chat/faq/**").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/groups").permitAll()
                                // 1. Endpoint-uri publice — întâi cele specifice, apoi wildcard generic
                                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/groups").permitAll()
                                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/groups/top").permitAll()
                                .requestMatchers("/api/groups/pending").hasRole("ADMIN")
                                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/groups/*").permitAll()

// 2. Operațiuni admin pe un grup
                                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/groups/*/approve").hasRole("ADMIN")
                                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/groups/*/reject").hasRole("ADMIN")
                                .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/groups/*").hasRole("ADMIN")

// 3. Restul (join, leave, vote, messages, mute) cer autentificare
                                .requestMatchers("/api/groups/**").authenticated()
                                .requestMatchers("/api/recommendations/**").authenticated()
                                .requestMatchers("/api/reports/**").hasRole("ADMIN")
                                .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception
    {
        return config.getAuthenticationManager();
    }
}