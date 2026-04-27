package com.lappyqt.glacialairlines;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.lappyqt.glacialairlines")
public class GlacialAirlinesWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(GlacialAirlinesWebApplication.class, args);
    }
}
