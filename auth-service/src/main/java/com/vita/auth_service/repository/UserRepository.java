package com.vita.auth_service.repository;

import com.vita.auth_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Optional<User> findByRoleName(@Param("roleName") String roleName);

    @Query("SELECT COUNT(u) > 0 FROM User u JOIN u.roles r WHERE r.name = :roleName")
    boolean existsByRoleName(@Param("roleName") String roleName);
}
