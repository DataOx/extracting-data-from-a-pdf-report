package com.dataox.shaimaaalansaripdftoscv;

import com.dataox.shaimaaalansaripdftoscv.config.GraphConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ShaimaaAlAnsariPdfToScvApplication {

    public static void main(String[] args) throws Exception {
        GraphConfig.initializeGraphAccount();
        SpringApplication.run(ShaimaaAlAnsariPdfToScvApplication.class, args);
    }

}
