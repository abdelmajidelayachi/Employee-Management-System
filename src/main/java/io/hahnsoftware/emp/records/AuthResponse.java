package io.hahnsoftware.emp.records;

public record AuthResponse(String token) {
    public String getToken() {
        return token;
    }
}
