package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @RestController
    static class ApiController {

        @GetMapping("/api/message")
        public String getMessage() {
            return "Hello from backend!";
        }

        @GetMapping("/admin")
        public String admin() {
            return "Admin area protected by Nginx Basic Auth";
        }
    }
}
