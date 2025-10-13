package com.hirematch.hirematch_api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_stats")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "usuario_id") // Cambiado de user_id a usuario_id
    private Usuario usuario; // Cambiado de User a Usuario

    private Integer likesRemaining = 0;
    private Integer superlikesRemaining = 0;
    
    private Boolean hasSubscription = false;
    private String subscriptionType; // "BASIC", "PREMIUM", "GOLD", etc.
    private LocalDateTime subscriptionStartDate;
    private LocalDateTime subscriptionEndDate;

    // Métodos para manejar likes y superlikes
    public boolean canSendLike() {
        return likesRemaining > 0 || hasSubscription;
    }

    public boolean canSendSuperlike() {
        return superlikesRemaining > 0;
    }

    public void useLike() {
        if (!hasSubscription && likesRemaining > 0) {
            likesRemaining--;
        }
    }

    public void useSuperlike() {
        if (superlikesRemaining > 0) {
            superlikesRemaining--;
        }
    }

    public void addLikes(int amount) {
        this.likesRemaining += amount;
    }

    public void addSuperlikes(int amount) {
        this.superlikesRemaining += amount;
    }

    public boolean isSubscriptionActive() {
        return hasSubscription && LocalDateTime.now().isBefore(subscriptionEndDate);
    }

    public void updateSubscriptionStatus() {
        if (hasSubscription && LocalDateTime.now().isAfter(subscriptionEndDate)) {
            hasSubscription = false;
        }
    }
}