package org.bot.animalsaitelegrambot;

import org.bot.animalsaitelegrambot.config.BotConfig;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.springframework.core.io.ByteArrayResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AnimalsAiBot extends TelegramLongPollingBot {

    private enum DetectionMode {
        CAT_DOG, YOLO, LLAMA
    }

    private final BotConfig config;
    private final RestTemplate restTemplate;
    private DetectionMode currentMode;

    public AnimalsAiBot(BotConfig config, RestTemplate restTemplate) {
        super(config.getToken());
        this.config = config;
        this.restTemplate = restTemplate;
    }

    @Override
    public String getBotUsername() {
        return config.getName();
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                if (update.getMessage().hasText()) {
                    handleTextMessage(update);
                } else if (update.getMessage().hasPhoto()) {
                    handlePhotoMessage(update);
                }
            }
        } catch (Exception e) {
            sendErrorMessage(update.getMessage().getChatId(), "Ошибка: " + e.getMessage());
        }
    }

    private void handleTextMessage(Update update) throws TelegramApiException {
        String messageText = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();

        switch (messageText) {
            case "/start":
                sendModeSelectionKeyboard(chatId);
                break;
            case "🐱🐶 Определить кошку/собаку":
                currentMode = DetectionMode.CAT_DOG;
                sendMessage(chatId, "Выбран режим определения кошек и собак. Отправьте фото животного.");
                break;
            case "🖼 YOLO анализ":
                currentMode = DetectionMode.YOLO;
                sendMessage(chatId, "Выбран YOLO режим анализа. Отправьте фото для детекции объектов.");
                break;
            case "💬 Чат с Llama 4":
                currentMode = DetectionMode.LLAMA;
                sendMessage(chatId, "Режим чата с нейросетью. Напишите ваш вопрос.");
                break;
            default:
                if (currentMode == DetectionMode.LLAMA) {
                    String response = askLlama(messageText, chatId);  // Используем новый метод
                    sendMessage(chatId, response);
                } else {
                    sendMessage(chatId, "Пожалуйста, выберите режим работы из меню");
                }
        }
    }
    private String askLlama(String userMessage, long chatId) {
        try {
            String apiUrl = config.llamaApiUrl();  // Используем URL из конфига

            // Формируем JSON-запрос
            JSONObject requestBody = new JSONObject();
            requestBody.put("message", userMessage);
            requestBody.put("chat_id", chatId);

            // Настраиваем заголовки
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Отправляем POST-запрос
            HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);

            // Обрабатываем ответ
            JSONObject jsonResponse = new JSONObject(response.getBody());
            if (jsonResponse.getString("status").equals("success")) {
                return jsonResponse.getString("response");
            } else {
                return "Ошибка нейросети: " + jsonResponse.optString("error", "Unknown error");
            }

        } catch (Exception e) {
            return "Ошибка соединения с нейросетью: " + e.getMessage();
        }
    }
    private String formatYoloResponse(String jsonResponse) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            StringBuilder result = new StringBuilder("🔍 Результаты анализа:\n\n");

            // Проверяем наличие массива detections
            if (json.has("detections")) {
                JSONArray detections = json.getJSONArray("detections");

                if (detections.length() == 0) {
                    return result.append("ℹ️ На изображении не обнаружено объектов\n")
                            .append("Файл: ").append(json.optString("filename", "неизвестный"))
                            .append("\nТип: ").append(json.optString("media_type", "неизвестный"))
                            .toString();
                }

                // Группируем объекты по классам
                Map<String, Integer> objectCounts = new HashMap<>();
                for (int i = 0; i < detections.length(); i++) {
                    JSONObject det = detections.getJSONObject(i);
                    String objClass = det.optString("class", "неизвестный объект");
                    objectCounts.put(objClass, objectCounts.getOrDefault(objClass, 0) + 1);
                }

                // Формируем сводку
                result.append("📊 Обнаружено объектов: ").append(detections.length()).append("\n\n")
                        .append("Состав объектов:\n");

                objectCounts.forEach((cls, count) ->
                        result.append("• ").append(cls).append(": ").append(count).append("\n"));

                // Добавляем детали
                result.append("\n🔎 Детали обнаружения:\n");
                for (int i = 0; i < detections.length(); i++) {
                    JSONObject det = detections.getJSONObject(i);
                    result.append("\n").append(i+1).append(". ")
                            .append(det.optString("class", "неизвестный объект")).append("\n")
                            .append("   Уверенность: ")
                            .append(String.format("%.1f", det.optDouble("confidence", 0)*100))
                            .append("%\n");

                    if (det.has("bbox")) {
                        JSONObject bbox = det.getJSONObject("bbox");
                        result.append("   Координаты: [")
                                .append(String.format("%.0f", bbox.optDouble("x1", 0))).append(",")
                                .append(String.format("%.0f", bbox.optDouble("y1", 0))).append("]-[")
                                .append(String.format("%.0f", bbox.optDouble("x2", 0))).append(",")
                                .append(String.format("%.0f", bbox.optDouble("y2", 0))).append("]\n")
                                .append("   Размер: ")
                                .append(String.format("%.0f", bbox.optDouble("x2", 0)-bbox.optDouble("x1", 0)))
                                .append("x")
                                .append(String.format("%.0f", bbox.optDouble("y2", 0)-bbox.optDouble("y1", 0)));
                    }
                }

                return result.toString();
            }

            return result.append("⚠️ В ответе сервера отсутствуют данные об обнаружениях\n")
                    .append("Полный ответ:\n")
                    .append(json.toString(2))
                    .toString();

        } catch (Exception e) {
            return "⚠️ Ошибка обработки ответа: " + e.getMessage()
                    + "\n\nПолный ответ:\n"
                    + jsonResponse;
        }
    }
    private String sendPhotoToApi(byte[] photoBytes, String apiUrl) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            ByteArrayResource resource = new ByteArrayResource(photoBytes) {
                @Override
                public String getFilename() {
                    return "animal_photo.jpg";
                }
            };

            // 3. Формирование тела запроса
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", resource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);

            // 4. Отправка запроса
            ResponseEntity<String> response = restTemplate.postForEntity(
                    apiUrl,
                    requestEntity,
                    String.class
            );

            // 5. Проверка статуса ответа
            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("API вернул статус: " + response.getStatusCode());
            }

            return response.getBody();

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при отправке фото на API: " + e.getMessage(), e);
        }
    }
    private String formatCatDogResponse(String jsonResponse) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            StringBuilder result = new StringBuilder("🐾 Результат определения:\n\n");

            // Вариант 1: Новый формат (label + probability)
            if (json.has("label") && json.has("probability")) {
                String label = json.getString("label");
                double probability = json.getDouble("probability") * 100;

                String emoji = label.equalsIgnoreCase("cat") ? "🐱" : "🐶";
                String animalName = label.equalsIgnoreCase("cat") ? "кошка" : "собака";

                result.append("Животное: ").append(animalName).append(" ").append(emoji).append("\n")
                        .append("Уверенность: ").append(String.format("%.1f", probability)).append("%");

                return result.toString();
            }
            // Вариант 2: Старый формат (class + confidence)
            else if (json.has("class") && json.has("confidence")) {
                String animalClass = json.getString("class");
                double confidence = json.getDouble("confidence") * 100;

                return String.format(
                        "Животное: %s\nУверенность: %.1f%%",
                        animalClass.equals("cat") ? "кошка 🐱" : "собака 🐶",
                        confidence
                );
            }
            else if (json.has("prediction")) {
                return "Результат: " + json.getString("prediction");
            }
            else {
                return "🔍 Не удалось определить животное. Неожиданный формат ответа.\n"
                        + "Ответ сервера: " + json.toString();
            }

        } catch (Exception e) {
            return "⚠️ Ошибка обработки ответа сервера:\n"
                    + e.getMessage()
                    + "\n\nПолный ответ:\n"
                    + jsonResponse;
        }
    }

    private void handlePhotoMessage(Update update) throws TelegramApiException {
        if (currentMode == null) {
            sendMessage(update.getMessage().getChatId(), "Сначала выберите режим работы из меню");
            return;
        }

        long chatId = update.getMessage().getChatId();
        String fileId = update.getMessage().getPhoto().get(0).getFileId();

        sendMessage(chatId, "📨 Фото получено, идет обработка...");

        try {
            byte[] photoBytes = downloadPhotoFromTelegram(fileId);

            String apiUrl = currentMode == DetectionMode.CAT_DOG
                    ? config.catAndDogDetectionUrl()
                    : config.yoloDetectionUrl();

            String response = sendPhotoToApi(photoBytes, apiUrl);

            System.out.println("Raw API response: " + response);
            String formattedResponse = currentMode == DetectionMode.CAT_DOG
                    ? formatCatDogResponse(response)
                    : formatYoloResponse(response);

            // 5. Отправляем результат
            sendMessage(chatId, formattedResponse);
        } catch (Exception e) {
            sendErrorMessage(chatId, "Ошибка при обработке фото: " + e.getMessage());
        }
    }

    private byte[] downloadPhotoFromTelegram(String fileId) throws TelegramApiException {
        org.telegram.telegrambots.meta.api.objects.File file = execute(new GetFile(fileId));
        String fileUrl = "https://api.telegram.org/file/bot" + getBotToken() + "/" + file.getFilePath();

        try {
            return restTemplate.getForObject(fileUrl, byte[].class);
        } catch (Exception e) {
            throw new TelegramApiException("Ошибка загрузки фото", e);
        }
    }

    private String formatResponse(String apiResponse, DetectionMode mode) {
        if (mode == DetectionMode.CAT_DOG) {
            return "Результат определения:\n" + apiResponse;
        } else {
            return "YOLO детекция:\n" + apiResponse;
        }
    }

    private void sendModeSelectionKeyboard(long chatId) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите режим работы:");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("🐱🐶 Определить кошку/собаку");
        row1.add("🖼 YOLO анализ");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("💬 Чат с Llama 4");  // Новая кнопка

        keyboard.add(row1);
        keyboard.add(row2);
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);

        message.setReplyMarkup(keyboardMarkup);
        execute(message);
    }

    private void sendMessage(long chatId, String text) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        execute(message);
    }

    private void sendErrorMessage(long chatId, String errorText) {
        try {
            sendMessage(chatId, "⚠️ " + errorText);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}