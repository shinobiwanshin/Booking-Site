package com.capstone.tickets.filters;

import com.capstone.tickets.domain.entities.User;
import com.capstone.tickets.repositories.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class UserProvisioningFilter extends OncePerRequestFilter {

  private final UserRepository userRepository;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication != null
        && authentication.isAuthenticated()
        && authentication.getPrincipal() instanceof Jwt jwt) {

      String subject = jwt.getSubject();
      UUID userId;

      // Try to parse subject as UUID; if it fails, generate a deterministic UUID from
      // the subject
      try {
        userId = UUID.fromString(subject);
      } catch (IllegalArgumentException e) {
        // Subject is not a UUID (e.g., email or username from OAuth provider)
        // Generate a deterministic UUID v5 from the subject string
        userId = UUID.nameUUIDFromBytes(subject.getBytes());
      }

      if (!userRepository.existsById(userId)) {

        User user = new User();
        user.setId(userId);
        user.setName(jwt.getClaimAsString("preferred_username"));
        user.setEmail(jwt.getClaimAsString("email"));

        userRepository.save(user);
      }

    }

    filterChain.doFilter(request, response);
  }
}
