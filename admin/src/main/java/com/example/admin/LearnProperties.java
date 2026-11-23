package com.example.admin;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
@Data
@Component
@ConfigurationProperties(prefix = "learn")
public class LearnProperties {

    private String env;
    private String banner;
}