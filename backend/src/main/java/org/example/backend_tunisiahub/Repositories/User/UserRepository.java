package org.example.backend_tunisiahub.Repositories.User;

import org.example.backend_tunisiahub.Entities.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    boolean existsByEmail(String email);

}