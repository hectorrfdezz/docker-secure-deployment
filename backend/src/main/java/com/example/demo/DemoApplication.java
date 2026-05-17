package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Entry point for the backend application.  This simple service exposes
 * one endpoint under /api/message which returns a greeting string.  In a
 * real project the API would implement business logic and interact with
 * the database layer defined through Spring Data JPA.
 */
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
  }
}