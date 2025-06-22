package com.example.websitebackend.security.db;

import com.example.websitebackend.security.keycode.TokenDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<Token, String> {
    TokenDetails findByOwner(String owner);
}
