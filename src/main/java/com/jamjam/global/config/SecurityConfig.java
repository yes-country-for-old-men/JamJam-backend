package com.jamjam.global.config;

import com.jamjam.global.properties.CorsProperties;
import com.jamjam.global.properties.WebSocketProperties;
import com.jamjam.infra.jwt.application.JwtUtil;
import com.jamjam.infra.jwt.domain.repository.RefreshRepository;
import com.jamjam.infra.jwt.exception.CustomAuthenticationEntryPoint;
import com.jamjam.infra.jwt.filter.CustomLogoutFilter;
import com.jamjam.infra.jwt.filter.JwtFilter;
import com.jamjam.infra.jwt.filter.LoginFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.RequiredArgsConstructor;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/user/reissue",
            "/api/user/login",
            "/api/user/join/**",
            "/favicon.ico",
            "/error",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/webjars/**",
            "/api/user/sms/**",
            "/api/user/check/**",
            "/ws-chat/**",
            "/ws-chat"
    };

    private static final String[] ADMIN_ENDPOINTS = {
            "/admin/**"
    };

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtUtil jwtUtil;
    private final RefreshRepository refreshRepository;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CorsProperties corsProps;
    private final WebSocketProperties webSocketProps;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(this::buildCorsConfig));

        http.sessionManagement(sm ->
                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.exceptionHandling(eh ->
                eh.authenticationEntryPoint(customAuthenticationEntryPoint));

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                .requestMatchers(webSocketProps.getEndpoint(),
                        webSocketProps.getEndpoint() + "/**").permitAll()
                .requestMatchers(ADMIN_ENDPOINTS).hasRole("ADMIN")
                .anyRequest().authenticated());

        http.addFilterAt(new LoginFilter(authenticationManager(), jwtUtil, refreshRepository),
                UsernamePasswordAuthenticationFilter.class);

        http.addFilterAt(new CustomLogoutFilter(jwtUtil, refreshRepository),
                org.springframework.security.web.authentication.logout.LogoutFilter.class);

        http.addFilterBefore(new JwtFilter(jwtUtil, PUBLIC_ENDPOINTS),
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private CorsConfiguration buildCorsConfig(HttpServletRequest req) {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(corsProps.getAllowedOrigins());
        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("Authorization","Set-Cookie"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);
        return cfg;
    }
}