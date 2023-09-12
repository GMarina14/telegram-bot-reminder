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

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;

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

}
