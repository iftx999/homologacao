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

        System.out.println("\n================ JWT FILTER ================");
        System.out.println("PATH: " + request.getServletPath());
        System.out.println("AUTH (início): " + SecurityContextHolder.getContext().getAuthentication());

        // 🔓 Ignora login
        if (request.getServletPath().startsWith("/auth")) {
            System.out.println("🔓 Rota /auth ignorada");
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        System.out.println("HEADER AUTHORIZATION: " + authHeader);

        // ❌ Sem token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("❌ Token não enviado ou inválido");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        System.out.println("TOKEN RECEBIDO: " + token);

        try {
            String username = null;

            try {
                username = jwtService.extrairUsername(token);
                System.out.println("USERNAME EXTRAIDO DO TOKEN: " + username);
            } catch (Exception e) {
                System.out.println("💥 ERRO AO EXTRAIR USERNAME: " + e.getMessage());
            }

            if (username != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                System.out.println("🔍 Buscando usuário no banco...");

                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(username);

                System.out.println("USERDETAILS USERNAME: " + userDetails.getUsername());
                System.out.println("AUTHORITIES: " + userDetails.getAuthorities());

                boolean tokenValido = false;

                try {
                    tokenValido = jwtService.tokenValido(token, userDetails.getUsername());
                    System.out.println("TOKEN VALIDO: " + tokenValido);
                } catch (Exception e) {
                    System.out.println("💥 ERRO AO VALIDAR TOKEN: " + e.getMessage());
                }

                // 🔥 Autenticação
                if (tokenValido) {

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(auth);

                    System.out.println("✅ AUTHENTICATED: " + SecurityContextHolder.getContext().getAuthentication());
                } else {
                    System.out.println("❌ TOKEN INVÁLIDO - NÃO AUTENTICOU");
                }

            } else {
                System.out.println("❌ Username nulo ou já autenticado");
            }

        } catch (JwtException e) {
            System.out.println("💥 EXCEPTION JWT: " + e.getMessage());
            SecurityContextHolder.clearContext();
        }

        System.out.println("AUTH (final): " + SecurityContextHolder.getContext().getAuthentication());
        System.out.println("============================================\n");

        filterChain.doFilter(request, response);
    }
}