package com.loyalty.system.service;

import com.loyalty.system.model.Notification;
import com.loyalty.system.model.User;
import com.loyalty.system.repository.NotificationRepository;
import com.loyalty.system.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification n = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));
        if (n.getUser().getId().equals(userId)) {
            n.setIsRead(true);
            notificationRepository.save(n);
        }
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> list = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        for (Notification n : list) {
            if (!n.getIsRead()) {
                n.setIsRead(true);
            }
        }
        notificationRepository.saveAll(list);
    }

    @Transactional
    public void createNotification(Long userId, String title, String message, String type) {
        if (userId != null) {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
            notificationRepository.save(new Notification(user, title, message, type));
        } else {
            // Broadcast to all active customers
            List<User> customers = userRepository.findByRole("CUSTOMER");
            for (User u : customers) {
                notificationRepository.save(new Notification(u, title, message, type != null ? type : "PROMO"));
            }
        }
    }
}
