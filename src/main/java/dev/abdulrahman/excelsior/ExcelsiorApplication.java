package dev.abdulrahman.excelsior;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ExcelsiorApplication {
    static void main(String[] args) {
        SpringApplication.run(ExcelsiorApplication.class, args);
    }
}
