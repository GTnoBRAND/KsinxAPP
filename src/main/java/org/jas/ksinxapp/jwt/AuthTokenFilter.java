package org.jas.ksinxapp.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jas.ksinxapp.security.MyUserDetailService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    public static final String BEARER_ = "Bearer ";
    private JwtService jwtService;
    private MyUserDetailService myUserDetailService;

    public AuthTokenFilter(JwtService jwtService,  MyUserDetailService myUserDetailService) {
        this.jwtService = jwtService;
        this.myUserDetailService = myUserDetailService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try{
            String jwt = parseJwt(request);
            if(jwt != null && jwtService.validateJwtToken(jwt)){
                final String username = jwtService.getUserFromToken(jwt);
                final UserDetails userDetails = myUserDetailService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());
                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request));
                SecurityContextHolder.getContext()
                        .setAuthentication(authenticationToken);

                System.out.println("--- SECURITY DEBUG ---");
                System.out.println("User: " + username);
                System.out.println("Authorities in Context: " + SecurityContextHolder.getContext().getAuthentication().getAuthorities());
            }
        }catch(Exception e){
            System.out.println("Jwt validation error: "+e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if(headerAuth != null && headerAuth.startsWith(BEARER_)){
            return headerAuth.substring(BEARER_.length());
        }
        return null;
    }
}
