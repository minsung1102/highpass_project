package com.example.highpass_backend.security;

import com.example.highpass_backend.config.CookieUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CookieUtils cookieUtils;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = cookieUtils.getCookieValue(request, "access_token");

        if (token != null && jwtTokenProvider.validateToken(token)) {
            if ("access".equals(jwtTokenProvider.getTokenType(token))) {
                SecurityContextHolder.getContext()
                        .setAuthentication(jwtTokenProvider.getAuthentication(token));
            }
        }

        filterChain.doFilter(request, response);
    }
}