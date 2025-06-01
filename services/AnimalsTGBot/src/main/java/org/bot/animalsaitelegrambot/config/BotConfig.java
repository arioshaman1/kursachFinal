package org.bot.animalsaitelegrambot.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BotConfig {
    @Value("${telegram.bot.name}")
    private String botName;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${api.catanddogs-detection.url}")
    private String catAndDogDetectionUrl;

    @Value("${api.yolo-detection.url}")
    private String yoloDetectionUrl;
    @Value("${server.port}")
    private String serverPort;

    @Value("${api.llama.url}")
    private String llamaApiUrl;

    public String llamaApiUrl() {
        return llamaApiUrl;
    }



    public String getServerPort() { return serverPort; }
    public String getName() { return botName; }
    public String getToken() { return botToken; }
    public String catAndDogDetectionUrl() { return catAndDogDetectionUrl; }
    public String yoloDetectionUrl() { return yoloDetectionUrl; }
}