package com.example.homologacao.config;

import com.example.homologacao.Service.CustomUserDetailsService;
import com.example.homologacao.Service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtFilter(JwtService jwtService,
                     CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        System.out.println("\n================ JWT FILTER ================");
        System.out.println("PATH: " + path);
        System.out.println("AUTH (início): " + SecurityContextHolder.getContext().getAuthentication());

        // ✅ IGNORA ROTAS PÚBLICAS
        if (path.startsWith("/api/auth") ||
                (path.equals("/api/usuarios") && request.getMethod().equalsIgnoreCase("POST"))) {

            System.out.println("Rota pública ignorada");
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        System.out.println("HEADER AUTHORIZATION: " + authHeader);

        // ✅ Se não tem token, só segue (Spring vai barrar depois)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("Token não enviado");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            String username = jwtService.extrairUsername(token);

            if (username != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(username);

                if (jwtService.tokenValido(token, username)) {

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);

                    System.out.println("AUTH OK");
                } else {
                    System.out.println("TOKEN INVÁLIDO");
                }
            }

        } catch (Exception e) {
            System.out.println("JWT error: " + e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}