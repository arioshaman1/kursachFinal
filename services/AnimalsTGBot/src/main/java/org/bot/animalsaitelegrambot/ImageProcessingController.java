package org.bot.animalsaitelegrambot;

import org.bot.animalsaitelegrambot.config.BotConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/image")
public class ImageProcessingController {

    private final BotConfig config;
    private final RestTemplate restTemplate;

    @Autowired
    public ImageProcessingController(BotConfig config, RestTemplate restTemplate) {
        this.config = config;
        this.restTemplate = restTemplate;
    }

    @PostMapping(value = "/process", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> processImage(
            @RequestParam("mode") String mode,
            @RequestPart("file") MultipartFile file) {

        try {
            // Определяем URL API в зависимости от режима
            String apiUrl = mode.equalsIgnoreCase("cat_dog")
                    ? config.catAndDogDetectionUrl()
                    : config.yoloDetectionUrl();

            // Подготавливаем запрос
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", resource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Отправляем запрос в сервис модели
            ResponseEntity<String> response = restTemplate.postForEntity(
                    apiUrl,
                    requestEntity,
                    String.class
            );

            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка обработки изображения: " + e.getMessage());
        }
    }
}