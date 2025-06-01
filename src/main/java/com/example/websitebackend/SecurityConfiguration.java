package com.example.websitebackend;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.NullSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final MyCustomAuthenticationProvider customAuthProvider;

    public SecurityConfiguration(MyCustomAuthenticationProvider customAuthProvider) {
        this.customAuthProvider = customAuthProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(Collections.singletonList(customAuthProvider));
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new NullSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(customAuthProvider)
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/").permitAll()
                        .requestMatchers(HttpMethod.POST, "/authenticate").permitAll()
                        .anyRequest().authenticated())
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(new KeyCodeAuthenticationFilter(authenticationManager()), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

}
