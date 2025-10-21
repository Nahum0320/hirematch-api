package com.hirematch.hirematch_api.tasks;

import com.hirematch.hirematch_api.entity.UserStats;
import com.hirematch.hirematch_api.repository.UserStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DailyLikesResetTask {
    @Autowired
    private UserStatsRepository userStatsRepository;

    // Ejecuta todos los días a las 3:00 AM
    @Scheduled(cron = "0 0 3 * * *")
    public void resetLikesForAllUsers() {
        List<UserStats> allStats = userStatsRepository.findAll();
        int resetCount = 0;
        for (UserStats stats : allStats) {
            if (stats.getLikesRemaining() < 5) {
                stats.setLikesRemaining(5);
                userStatsRepository.save(stats);
                resetCount++;
            }
        }
    }
}
