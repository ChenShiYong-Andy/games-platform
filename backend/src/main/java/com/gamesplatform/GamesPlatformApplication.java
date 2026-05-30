package com.gamesplatform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.gamesplatform.**.mapper")
public class GamesPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(GamesPlatformApplication.class, args);
    }
}
