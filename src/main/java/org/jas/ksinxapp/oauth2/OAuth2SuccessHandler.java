package org.jas.ksinxapp.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jas.ksinxapp.jwt.JwtService;
import org.jas.ksinxapp.model.User;
import org.jas.ksinxapp.repo.UserRepo;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepo userRepo;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email    = oAuth2User.getAttribute("email");
        String fullName = oAuth2User.getAttribute("name");

        User user = userRepo.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFullName(fullName != null ? fullName : email);
            newUser.setRole(User.Role.STUDENT);
            newUser.setPassword("");
            return userRepo.save(newUser);
        });

        String token = jwtService.generateToken( user.getRole(), user.getEmail());

        String encodedName = URLEncoder.encode(user.getFullName(), StandardCharsets.UTF_8);
        String encodedRole = URLEncoder.encode(user.getRole().name(), StandardCharsets.UTF_8);
        String encodedId   = URLEncoder.encode(String.valueOf(user.getId()), StandardCharsets.UTF_8);

        response.sendRedirect("/oauth-callback.html"
                + "?token=" + token
                + "&name="  + encodedName
                + "&role="  + encodedRole
                + "&id="    + encodedId);
    }
}
