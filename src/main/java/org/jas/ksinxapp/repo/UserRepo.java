package org.jas.ksinxapp.repo;

import org.jas.ksinxapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User,Long> {

    boolean existsByEmail(String email);

    boolean existsByRole(User.Role role);

    long countByRole(User.Role role);

    User findByFullName(String fullName);

    Optional<User> findByEmail(String email);
}
