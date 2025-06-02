package com.example.websitebackend.controllers;

import com.example.websitebackend.security.CustomUserDetailsManager;
import com.example.websitebackend.security.keycode.TokenDetails;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class AdminController {

    private final CustomUserDetailsManager userDetailsManager;

    public AdminController(UserDetailsService userDetailsService) {
        this.userDetailsManager = (CustomUserDetailsManager) userDetailsService;
    }

    public record TokenRequest(String token, String owner) {
    }

    @PutMapping("/admin/tokens")
    public ResponseEntity<?> addTokens(@RequestBody List<TokenRequest> tokens) throws Exception {
        validateTokenRequest(tokens);

        for (var tokenRequest : tokens) {
            userDetailsManager.createToken(new TokenDetails(tokenRequest.token, tokenRequest.owner, LocalDateTime.now()));
        }
        var response = userDetailsManager.getAllTokens();
        return ResponseEntity.ok(response);
    }

    private void validateTokenRequest(List<TokenRequest> tokens) throws BadRequestException {
        for (var tokenRequest : tokens) {
            if (tokenRequest.token == null || !tokenRequest.token.matches("\\d{8}")) {
                throw new BadRequestException("Token must not be null or blank and must be exactly 8 digits long.");
            }
            if (tokenRequest.owner == null || tokenRequest.owner.isBlank()) {
                throw new BadRequestException("Owner must not be null or blank.");
            }
        }
    }

}
