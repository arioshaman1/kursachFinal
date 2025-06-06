package org.bot.animalsaitelegrambot;

import org.bot.animalsaitelegrambot.config.BotConfig;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.springframework.core.io.ByteArrayResource;

import java.util.*;

import static java.util.Map.entry;

@Component
public class AnimalsAiBot extends TelegramLongPollingBot {

    private enum DetectionMode {
        CAT_DOG, YOLO, LLAMA, MOBILENET
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
                    handleTextMessage(update); // Текстовые сообщения обрабатываем здесь
                } else if (update.getMessage().hasPhoto()) {
                    handlePhotoMessage(update); // Фото обрабатываем здесь
                }
            }
        } catch (Exception e) {
            sendErrorMessage(update.getMessage().getChatId(), "Ошибка: " + e.getMessage());
        }
    }
    private void handleTextMessage(Update update) throws TelegramApiException {
        String messageText = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();

        if (messageText.equals("/start")) {
            sendModeSelectionKeyboard(chatId);
        } else {
            // Обработка других текстовых сообщений
            sendMessage(chatId, "Пожалуйста, выберите режим работы из меню или отправьте фото");
        }
    }

    private void handlePhotoMessage(Update update) throws TelegramApiException {
        long chatId = update.getMessage().getChatId();

        try {
            // Проверяем, что фото действительно есть
            if (update.getMessage().getPhoto().isEmpty()) {
                sendMessage(chatId, "⚠️ Не удалось получить фото. Попробуйте отправить еще раз.");
                return;
            }

            // Получаем самое качественное фото
            String fileId = update.getMessage().getPhoto()
                    .stream()
                    .max(Comparator.comparing(PhotoSize::getFileSize))
                    .orElseThrow()
                    .getFileId();

            sendMessage(chatId, "⏳ Обрабатываю фото...");

            // Скачиваем фото с улучшенной обработкой ошибок
            byte[] photoBytes = downloadPhotoFromTelegram(fileId);

            // Проверяем размер фото
            if (photoBytes == null || photoBytes.length == 0) {
                sendMessage(chatId, "⚠️ Не удалось загрузить фото. Попробуйте отправить другое изображение.");
                return;
            }

            // Отправляем на сервис
            String apiUrl = "http://localhost:8080/api/mobilenet/upload";
            String response = sendPhotoToApi(photoBytes, apiUrl);

            // Форматируем и отправляем результат
            String formattedResponse = formatAnimalResponse(response);
            sendMessage(chatId, formattedResponse);

        } catch (Exception e) {
            sendMessage(chatId, "⚠️ Произошла ошибка при обработке фото. Пожалуйста, попробуйте еще раз.");
            e.printStackTrace(); // Логируем ошибку для отладки
        }
    }

    private String formatAnimalResponse(String jsonResponse) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            JSONObject topPred = json.getJSONObject("top_prediction");

            // Переводим и форматируем название животного
            String animalName = translateAnimalName(topPred.getString("label"));
            int confidence = (int)(topPred.getDouble("confidence") * 100);

            // Создаем основной ответ
            StringBuilder result = new StringBuilder();
            result.append("🖼 Результат анализа:\n\n")
                    .append("На фото: *").append(animalName).append("*\n")
                    .append("Уверенность: ").append(confidence).append("%\n\n");

            // Добавляем эмодзи в зависимости от животного
            result.append(getAnimalEmoji(animalName)).append("\n\n");

            // Добавляем интересные факты для некоторых животных
            result.append(getAnimalFact(animalName));

            return result.toString();

        } catch (Exception e) {
            return "🔍 Не удалось обработать результат. Техническая информация:\n" + jsonResponse;
        }
    }

    private String translateAnimalName(String englishName) {
        // Удаляем префиксы и нижние подчеркивания
        String cleanName = englishName.replaceAll("^n\\d+_", "").replace("_", " ");

        // Специальные случаи
        Map<String, String> specialCases = Map.of(
                "giant panda", "большая панда",
                "Arctic fox", "песец",
                "Great Pyrenees", "пиренейская горная собака",
                "malamute", "маламут",
                "hog", "кабан"
        );

        return specialCases.getOrDefault(cleanName, cleanName);
    }

    private String getAnimalEmoji(String animalName) {
        return switch (animalName.toLowerCase()) {
            case "большая панда", "панда" -> "🐼 Панды - удивительные животные, которые питаются преимущественно бамбуком!";
            case "песец" -> "🦊 Песец - арктический лис с красивым белым мехом!";
            case "кабан" -> "🐗 Будьте осторожны, кабаны могут быть агрессивными!";
            case "пиренейская горная собака", "маламут" -> "🐶 Это красивая и крупная порода собак!";
            default -> "ℹ️ Это интересный представитель животного мира!";
        };
    }

    private String getAnimalFact(String animalName) {
        return switch (animalName.toLowerCase()) {
            case "большая панда", "панда" ->
                    "📌 *Интересный факт:* Большие панды проводят до 12 часов в день за едой, съедая до 15% от своего веса в бамбуке!";
            case "песец" ->
                    "📌 *Интересный факт:* Песцы могут выживать при температурах до -50°C благодаря своему густому меху!";
            case "кабан" ->
                    "📌 *Интересный факт:* Кабаны обладают отличным обонянием и могут учуять пищу под землей!";
            default ->
                    "🔍 Хотите узнать больше об этом животном? Напишите мне его название!";
        };
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
    private String formatMobileNetResponse(String jsonResponse) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            StringBuilder result = new StringBuilder("🔍 Результат анализа изображения:\n\n");

            JSONArray predictions = json.getJSONArray("predictions");
            JSONObject topPrediction = json.getJSONObject("top_prediction");

            // Фильтруем только релевантные результаты с достаточной уверенностью
            List<JSONObject> animalPredictions = new ArrayList<>();
            for (int i = 0; i < predictions.length(); i++) {
                JSONObject pred = predictions.getJSONObject(i);
                if (isRelevantAnimal(pred.getString("label")) && pred.getDouble("confidence") > 0.05) {
                    animalPredictions.add(pred);
                }
            }

            if (animalPredictions.isEmpty()) {
                // Если среди предсказаний нет животных
                return result.append("Не удалось определить животное на фото\n")
                        .append("Сервис считает, что это может быть:\n")
                        .append(topPrediction.getString("label"))
                        .append(" (")
                        .append(String.format("%.1f", topPrediction.getDouble("confidence") * 100))
                        .append("%)")
                        .toString();
            }

            // Сортируем по уверенности
            animalPredictions.sort((a, b) -> Double.compare(b.getDouble("confidence"), a.getDouble("confidence")));

            // Берем топ-3 релевантных результата
            result.append("На фото вероятно изображено:\n");
            for (int i = 0; i < Math.min(3, animalPredictions.size()); i++) {
                JSONObject pred = animalPredictions.get(i);
                String label = translateAnimalName(pred.getString("label"));
                result.append("\n")
                        .append(i + 1).append(". ")
                        .append(getAnimalEmoji(label)).append(" ")
                        .append(label).append(" - ")
                        .append(String.format("%.1f", pred.getDouble("confidence") * 100))
                        .append("%");
            }

            return result.toString();

        } catch (Exception e) {
            return "⚠️ Ошибка обработки результатов. Попробуйте другое фото.\n" +
                    "Техническая информация: " + e.getMessage();
        }
    }

    private boolean isRelevantAnimal(String label) {
        // Исключаем предметы и нерелевантные категории
        String lowerLabel = label.toLowerCase();
        return !lowerLabel.contains("_") &&  // Исключаем составные названия
                !lowerLabel.contains(" ") &&
                !lowerLabel.equals("dog") &&
                !lowerLabel.equals("cat") &&
                !lowerLabel.equals("chihuahua") &&
                !lowerLabel.equals("terrier") &&
                !lowerLabel.matches(".*(curtain|screen|furniture|device|tool).*");
    }

    private HttpEntity<MultiValueMap<String, Object>> createRequestWithPhoto(byte[] photoBytes) {
        // Создаем заголовки
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Подготавливаем файл для отправки
        ByteArrayResource fileAsResource = new ByteArrayResource(photoBytes) {
            @Override
            public String getFilename() {
                return "telegram_photo.jpg";
            }
        };

        // Формируем тело запроса
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileAsResource);

        return new HttpEntity<>(body, headers);
    }
    private byte[] downloadPhotoFromTelegram(String fileId) throws TelegramApiException {
        try {
            // Получаем информацию о файле
            org.telegram.telegrambots.meta.api.objects.File file = execute(new GetFile(fileId));

            // Формируем URL для скачивания
            String fileUrl = "https://api.telegram.org/file/bot" + getBotToken() + "/" + file.getFilePath();

            // Загружаем файл
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    fileUrl,
                    HttpMethod.GET,
                    null,
                    byte[].class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new TelegramApiException("Не удалось загрузить фото: " + response.getStatusCode());
            }
        } catch (RestClientException e) {
            throw new TelegramApiException("Ошибка при загрузке фото: " + e.getMessage(), e);
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
        row2.add("📱 Классификация животных");  // Новая кнопка
        row2.add("💬 Чат с Llama 4");

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