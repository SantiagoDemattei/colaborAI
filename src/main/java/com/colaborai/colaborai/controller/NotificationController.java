package com.colaborai.colaborai.controller;

import com.colaborai.colaborai.dto.NotificationDTO;
import com.colaborai.colaborai.security.annotation.RequireOwnership;
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

    @RequireOwnership(userIdParam = "userId", message = "No tienes permisos para ver estas notificaciones")
    @GetMapping("/user/{userId}")
    public List<NotificationDTO> getUserNotifications(@PathVariable Long userId) {
        return notificationService.getUserNotifications(userId);
    }

    @RequireOwnership(userIdParam = "userId", message = "No tienes permisos para ver estas notificaciones")
    @GetMapping("/user/{userId}/unread")
    public List<NotificationDTO> getUnreadNotifications(@PathVariable Long userId) {
        return notificationService.getUnreadNotifications(userId);
    }

    @RequireOwnership(userIdParam = "userId", message = "No tienes permisos para ver el contador de notificaciones")
    @GetMapping("/user/{userId}/unread/count")
    public long getUnreadCount(@PathVariable Long userId) {
        return notificationService.getUnreadCount(userId);
    }

    @RequireOwnership(userIdParam = "userId", message = "No tienes permisos para marcar esta notificación")
    @PutMapping("/{notificationId}/read")
    public NotificationDTO markAsRead(@PathVariable Long notificationId, @RequestParam Long userId) {
        return notificationService.markAsRead(notificationId, userId);
    }

    @RequireOwnership(userIdParam = "userId", message = "No tienes permisos para marcar estas notificaciones")
    @PutMapping("/user/{userId}/read-all")
    public void markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
    }
}
