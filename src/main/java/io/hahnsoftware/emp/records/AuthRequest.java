package io.hahnsoftware.emp.records;

public record AuthRequest(String username, String password) {
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}

