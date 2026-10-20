package com.manilogix.modulith.auth;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
@Component
public class CurrentUser {
 public long id() {
  Authentication auth = SecurityContextHolder.getContext().getAuthentication();
  if (auth == null || !auth.isAuthenticated()) throw new AccessDeniedException("Authentication required");
  try { return Long.parseLong(auth.getName()); }
  catch (NumberFormatException ex) { throw new AccessDeniedException("Invalid principal"); }
 }
 public boolean hasRole(String role) {
  Authentication auth = SecurityContextHolder.getContext().getAuthentication();
  return auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
 }
}
