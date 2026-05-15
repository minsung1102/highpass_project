package com.example.highpass_backend.config;

import com.example.highpass_backend.security.JwtAuthenticationFilter;
import com.example.highpass_backend.security.OAuth2FailureHandler;
import com.example.highpass_backend.security.OAuth2SuccessHandler;
import com.example.highpass_backend.service.oauth2.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oauth2SuccessHandler;
    private final OAuth2FailureHandler oauth2FailureHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                request -> request.getServletPath().startsWith("/api/")
                                        || request.getServletPath().startsWith("/chat/")
                        )
                        .defaultAccessDeniedHandlerFor(
                                (request, response, accessDeniedException) ->
                                        response.sendError(HttpServletResponse.SC_FORBIDDEN),
                                request -> request.getServletPath().startsWith("/api/")
                                        || request.getServletPath().startsWith("/chat/")
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login/", "/oauth2/**").permitAll()
                        .requestMatchers("/ws-stomp/**", "/rooms/**").permitAll()
                        .requestMatchers("/api/auth/**", "/api/oauth2/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/reports/inquiries").permitAll()
                        .requestMatchers("/api/users/me", "/api/users/me/**").authenticated()
                        .requestMatchers(HttpMethod.GET,
                                "/api/boards", "/api/boards/**",
                                "/api/study", "/api/study/**",
                                "/api/comments/**",
                                "/api/certificates/schedules",
                                "/api/certificates/data-industry-schedules",
                                "/api/calendar/holidays/**",
                                "/api/users/**"
                        ).permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/notifications/**").authenticated()
                        .requestMatchers("/chat/**").authenticated()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oauth2SuccessHandler)
                        .failureHandler(oauth2FailureHandler)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
