package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pro.sky.telegrambot.model.NotificationTask;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationTask, Long> {

    List<NotificationTask> findAllByChatId(long chatId);
/*    @Query("SELECT  FROM notification_task where notification_send_time = : sendtime")
    List<NotificationTask> findAllBySendTime(LocalDateTime sendTime);*/


    @Query(value = "SELECT n FROM NotificationTask n WHERE n.sendTime = :sendTime")
    List<NotificationTask> findAllBySendTime(LocalDateTime sendTime);

}
