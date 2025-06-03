package com.example.photo_uploader.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ByteArrayResource;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/mobilenet")
public class MobileNetController {

    private final RestTemplate restTemplate;
    private static final String FLASK_API_URL = "http://localhost:5003/predict";

    @Autowired
    public MobileNetController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/upload")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<String> classifyImage(@RequestParam("file") MultipartFile file) {
        try {
            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            // Prepare the request body
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileResource);

            // Create the HTTP entity
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Send request to Flask API
            ResponseEntity<String> response = restTemplate.postForEntity(
                    FLASK_API_URL,
                    requestEntity,
                    String.class
            );

            // Return the Flask API response directly
            return ResponseEntity
                    .status(response.getStatusCode())
                    .body(response.getBody());

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // Simple endpoint to check if the controller is working
    @GetMapping("/test")
    public ResponseEntity<String> testConnection() {
        try {
            String response = restTemplate.getForObject(FLASK_API_URL, String.class);
            return ResponseEntity.ok("Connection to Flask API successful. Response: " + response);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to connect to Flask API: " + e.getMessage());
        }
    }
}