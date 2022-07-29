package com.dataox.shaimaaalansaripdftoscv.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;

@Configuration
@PropertySource(value = "classpath:/application.properties", encoding = "UTF-8")
public class ApplicationContext {

    @Bean
    public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer properties = new PropertySourcesPlaceholderConfigurer();
        properties.setLocation(new FileSystemResource("application.properties"));
        properties.setIgnoreResourceNotFound(false);
        return properties;
    }

}
