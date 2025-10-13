package com.hirematch.hirematch_api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hirematch.hirematch_api.entity.UserStats;
import com.hirematch.hirematch_api.entity.Usuario;
import com.hirematch.hirematch_api.repository.UserStatsRepository;
import com.hirematch.hirematch_api.repository.UsuarioRepository;

import java.time.LocalDateTime;

@Service
public class UserStatsService {
    
    @Autowired
    private UserStatsRepository userStatsRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
     public UserStats getUserStats(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
        return userStatsRepository.findByUsuario(usuario)
            .orElseGet(() -> {
                UserStats newStats = new UserStats();
                newStats.setUsuario(usuario);
                // Valores iniciales
                newStats.setLikesRemaining(10); // Por ejemplo, 10 likes gratuitos
                newStats.setSuperlikesRemaining(0);
                newStats.setHasSubscription(false);
                return userStatsRepository.save(newStats);
            });
    }
    
    @Transactional
    public boolean useLike(Long userId) {
        UserStats stats = getUserStats(userId);
        stats.updateSubscriptionStatus();
        
        if (stats.canSendLike()) {
            stats.useLike();
            userStatsRepository.save(stats);
            return true;
        }
        return false;
    }
    
    @Transactional
    public boolean useSuperlike(Long userId) {
        UserStats stats = getUserStats(userId);
        
        if (stats.canSendSuperlike()) {
            stats.useSuperlike();
            userStatsRepository.save(stats);
            return true;
        }
        return false;
    }
    
    @Transactional
    public void addLikes(Long userId, int amount) {
        UserStats stats = getUserStats(userId);
        stats.addLikes(amount);
        userStatsRepository.save(stats);
    }
    
    @Transactional
    public void addSuperlikes(Long userId, int amount) {
        UserStats stats = getUserStats(userId);
        stats.addSuperlikes(amount);
        userStatsRepository.save(stats);
    }
    
    @Transactional
    public void activateSubscription(Long userId, String subscriptionType, int durationInDays) {
        UserStats stats = getUserStats(userId);
        stats.setHasSubscription(true);
        stats.setSubscriptionType(subscriptionType);
        stats.setSubscriptionStartDate(LocalDateTime.now());
        stats.setSubscriptionEndDate(LocalDateTime.now().plusDays(durationInDays));
        userStatsRepository.save(stats);
    }
}