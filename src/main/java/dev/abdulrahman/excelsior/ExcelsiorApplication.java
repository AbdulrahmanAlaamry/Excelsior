package dev.abdulrahman.excelsior;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class ExcelsiorApplication {

    private static final Logger log = LoggerFactory.getLogger(ExcelsiorApplication.class);

    static void main(String[] args) {
        SpringApplication.run(ExcelsiorApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        log.info("Excelsior API server is live (https://api.excelsior-library.com/api).");
    }
}
