package com.notifplatform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String phone;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters
    public Long getId()            { return id; }
    public String getName()        { return name; }
    public String getEmail()       { return email; }
    public String getPhone()       { return phone; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setId(Long id)          { this.id = id; }
    public void setName(String name)    { this.name = name; }
    public void setEmail(String email)  { this.email = email; }
    public void setPhone(String phone)  { this.phone = phone; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}