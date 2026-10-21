
package com.debarag.stockpulse.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "whatsapp_phone", length = 20)
    private String whatsappPhone;

    @Column(name = "whatsapp_enabled", nullable = false)
    private boolean whatsappEnabled = false;

    @Column(
            name = "created_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    protected User() {
        // Required by JPA
    }

    public User(String email, String passwordHash) {
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getWhatsappPhone() {
        return whatsappPhone;
    }

    public boolean isWhatsappEnabled() {
        return whatsappEnabled;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setWhatsappPhone(String whatsappPhone) {
        this.whatsappPhone = whatsappPhone;
    }

    public void setWhatsappEnabled(boolean whatsappEnabled) {
        this.whatsappEnabled = whatsappEnabled;
    }
}
