package dev.abdulrahman.excelsior;

import dev.abdulrahman.excelsior.game.Game;
import dev.abdulrahman.randomstringgenerator.RandomStringGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentSkipListMap;

@Configuration
public class ExcelsiorConfig {
    @Bean
    public ConcurrentSkipListMap<Long, Game> games() {
        return new ConcurrentSkipListMap<>();
    }

    @Bean
    public RandomStringGenerator numberGenerator() {
        return new RandomStringGenerator.Builder()
                .customPool("0123456789")
                .length(10)
                .build();
    }
}
