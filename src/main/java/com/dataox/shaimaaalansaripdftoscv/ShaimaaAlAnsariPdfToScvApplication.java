package com.dataox.shaimaaalansaripdftoscv;

import com.dataox.shaimaaalansaripdftoscv.config.GraphConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ShaimaaAlAnsariPdfToScvApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(ShaimaaAlAnsariPdfToScvApplication.class, args);
        GraphConfig.initializeGraph();
    }

}
