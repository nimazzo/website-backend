package com.example.websitebackend.security;

import com.example.websitebackend.security.bruteforce.BruteForceDefender;
import com.example.websitebackend.security.db.TokenRepository;
import com.example.websitebackend.security.keycode.KeyCodeAuthenticationFilter;
import com.example.websitebackend.security.keycode.KeyCodeAuthenticationProvider;
import com.example.websitebackend.security.keycode.TokenDetails;
import com.example.websitebackend.security.tracking.AuthenticationTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.UUID;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfiguration.class);

    private final SecurityProperties securityProperties;

    public SecurityConfiguration(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder, TokenRepository tokenRepository, DataSource dataSource) {
        var udm = new CustomUserDetailsManager(dataSource, encoder, tokenRepository);

        if (!udm.userExists(securityProperties.adminUsername())) {
            var adminPassword = securityProperties.adminPassword();
            if (adminPassword == null || adminPassword.isBlank()) {
                log.warn("Admin password is not set. Creating a random password.");
                adminPassword = UUID.randomUUID().toString();
                System.out.println(adminPassword);
            }

            udm.createUser(User
                    .withUsername(securityProperties.adminUsername())
                    .password(encoder.encode(adminPassword))
                    .roles("ADMIN")
                    .build());
        }

        if (!udm.tokenExists("00000000")) {
            if (securityProperties.createPublicToken()) {
                udm.createToken(new TokenDetails("00000000", "public", LocalDateTime.now()));
            }
        }

        return udm;
    }

    @Bean
    public AuthenticationManager authenticationManager(BruteForceDefender bruteForceDefender,
                                                       UserDetailsService uds,
                                                       AuthenticationTracker authenticationTracker) {
        var authenticationProvider = new DaoAuthenticationProvider(uds);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        var tokenProvider = new KeyCodeAuthenticationProvider(uds, passwordEncoder(), bruteForceDefender, authenticationTracker);
        return new ProviderManager(authenticationProvider, tokenProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationManager authenticationManager,
                                                   NoPopupAuthenticationEntryPoint noPopupEntryPoint,
                                                   Environment env) throws Exception {
        var keyCodeAuthenticationFilter = new KeyCodeAuthenticationFilter(authenticationManager);

        http
                .authorizeHttpRequests((auth) -> {
                    auth
                            .requestMatchers(HttpMethod.GET, "/", "/public/**", "/error/**").permitAll()
                            .requestMatchers(HttpMethod.POST, "/login").permitAll()
                            .requestMatchers("/admin/**").hasRole("ADMIN")
                            .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ADMIN")
                            .requestMatchers("/private/**").authenticated();

                    if (env.acceptsProfiles(Profiles.of("dev"))) {
                        auth.requestMatchers("/h2-console/**").permitAll();
                    }

                    auth.anyRequest().denyAll();
                })
                .headers(headers -> {
                    if (env.acceptsProfiles(Profiles.of("dev"))) {
                        headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin);
                    }
                })
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(e -> e.authenticationEntryPoint(noPopupEntryPoint))
                .httpBasic(Customizer.withDefaults())
                .logout(logout -> logout.logoutSuccessUrl("/public/index.html"))
                .addFilterBefore(keyCodeAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
