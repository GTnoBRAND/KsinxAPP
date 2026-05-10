package org.jas.ksinxapp.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jas.ksinxapp.security.MyUserDetailService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AuthTokenFilter extends OncePerRequestFilter {

    public static final String BEARER_ = "Bearer ";
    private JwtService jwtService;
    private MyUserDetailService myUserDetailService;


    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if(headerAuth != null && headerAuth.startsWith(BEARER_)){
            return headerAuth.substring(BEARER_.length());
        }
        return null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try{
            String jwt = parseJwt(request);
            if(jwt!= null && jwtService.validateJwtToken(jwt)){
                String username = jwtService.getUserFromToken(jwt);     //get fullname from the request
                UserDetails userDetails = myUserDetailService.loadUserByUsername(username);     //fetch user from db and compares with the user from the request
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken( //security pass for this request
                        userDetails,    //who they are(userDetails),
                        null,   //no password needed token already provided identity
                        userDetails.getAuthorities()    //their permissions(their role)
                );auth.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)      //attaches ip address and session id
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }filterChain.doFilter(request, response);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
}
