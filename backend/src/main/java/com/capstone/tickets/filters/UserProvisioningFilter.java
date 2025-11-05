package com.capstone.tickets.filters;

import com.capstone.tickets.domain.entities.User;
import com.capstone.tickets.domain.enums.Role;
import com.capstone.tickets.repositories.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
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

        // Extract role from JWT attributes
        Role role = extractRoleFromJwt(jwt);
        user.setRole(role);

        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
      } else {
        // User already exists - this means they were registered via the custom form
        // The role is already set in the database, so we don't need to do anything
        // The existing user record will be used as-is
      }

    }

    filterChain.doFilter(request, response);
  }

  private Role extractRoleFromJwt(Jwt jwt) {
    try {
      // Try to get role from attributes claim
      Object attributesObj = jwt.getClaim("attributes");
      if (attributesObj instanceof java.util.Map) {
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> attributes = (java.util.Map<String, Object>) attributesObj;
        Object roleObj = attributes.get("role");

        if (roleObj instanceof List) {
          @SuppressWarnings("unchecked")
          List<String> roleList = (List<String>) roleObj;
          if (!roleList.isEmpty()) {
            String roleStr = roleList.get(0);
            return Role.valueOf(roleStr.toUpperCase());
          }
        } else if (roleObj instanceof String) {
          return Role.valueOf(((String) roleObj).toUpperCase());
        }
      }

      // Default to ATTENDEE if no role found
      return Role.ATTENDEE;

    } catch (Exception e) {
      // If any error occurs, default to ATTENDEE
      return Role.ATTENDEE;
    }
  }
}
