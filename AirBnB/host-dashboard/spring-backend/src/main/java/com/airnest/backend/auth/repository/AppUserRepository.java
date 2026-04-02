package com.airnest.backend.auth.repository;

import com.airnest.backend.auth.entity.AppUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmailIgnoreCase(String email);

    Optional<AppUser> findByIdAndActiveTrue(Long id);

    boolean existsByEmailIgnoreCase(String email);
}
