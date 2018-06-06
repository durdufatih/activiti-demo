package com.arel.activiti.config.security;

import com.arel.activiti.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        AccountCredentials accountCredentials = (AccountCredentials) userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authenticationToken = null;
        if (accountCredentials == null)
            return null;
        else
            authenticationToken = new UsernamePasswordAuthenticationToken(username, password, accountCredentials.getAuthorities());

        authenticationToken.setDetails(accountCredentials);
        if (bCryptPasswordEncoder.matches(password, accountCredentials.getPassword())) {
            return authenticationToken;
        }

        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

}
