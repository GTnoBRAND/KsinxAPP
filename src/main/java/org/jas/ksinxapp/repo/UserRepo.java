package org.jas.ksinxapp.repo;

import org.jas.ksinxapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<User,Long> {

    boolean existsByEmail(String email);
}
