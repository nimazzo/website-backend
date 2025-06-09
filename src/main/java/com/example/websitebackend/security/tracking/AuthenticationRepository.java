package com.example.websitebackend.security.tracking;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthenticationRepository extends JpaRepository<Authentication, Integer> {
}
