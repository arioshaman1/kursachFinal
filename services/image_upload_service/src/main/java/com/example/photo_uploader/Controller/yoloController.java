package com.example.photo_uploader.Controller;


import com.example.photo_uploader.JWT.JwtUtil;
import com.example.photo_uploader.Repository.FileMetadataRepository;
import com.example.photo_uploader.Service.MinioService;
import com.example.photo_uploader.Service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin

@RestController
@RequestMapping("api/yolo")
public class yoloController {
    private final MinioService minioService;
    private final RestTemplate restTemplate;
    private final TokenService tokenService;
    private final JwtUtil jwtUtil;

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @Autowired
    public yoloController(MinioService minioService, RestTemplate restTemplate, TokenService tokenService, JwtUtil jwtUtil, FileMetadataRepository fileMetadataRepository) {
        this.minioService = minioService;
        this.restTemplate = restTemplate;
        this.tokenService = tokenService;
        this.jwtUtil = jwtUtil;
        this.fileMetadataRepository = fileMetadataRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadYoloModelFile(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        try {
            // Используем TokenService для получения username
            String username = tokenService.getUsernameFromToken(request);
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token or username not found");
            }

            System.out.println("Uploading file: " + file.getOriginalFilename());
            String fileUrl = minioService.uploadFile(file, username);
            String pythonServiceUrl = "http://127.0.0.1:5001/predict";
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
}
