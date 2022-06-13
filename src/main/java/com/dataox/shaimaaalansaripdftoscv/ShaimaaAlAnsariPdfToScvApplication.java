package com.dataox.shaimaaalansaripdftoscv;

import com.dataox.shaimaaalansaripdftoscv.config.SubscribeConfig;
import lombok.AllArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@AllArgsConstructor
@SpringBootApplication
public class ShaimaaAlAnsariPdfToScvApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShaimaaAlAnsariPdfToScvApplication.class, args);

        SubscribeConfig.initializeGraph();
    }

}
