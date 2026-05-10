package org.jas.ksinxapp.security;

import lombok.RequiredArgsConstructor;
import org.jas.ksinxapp.model.User;
import org.jas.ksinxapp.repo.UserRepo;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyUserDetailService implements UserDetailsService {

    private final UserRepo userRepo;

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String fullName) throws UsernameNotFoundException {
        User user = userRepo.findByFullName(fullName);

        if (user == null) {
            throw new UsernameNotFoundException(fullName);
        }

        return new UserPrincipal(user);
    }
}
