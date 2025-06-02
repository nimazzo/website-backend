package com.example.websitebackend;

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
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    private UserDetailsService userDetailsService(PasswordEncoder encoder) {
        return new InMemoryUserDetailsManager(
                User.withUsername("admin").password(encoder.encode("admin")).roles("ADMIN").build(),
                User.withUsername("00000000").password(encoder.encode("00000000")).roles("USER").build()
        );
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
                        .anyRequest().authenticated())
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(e -> e.authenticationEntryPoint(noPopupEntryPoint))
                .httpBasic(Customizer.withDefaults())
                .addFilterBefore(keyCodeAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
