package com.example.admin;

import com.example.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class ConfigController {

    private final LearnProperties properties;

    public ConfigController(LearnProperties properties) {
        this.properties = properties;
    }

    @GetMapping("/config")
    public Result config() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("env", properties.getEnv());
        data.put("banner", properties.getBanner());
        return Result.success(data);
    }
}