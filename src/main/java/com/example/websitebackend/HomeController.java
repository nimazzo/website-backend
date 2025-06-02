package com.example.websitebackend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    @GetMapping
    public String home(Authentication auth) {
        var authenticated = auth != null && auth.isAuthenticated() && !auth.getName().equals("userAnonymousUser");
        log.info(authenticated ? String.format("Authenticated (%s)", auth.getName()) :
                String.format("Not Authenticated (%s)", auth != null ? auth.getName() : "null"));

        return authenticated ? "redirect:/private/index.html" : "redirect:/public/index.html";
    }

}
