package com.colaborai.colaborai.controller;

import com.colaborai.colaborai.dto.NotificationDTO;
import com.colaborai.colaborai.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/user/{userId}")
    public List<NotificationDTO> getUserNotifications(@PathVariable Long userId) {
        return notificationService.getUserNotifications(userId);
    }

    @GetMapping("/user/{userId}/unread")
    public List<NotificationDTO> getUnreadNotifications(@PathVariable Long userId) {
        return notificationService.getUnreadNotifications(userId);
    }

    @GetMapping("/user/{userId}/unread/count")
    public long getUnreadCount(@PathVariable Long userId) {
        return notificationService.getUnreadCount(userId);
    }

    @PutMapping("/{notificationId}/read")
    public NotificationDTO markAsRead(@PathVariable Long notificationId, @RequestParam Long userId) {
        return notificationService.markAsRead(notificationId, userId);
    }

    @PutMapping("/user/{userId}/read-all")
    public void markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
    }
}
