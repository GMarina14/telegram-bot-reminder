package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pro.sky.telegrambot.model.NotificationTask;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationTask, Long> {

/*    @Query(value ="ALTER TABLE notification_task ALTER ", nativeQuery = true)
    void createNotification(NotificationTask notificationTask);*/

    List<NotificationTask> findAllByChatId(long chatId);
    List<NotificationTask> findAllBySendTime(LocalDateTime sendTime);
}
