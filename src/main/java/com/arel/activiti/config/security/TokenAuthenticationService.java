package com.arel.activiti.config.security;

import com.arel.activiti.model.model.LoginResponse;
import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


class TokenAuthenticationService {
    static final long EXPIRATIONTIME = 864_000_000; // 10 days
    static final String SECRET = "ThisIsASecret";
    static final String TOKEN_PREFIX = "Bearer";
    static final String HEADER_STRING = "Authorization";


    static void addAuthentication(HttpServletResponse res, Authentication auth) throws IOException {

        Claims claims = Jwts.claims().setSubject(auth.getName());
        claims.put("scopes", auth.getAuthorities().stream().map(s -> s.toString()).collect(Collectors.toList()));

        AccountCredentials userDetails = (AccountCredentials) auth.getDetails();
        String JWT = Jwts.builder()
                .setClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATIONTIME))
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();

        res.addHeader(HEADER_STRING, TOKEN_PREFIX + " " + JWT);
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setRoles(auth.getAuthorities().stream().map(s -> s.toString()).collect(Collectors.toList()));
        loginResponse.setName(userDetails.getName());
        loginResponse.setToken(JWT);
        Gson gson = new Gson();
        String loginJson = gson.toJson(loginResponse);
        res.setContentType("application/json");
        res.getWriter().write(loginJson);
    }

    static Authentication getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(HEADER_STRING);
        if (token != null) {

            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
                    .getBody();

            String user = claims.getSubject();
            List<String> authorities = (List<String>) claims.get("scopes");

            Collection<GrantedAuthority> grantedAuthories = Collections.EMPTY_LIST;
            if (!authorities.isEmpty()) {
                grantedAuthories = authorities.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
            }
            return user != null ?
                    new UsernamePasswordAuthenticationToken(user, null, grantedAuthories) :
                    null;
        }
        return null;
    }
}