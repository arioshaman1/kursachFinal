package com.example.photo_uploader.Service;

import com.example.photo_uploader.JWT.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    @Autowired
    private JwtUtil jwtUtil;

    public String getTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                System.out.println("Cookie name: " + cookie.getName() + ", value: " + cookie.getValue());
                if ("token".equals(cookie.getName())) {
                    System.out.println("Token found: " + cookie.getValue());
                    return cookie.getValue();
                }
            }
        }
        System.out.println("Token not found in cookies");
        return null;
    }

    public String getUsernameFromToken(HttpServletRequest request) {
        String token = getTokenFromCookies(request);
        if (token != null) {
            System.out.println("Token extracted: " + token);
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);
                System.out.println("Username extracted: " + username);
                return username;
            } else {
                System.out.println("Token is invalid");
            }
        } else {
            System.out.println("Token is null");
        }
        return null;
    }
}