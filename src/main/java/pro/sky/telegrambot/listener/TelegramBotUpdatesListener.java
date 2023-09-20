package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.botcommandscope.BotCommandScopeDefault;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.configuration.TelegramBotConfiguration;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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

    private final TelegramBotConfiguration telegramBotConfiguration;


    public TelegramBotUpdatesListener(NotificationRepository notificationRepository, TelegramBotConfiguration telegramBotConfiguration) {
        this.notificationRepository = notificationRepository;
        this.telegramBotConfiguration = telegramBotConfiguration;

    /*    List<BotCommand> botsCommands = new ArrayList<>();
        botsCommands.add(new BotCommand("/start", "press to start the RemindMe bot"));
        botsCommands.add(new BotCommand("/new", "create new notification"));
        botsCommands.add(new BotCommand("/help", "invoke this if want to see this message again"));

        telegramBot.execute(new SetMyCommands(botsCommands, new BotCommandScopeDefault(), null));*/

            //telegramBot.execute(new SetMyCommands(botsCommands, new BotCommandScopeDefault(), null));


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
                String text = update.message().text();

                // sending feedback if needed
                if("/start".equals(text)){
                    startMessage(chatId, update.message().chat().firstName());
                    logger.info("Everything is ok. The greeting message was sent");
                }else{
                  // if(checkFormatAndDate(chatId, update.message().text())){
                       saveNotification(chatId, update.message().text());
                       logger.info("Everything is ok. The notification is saved");
                }



            });

        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    // checking if format is correct and notification date is actual
    private boolean checkFormatAndDate(long chatId, String notification){
        Matcher matcher = pattern.matcher(notification);
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime desirableDate =  LocalDateTime.parse(matcher.group(1), dateFormatter);
        boolean result = true;

        if(!matcher.matches()) {
            logger.error("Notification format is incorrect");
            sendMessage(chatId, "Format is incorrect");
            result= false;
        } else if (!(desirableDate.isEqual(today) || desirableDate.isAfter(today))) {
            logger.error("The date is not actual");
            sendMessage(chatId, "Date has to be actual: today or after today. You can't send a reminder to the past yet");
            result = false;
        }

        return result;
    }


    private void startMessage(long chatId, String name) {
        // creating the greetings message
        String answer = " Hi, " + name + " , welcome to your personal reminder";
        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String text) {
        // creating a message for a specified user
        SendMessage message = new SendMessage(String.valueOf(chatId), text);

        //sending the message
        SendResponse response = telegramBot.execute(message);

        // checking if any errors occurred
        if (!response.isOk())
            logger.error("Error " + response.errorCode() + " occurred.");
    }


    private void saveNotification(long chatId, String notification) {
        Matcher matcher = pattern.matcher(notification);
        LocalDateTime today = LocalDateTime.now();

        if (matcher.matches()) {
            // grouping notification info
            String date = matcher.group(1);
            String notificationText = matcher.group(3);

            LocalDateTime notificationDate = LocalDateTime.parse(date, dateFormatter);

            // checking if data is actual
            if (!(notificationDate.isEqual(today) || notificationDate.isAfter(today))){
                logger.error("The date is not actual");
                sendMessage(chatId, "Date has to be actual: today or after today. You can't send a reminder to the past yet");
            }else {
                logger.info("Request contains all needed info");
                notificationRepository.save(new NotificationTask(chatId, notificationText, notificationDate));
                sendMessage(chatId,"Notification is saved");
            }

        } else {
            logger.info("Something went wrong. Not enough data received");
            sendMessage(chatId, "Wrong format. Please try again");
        }

    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void sendNotifications() {
        // creating a list of all notifications needed to be sent this very minute
        List<NotificationTask> notifications = notificationRepository.findAllBySendTime(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
       //sending notifications to exact users
        for (NotificationTask notification : notifications) {
            sendMessage(notification.getChatId(), notification.getNotification());
            logger.info("Sending notifications to users");
            notificationRepository.delete(notification);
        }
    }
}
