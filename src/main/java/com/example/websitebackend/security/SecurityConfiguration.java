package com.example.websitebackend.security;

import com.example.websitebackend.security.bruteforce.BruteForceDefender;
import com.example.websitebackend.security.keycode.KeyCodeAuthenticationFilter;
import com.example.websitebackend.security.keycode.KeyCodeAuthenticationProvider;
import com.example.websitebackend.security.keycode.TokenDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Value("${admin.username:admin}")
    private String adminUsername;

    @Value("${admin.password:admin}")
    private String adminPassword;

    @Value("${token.create.public.token:false}")
    private boolean createPublicToken;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        var udm = new CustomUserDetailsManager(encoder);
        udm.createUser(User.withUsername(adminUsername).password(encoder.encode(adminPassword)).roles("ADMIN").build());
        if (createPublicToken) {
            udm.createToken(new TokenDetails("00000000", "public", LocalDateTime.now()));
        }
        return udm;
    }

    @Bean
    public AuthenticationManager authenticationManager(BruteForceDefender bruteForceDefender) {
        var uds = userDetailsService(passwordEncoder());
        var authenticationProvider = new DaoAuthenticationProvider(uds);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        var tokenProvider = new KeyCodeAuthenticationProvider(uds, passwordEncoder(), bruteForceDefender);
        return new ProviderManager(authenticationProvider, tokenProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationManager authenticationManager,
                                                   NoPopupAuthenticationEntryPoint noPopupEntryPoint) throws Exception {
        var keyCodeAuthenticationFilter = new KeyCodeAuthenticationFilter(authenticationManager);

        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/", "/public/**", "/error/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/authenticate").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/admin/tokens").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/admin/unblock").hasRole("ADMIN")
                        .requestMatchers("/content").authenticated()
                        .requestMatchers(HttpMethod.POST, "/content").hasRole("ADMIN")
                        .requestMatchers("/private/**").authenticated()
                        .anyRequest().denyAll())
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(e -> e.authenticationEntryPoint(noPopupEntryPoint))
                .httpBasic(Customizer.withDefaults())
                .addFilterBefore(keyCodeAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
