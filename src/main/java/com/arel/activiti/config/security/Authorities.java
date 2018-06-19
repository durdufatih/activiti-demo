package com.arel.activiti.config.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public interface Authorities {
    GrantedAuthority ADMINISTRATOR = new SimpleGrantedAuthority("Administrator");
    GrantedAuthority CHIEF= new SimpleGrantedAuthority("Chief");
    GrantedAuthority OFFICER = new SimpleGrantedAuthority("Officer");
}
