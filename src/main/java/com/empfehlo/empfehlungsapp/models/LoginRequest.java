package com.empfehlo.empfehlungsapp.models;

public class LoginRequest {
    private String username;
    private String password;

    // Getter und Setter
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}

