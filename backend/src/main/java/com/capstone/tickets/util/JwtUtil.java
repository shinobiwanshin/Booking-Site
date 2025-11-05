package com.capstone.tickets.util;

import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;

public final class JwtUtil {
  private JwtUtil() {
  }

  public static UUID parseUserId(Jwt jwt) {
    String subject = jwt.getSubject();

    // Try to parse subject as UUID; if it fails, generate a deterministic UUID from
    // the subject
    try {
      return UUID.fromString(subject);
    } catch (IllegalArgumentException e) {
      // Subject is not a UUID (e.g., email or username from OAuth provider)
      // Generate a deterministic UUID v5 from the subject string
      return UUID.nameUUIDFromBytes(subject.getBytes());
    }
  }

}
