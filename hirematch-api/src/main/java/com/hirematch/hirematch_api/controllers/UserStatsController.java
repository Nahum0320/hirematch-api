package com.hirematch.hirematch_api.controllers;

import com.hirematch.hirematch_api.entity.UserStats;
import com.hirematch.hirematch_api.service.UserStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user-stats")
public class UserStatsController {

    @Autowired
    private UserStatsService userStatsService;
    
    @GetMapping("/{userId}")
    public ResponseEntity<UserStats> getUserStats(@PathVariable Long userId) {
        UserStats stats = userStatsService.getUserStats(userId);
        return ResponseEntity.ok(stats);
    }
}