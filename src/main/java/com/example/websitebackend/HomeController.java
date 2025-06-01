package com.example.websitebackend;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping
    public String home(Authentication auth) {
        var authenticated = auth != null && auth.isAuthenticated() && !auth.getName().equals("userAnonymousUser");
        return authenticated ? String.format("Authenticated (%s)", auth.getName()) :
                String.format("Not Authenticated (%s)", auth != null ? auth.getName() : "null");
    }

}
