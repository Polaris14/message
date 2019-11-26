package com.tch.message;

import com.tch.message.socket.WebSocketServer;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@MapperScan("com.tch.message.mapper")
public class MessageApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(MessageApplication.class, args);
        WebSocketServer.setApplicationContext(applicationContext);
    }

}
