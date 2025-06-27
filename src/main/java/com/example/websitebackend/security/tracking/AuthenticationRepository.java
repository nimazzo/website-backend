package com.example.websitebackend.security.tracking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthenticationRepository extends JpaRepository<Authentication, Integer> {

    List<Authentication> findBySuccessful(Boolean successful, Sort sort);
}
