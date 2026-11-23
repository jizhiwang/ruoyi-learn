package com.example.admin.controller;

import com.example.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

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
    public Result health() {
        return Result.success("status", "UP");
    }
}
