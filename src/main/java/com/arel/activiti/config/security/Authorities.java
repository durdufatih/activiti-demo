package com.arel.activiti.config.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public interface Authorities {
    public static final GrantedAuthority ADMINISTRATOR = new SimpleGrantedAuthority("Administrator");
    public static final GrantedAuthority MAGAZINE_PUBLISHER = new SimpleGrantedAuthority("MagazinePublisher");
    public static final GrantedAuthority EDITOR = new SimpleGrantedAuthority("Editor");
    public static final GrantedAuthority AUDITOR = new SimpleGrantedAuthority("Auditor");
    public static final GrantedAuthority NEWSPAPER_PUBLISHER = new SimpleGrantedAuthority("NewspaperPublisher");
}
