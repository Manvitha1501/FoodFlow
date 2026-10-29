package com.foodflow.config;

import com.foodflow.security.JwtAuthenticationEntryPoint;
import com.foodflow.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            UserDetailsService userDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/api/admin/login", "/error").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/restaurant/**").hasRole("RESTAURANT")
                        .requestMatchers(HttpMethod.GET, "/api/ngo/**").hasAnyRole("RESTAURANT", "NGO", "ADMIN")
                        .requestMatchers("/api/ngo/**").hasRole("NGO")
                        .requestMatchers(HttpMethod.POST, "/api/donations/**").hasRole("RESTAURANT")
                        .requestMatchers(HttpMethod.PUT, "/api/donations/**").hasRole("RESTAURANT")
                        .requestMatchers(HttpMethod.GET, "/api/donations/**").hasAnyRole("RESTAURANT", "NGO", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/predictions/**").hasRole("RESTAURANT")
                        .requestMatchers(HttpMethod.GET, "/api/predictions/**").hasAnyRole("RESTAURANT", "NGO", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/risk-prediction/**").hasAnyRole("RESTAURANT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/risk-prediction/**").hasAnyRole("RESTAURANT", "NGO", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/matching/**").hasAnyRole("RESTAURANT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/matching/history").hasAnyRole("RESTAURANT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/matching/**").hasAnyRole("RESTAURANT", "NGO", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/allocation/**").hasAnyRole("RESTAURANT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/allocation/history").hasAnyRole("RESTAURANT", "NGO", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/allocation/*/my-portion").hasAnyRole("NGO", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/allocation/donation/*/my-portion").hasAnyRole("NGO", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/allocation/**").hasAnyRole("RESTAURANT", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/workflow/*/accept").hasAnyRole("NGO", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/workflow/*/confirm").hasAnyRole("RESTAURANT", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/workflow/*/schedule-pickup").hasAnyRole("RESTAURANT", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/workflow/*/pickup-start").hasAnyRole("NGO", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/workflow/*/pickup-complete").hasAnyRole("NGO", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/workflow/*/delivery-complete").hasAnyRole("NGO", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/workflow/*/cancel").hasAnyRole("RESTAURANT", "NGO", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/workflow/donation/*").hasAnyRole("RESTAURANT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/workflow/history").hasAnyRole("RESTAURANT", "NGO", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/workflow/**").hasAnyRole("RESTAURANT", "NGO", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/trust/recalculate/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/trust/**").hasAnyRole("RESTAURANT", "NGO", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/analytics/**").hasAnyRole("RESTAURANT", "NGO", "ADMIN")
                        .requestMatchers("/api/user/**").authenticated()
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173", "http://127.0.0.1:3000", "http://127.0.0.1:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Cache-Control", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
