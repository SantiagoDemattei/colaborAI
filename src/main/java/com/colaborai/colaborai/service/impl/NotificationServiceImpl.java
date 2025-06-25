package com.colaborai.colaborai.service.impl;

import com.colaborai.colaborai.dto.NotificationDTO;
import com.colaborai.colaborai.entity.Notification;
import com.colaborai.colaborai.entity.User;
import com.colaborai.colaborai.repository.NotificationRepository;
import com.colaborai.colaborai.repository.UserRepository;
import com.colaborai.colaborai.service.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                 UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    private NotificationDTO toDTO(Notification notification) {
        return new NotificationDTO(
            notification.getId(),
            notification.getTitle(),
            notification.getMessage(),
            notification.getType(),
            notification.isRead(),
            notification.getCreatedAt(),
            notification.getReferenceId()
        );
    }

    @Override
    public NotificationDTO createNotification(Long userId, String title, String message, 
                                            String type, Long referenceId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(Notification.NotificationType.valueOf(type));
        notification.setReferenceId(referenceId);

        Notification saved = notificationRepository.save(notification);
        return toDTO(saved);
    }

    @Override
    public List<NotificationDTO> getUserNotifications(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        List<Notification> notifications = notificationRepository
            .findByUserOrderByCreatedAtDesc(user);

        return notifications.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<NotificationDTO> getUnreadNotifications(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        List<Notification> notifications = notificationRepository
            .findByUserAndIsReadOrderByCreatedAtDesc(user, false);

        return notifications.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public NotificationDTO markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("Notificación no encontrada"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("No tienes permisos para marcar esta notificación");
        }

        notification.setRead(true);
        Notification saved = notificationRepository.save(notification);
        return toDTO(saved);
    }

    @Override
    public void markAllAsRead(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        List<Notification> unreadNotifications = notificationRepository
            .findByUserAndIsReadOrderByCreatedAtDesc(user, false);

        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    @Override
    public long getUnreadCount(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        return notificationRepository.countByUserAndIsRead(user, false);
    }
}
