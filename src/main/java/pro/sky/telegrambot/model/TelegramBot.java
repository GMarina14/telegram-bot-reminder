package pro.sky.telegrambot.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class TelegramBot {
    @Id
    @GeneratedValue
    private Long id;



}
