package br.com.techchallenge.mecanica.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {
    @AfterEach void clearContext() { SecurityContextHolder.clearContext(); }

    @Test void invalidTokenClearsAuthenticationAndContinuesChain() throws Exception {
        var jwt = mock(JwtService.class);
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var chain = mock(FilterChain.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-test-token");
        when(jwt.extractSubject("invalid-test-token")).thenThrow(new IllegalArgumentException("invalid"));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("previous", null));

        new JwtAuthenticationFilter(jwt).doFilterInternal(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
        verifyNoInteractions(response);
    }
}