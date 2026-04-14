package org.jas.ksinxapp.security;

import org.jas.ksinxapp.model.User;
import org.jas.ksinxapp.repo.UserRepo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailService implements UserDetailsService {

    private final UserRepo userRepo;
    public MyUserDetailService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String fullName) throws UsernameNotFoundException {
        User user = userRepo.findByFullName(fullName);

        if (user == null) {
            throw new UsernameNotFoundException(fullName);
        }

        return new UserPrincipal(user);
    }
}
