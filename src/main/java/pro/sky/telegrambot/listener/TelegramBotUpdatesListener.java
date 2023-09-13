package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final NotificationRepository notificationRepository;

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private Pattern pattern = Pattern.compile("([0-9.:\\s]{16})(\\s)([\\W+]+)");

    // variable to convert string back to needed date format
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:m");

    @Autowired
    private TelegramBot telegramBot;

    public TelegramBotUpdatesListener(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {

        // checking if any updates were received
        if (!updates.isEmpty()) {
            // processing all updates
            updates.forEach(update -> {
                logger.info("Processing update: {}", update);

                // identifying chat id
                long chatId = update.message().chat().id();

                // sending feedback if needed
                switch (update.message().text()) {
                    case "/start":
                        startMessage(chatId, update.message().chat().firstName());
                        logger.info("Everything is ok. The greeting message was sent");
                        break;

                    case "/new":
                        saveNotification(chatId, update.message().text());
                        logger.info("New notification is saved");
                        break;

                    default:
                        sendMessage(chatId, "Unexpected command, not able to process it yet");
                        logger.info("The command was not recognized");
                }

            });

        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }



    private void startMessage(long chatId, String name) {
        // creating the greetings message
        String answer = " Hi, " + name + " , welcome to your personal reminder";
        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String text) {
        // getting a greeting message for a specified user
        SendMessage message = new SendMessage(String.valueOf(chatId), text);

        //sending the message
        SendResponse response = telegramBot.execute(message);

        // checking if any errors occurred
        if (!response.isOk())
            logger.error("Error " + response.errorCode() + " occurred.");
    }


    private void saveNotification(long chatId, String notification) {
        // checking if the request text contains all needed info to create notification
        Matcher matcher = pattern.matcher(notification);

        if (matcher.matches()) {
            // grouping notification info
            String date = matcher.group(1);
            String notificationText = matcher.group(3);
            logger.info("Request contains all needed info");
            NotificationTask notificationTask = new NotificationTask(chatId, notificationText, LocalDateTime.parse(date, dateFormatter));
            notificationRepository.save(notificationTask);

        } else {
            logger.info("Something went wrong. Not enough data received");
            sendMessage(chatId, "Wrong format. Please try again");
        }
    }

}
