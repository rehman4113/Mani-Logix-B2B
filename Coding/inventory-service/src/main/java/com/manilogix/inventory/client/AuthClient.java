package com.manilogix.inventory.client;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class AuthClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String AUTH_VALIDATE_URL = "http://localhost:8081/api/auth/validate";

    /**
     * Validates the token and returns user details like email and roles.
     * Returns null if the token is invalid or auth service fails.
     */
    public Map<String, Object> getUserDetailsFromToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = Collections.singletonMap("token", token);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(AUTH_VALIDATE_URL, entity, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody(); // Should contain fields like "email", "roles"
            }
        } catch (Exception ex) {
            System.out.println("Auth Service validation failed: " + ex.getMessage());
        }

        return null;
    }

    /**
     * Extracts the "roles" list from the user details returned by validate call.
     * Returns null if token is invalid or roles not present.
     */
    public List<String> getUserRolesFromToken(String token) {
        Map<String, Object> userDetails = getUserDetailsFromToken(token);
        if (userDetails == null || !userDetails.containsKey("roles")) return null;

        Object rolesObj = userDetails.get("roles");

        if (rolesObj instanceof List<?>) {
            return ((List<?>) rolesObj).stream()
                    .map(String::valueOf)
                    .toList();
        } else if (rolesObj instanceof String roleString) {
            return Collections.singletonList(roleString);
        }

        return null;
    }
}
