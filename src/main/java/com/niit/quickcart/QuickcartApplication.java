package com.niit.quickcart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class QuickcartApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuickcartApplication.class, args);
        System.out.println("QuickCart running at http://localhost:8080");
    }
}
