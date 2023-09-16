package pro.sky.telegrambot.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class NotificationTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;
    private Long chatId;
    private String notification;
    private LocalDateTime sendTime;

    public NotificationTask(Long chatId, String notification, LocalDateTime sendTime) {
        this.chatId = chatId;
        this.notification = notification;
        this.sendTime = sendTime;
    }

    public NotificationTask() {

    }

    public Long getNotificationId() {
        return notificationId;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public LocalDateTime getSendTime() {
        return sendTime;
    }

    public void setSendTime(LocalDateTime sendTime) {
        this.sendTime = sendTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotificationTask)) return false;
        NotificationTask that = (NotificationTask) o;
        return notificationId == that.notificationId && chatId == that.chatId && notification.equals(that.notification) && sendTime.equals(that.sendTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationId, chatId, notification, sendTime);
    }

    @Override
    public String toString() {
        return "Notification details: \n" +
                "notification id is " + notificationId + '\n' +
                "notification \"" + notification + "\"" + '\n' +
                "notification is set on " + sendTime;
    }
}
