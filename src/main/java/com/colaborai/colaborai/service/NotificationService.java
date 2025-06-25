package com.colaborai.colaborai.service;

import com.colaborai.colaborai.dto.NotificationDTO;
import java.util.List;

public interface NotificationService {
    NotificationDTO createNotification(Long userId, String title, String message, 
                                     String type, Long referenceId);
    List<NotificationDTO> getUserNotifications(Long userId);
    List<NotificationDTO> getUnreadNotifications(Long userId);
    NotificationDTO markAsRead(Long notificationId, Long userId);
    void markAllAsRead(Long userId);
    long getUnreadCount(Long userId);
}
