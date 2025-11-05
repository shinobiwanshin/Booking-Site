package com.capstone.tickets.config;

import com.capstone.tickets.domain.entities.User;
import com.capstone.tickets.repositories.UserRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationConverter implements Converter<Jwt, JwtAuthenticationToken> {

  private final UserRepository userRepository;

  @Override
  public JwtAuthenticationToken convert(Jwt jwt) {
    Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
    return new JwtAuthenticationToken(jwt, authorities);
  }

  private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
    List<GrantedAuthority> authorities = new ArrayList<>();

    // Extract roles from Keycloak realm_access
    Map<String, Object> realmAccess = jwt.getClaim("realm_access");
    if (realmAccess != null && realmAccess.containsKey("roles")) {
      @SuppressWarnings("unchecked")
      List<String> roles = (List<String>) realmAccess.get("roles");

      authorities.addAll(roles.stream()
          .filter(role -> role.startsWith("ROLE_"))
          .map(SimpleGrantedAuthority::new)
          .collect(Collectors.toList()));
    }

    // Also check the user's role from the database
    try {
      String subject = jwt.getSubject();
      UUID userId;

      try {
        userId = UUID.fromString(subject);
      } catch (IllegalArgumentException e) {
        userId = UUID.nameUUIDFromBytes(subject.getBytes());
      }

      // Get user from database and add their role
      userRepository.findById(userId).ifPresent(user -> {
        String roleAuthority = "ROLE_" + user.getRole().name();
        authorities.add(new SimpleGrantedAuthority(roleAuthority));
      });

    } catch (Exception e) {
      // If any error occurs, just use the authorities we already have
    }

    return authorities.isEmpty() ? Collections.emptyList() : authorities;
  }
}
