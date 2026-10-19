package com.aiintelligence.config;

import java.time.Clock;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

@Configuration
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService users(@Value("${admin.username:admin}") String username,
            @Value("${admin.password:admin}") String password, PasswordEncoder encoder) {
        UserDetails user = User.withUsername(username).password(encoder.encode(password)).roles("ADMIN").build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    TotpVerifier totpVerifier(@Value("${admin.totp-secret:}") String secret) {
        return new TotpVerifier(secret, Clock.systemUTC());
    }

    @Bean
    AuthenticationDetailsSource<HttpServletRequest, TotpAuthenticationDetails> totpAuthenticationDetailsSource() {
        return TotpAuthenticationDetails::new;
    }

    @Bean
    AuthenticationManager authenticationManager(UserDetailsService users, PasswordEncoder encoder,
            TotpVerifier totpVerifier) {
        TotpAuthenticationProvider provider = new TotpAuthenticationProvider(totpVerifier);
        provider.setUserDetailsService(users);
        provider.setPasswordEncoder(encoder);
        return new ProviderManager(Collections.singletonList(provider));
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager,
            AuthenticationDetailsSource<HttpServletRequest, TotpAuthenticationDetails> detailsSource) throws Exception {
        return http.authenticationManager(authenticationManager)
                .authorizeRequests(authorize -> authorize.antMatchers("/admin", "/admin/**").hasRole("ADMIN")
                        .anyRequest().permitAll())
                .formLogin(form -> form.loginPage("/login").authenticationDetailsSource(detailsSource)
                        .defaultSuccessUrl("/admin", true).failureUrl("/login?error").permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/login?logout"))
                .csrf().disable().build();
    }

    private static final class TotpAuthenticationDetails extends WebAuthenticationDetails {
        private static final long serialVersionUID = 1L;
        private final String code;

        private TotpAuthenticationDetails(HttpServletRequest request) {
            super(request);
            this.code = request.getParameter("totp");
        }
    }

    private static final class TotpAuthenticationProvider extends DaoAuthenticationProvider {
        private final TotpVerifier totpVerifier;

        private TotpAuthenticationProvider(TotpVerifier totpVerifier) {
            this.totpVerifier = totpVerifier;
        }

        @Override
        protected void additionalAuthenticationChecks(UserDetails userDetails,
                UsernamePasswordAuthenticationToken authentication) {
            super.additionalAuthenticationChecks(userDetails, authentication);
            Object details = authentication.getDetails();
            if (!(details instanceof TotpAuthenticationDetails)
                    || !totpVerifier.verify(((TotpAuthenticationDetails) details).code)) {
                throw new BadCredentialsException("Invalid username, password, or verification code");
            }
        }
    }
}
