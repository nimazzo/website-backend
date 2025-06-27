package com.example.websitebackend.admin;

import com.example.websitebackend.content.ContentService;
import com.example.websitebackend.security.CustomUserDetailsManager;
import com.example.websitebackend.security.bruteforce.BruteForceDefender;
import com.example.websitebackend.security.keycode.TokenDetails;
import com.example.websitebackend.security.tracking.Authentication;
import com.example.websitebackend.security.tracking.AuthenticationTracker;
import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final CustomUserDetailsManager userDetailsManager;
    private final ContentService contentService;
    private final BruteForceDefender bruteForceDefender;
    private final AuthenticationTracker authenticationTracker;

    public AdminController(UserDetailsService userDetailsService, ContentService contentService, BruteForceDefender bruteForceDefender, AuthenticationTracker authenticationTracker) {
        this.userDetailsManager = (CustomUserDetailsManager) userDetailsService;
        this.contentService = contentService;
        this.bruteForceDefender = bruteForceDefender;
        this.authenticationTracker = authenticationTracker;
    }

    public record TokenRequest(String token, String owner) {
    }

    @GetMapping("/admin/tokens")
    public ResponseEntity<?> getTokens() {
        var response = userDetailsManager.getAllTokens();
        log.info("Returning all tokens: {}", response);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/admin/tokens")
    public ResponseEntity<?> addTokens(@RequestBody List<TokenRequest> tokens) throws Exception {
        log.info("Received token requests: {}. Validating...", tokens);
        validateTokenRequest(tokens);
        log.info("Token requests validated successfully. Creating tokens...");

        for (var tokenRequest : tokens) {
            userDetailsManager.createToken(new TokenDetails(tokenRequest.token, tokenRequest.owner, LocalDateTime.now()));
        }
        var response = userDetailsManager.getAllTokens();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/tokens/add")
    public ResponseEntity<?> createTokenForOwner(@RequestParam("owner") String owner) {
        log.info("Creating token for owner: {}", owner);
        if (owner == null || owner.isBlank()) {
            log.error("Owner must not be null or blank.");
            return ResponseEntity.badRequest().body("Owner must not be null or blank.");
        }

        if (userDetailsManager.getTokenByOwner(owner) != null) {
            log.error("Token already exists for owner: {}", owner);
            return ResponseEntity.badRequest().body("Token already exists for owner: " + owner);
        }

        var token = userDetailsManager.createTokenForOwner(owner);
        log.info("Token created successfully: {}", token);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/admin/unblock")
    public ResponseEntity<?> unblockAllUsers() {
        log.info("Unblocking all users...");
        bruteForceDefender.unblockAllUsers();
        return ResponseEntity.ok().build();
    }


    @PostMapping("/admin/content")
    public ResponseEntity<Void> setContent(@RequestParam("content") MultipartFile contentFile) {
        contentService.setContent(contentFile);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/authentications")
    public List<Authentication> getAllAuthentications(@RequestParam(value = "successful", required = false) Boolean successful) {
        if (successful != null) {
            return authenticationTracker.getAuthenticationsBySuccess(successful);
        }
        return authenticationTracker.getAllAuthentications();
    }

    private void validateTokenRequest(List<TokenRequest> tokens) throws BadRequestException {
        for (var tokenRequest : tokens) {
            if (tokenRequest.token == null || !tokenRequest.token.matches("\\d{8}")) {
                log.error("Invalid token: {}", tokenRequest.token);
                throw new BadRequestException("Token must not be null or blank and must be exactly 8 digits long.");
            }
            if (tokenRequest.owner == null || tokenRequest.owner.isBlank()) {
                log.error("Invalid owner: {}", tokenRequest.owner);
                throw new BadRequestException("Owner must not be null or blank.");
            }
            if (userDetailsManager.tokenExists(tokenRequest.token)) {
                log.error("Token already exists: {}", tokenRequest.token);
                throw new BadRequestException("Token already exists: " + tokenRequest.token);
            }
        }
    }

}
