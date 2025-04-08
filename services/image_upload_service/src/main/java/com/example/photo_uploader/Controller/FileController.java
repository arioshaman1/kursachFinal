package com.example.photo_uploader.Controller;

import com.example.photo_uploader.JWT.JwtUtil;
import com.example.photo_uploader.Repository.FileMetadataRepository;
import com.example.photo_uploader.Service.MinioService;
import com.example.photo_uploader.Service.TokenService;
import com.example.photo_uploader.entities.FileMetadata;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final MinioService minioService;
    private final RestTemplate restTemplate;
    private final TokenService tokenService;
    private final JwtUtil jwtUtil;

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @Autowired
    public FileController(MinioService minioService, RestTemplate restTemplate, TokenService tokenService, JwtUtil jwtUtil, FileMetadataRepository fileMetadataRepository) {
        this.minioService = minioService;
        this.restTemplate = restTemplate;
        this.tokenService = tokenService;
        this.jwtUtil = jwtUtil;
        this.fileMetadataRepository = fileMetadataRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        try {
            // Используем TokenService для получения username
            String username = tokenService.getUsernameFromToken(request);
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token or username not found");
            }

            System.out.println("Uploading file: " + file.getOriginalFilename());
            String fileUrl = minioService.uploadFile(file, username);
            String pythonServiceUrl = "http://127.0.0.1:5000/predict";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileResource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            System.out.println("Sending request to Flask...");

            // Запрос в Flask
            ResponseEntity<String> response = restTemplate.postForEntity(pythonServiceUrl, requestEntity, String.class);
            System.out.println("Flask response status: " + response.getStatusCode());
            System.out.println("Flask response body: " + response.getBody());

            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    @GetMapping("/user-photos")
    public ResponseEntity<?> getUserPhotos(HttpServletRequest request) {
        try {
            // Получаем токен из куки
            String token = tokenService.getTokenFromCookies(request);
            System.out.println(token);
            if (token == null || !jwtUtil.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
            }

            // Извлекаем username из токена
            String username = jwtUtil.extractUsername(token);

            // Получаем список фотографий пользователя из базы данных
            List<FileMetadata> userPhotos = fileMetadataRepository.findByUsername(username);

            // Формируем список фотографий для ответа
            List<Map<String, Object>> photosResponse = userPhotos.stream()
                    .map(photo -> {
                        Map<String, Object> photoData = new HashMap<>();
                        photoData.put("fileName", photo.getFileName());
                        photoData.put("originalFileName", photo.getOriginalFileName());
                        photoData.put("fileSize", photo.getFileSize());
                        photoData.put("fileType", photo.getFileType());
                        photoData.put("uploadTime", photo.getUploadTime());
                        return photoData;
                    })
                    .collect(Collectors.toList());

            // Создаем объект для ответа
            Map<String, Object> response = new HashMap<>();
            response.put("username", username);
            response.put("photos", photosResponse);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

}