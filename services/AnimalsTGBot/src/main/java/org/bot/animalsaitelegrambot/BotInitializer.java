package org.bot.animalsaitelegrambot;

import org.bot.animalsaitelegrambot.AnimalsAiBot;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class BotInitializer {

    @Bean
    public CommandLineRunner commandLineRunner(AnimalsAiBot bot) {
        return args -> {
            try {
                TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
                botsApi.registerBot(bot);
                System.out.println("Бот успешно запущен!");
            } catch (TelegramApiException e) {
                System.err.println("Ошибка при запуске бота: " + e.getMessage());
            }
        };
    }
}