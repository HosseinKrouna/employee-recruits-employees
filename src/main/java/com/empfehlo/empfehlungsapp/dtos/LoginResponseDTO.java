package com.empfehlo.empfehlungsapp.dtos;

public class LoginResponseDTO {
    private Long id;
    private String username;
    private String role;

    // Konstruktoren
    public LoginResponseDTO() {}

    public LoginResponseDTO(Long id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
