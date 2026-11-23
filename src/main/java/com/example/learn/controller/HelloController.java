package com.example.learn.controller;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import com.example.learn.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public Result hello(@RequestParam(defaultValue = "Ted") String name) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("message", "Hello, " + name + "!");
        data.put("time", LocalDateTime.now().toString());
        return Result.success(data);
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
