package com.gamesplatform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 游戏平台后端应用入口。
 */
@SpringBootApplication
@MapperScan("com.gamesplatform.**.mapper")
public class GamesPlatformApplication {

    /**
     * 启动游戏平台后端应用。
     *
     * @param args 启动参数。
     */
    public static void main(String[] args) {
        SpringApplication.run(GamesPlatformApplication.class, args);
    }
}
