/*
package edu.udg.tfg.Eureka;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Extract JWT token from the request header
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        */
/*if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwtToken = authorizationHeader.substring(7); // Remove "Bearer " prefix

            // Validate the JWT token
            try {
                if (!jwtTokenUtil.validateToken(jwtToken)) {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    return;
                }
            } catch (Exception e) {
                // If token is invalid or expired, return unauthorized response
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return;
            }
        } else {
            // If no JWT token found in the header, return unauthorized response
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }*//*


        // Continue filter chain
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        RequestMatcher matcher = new NegatedRequestMatcher(new AntPathRequestMatcher("/service-instances/**"));
        return matcher.matches(request);
    }
}
*/
