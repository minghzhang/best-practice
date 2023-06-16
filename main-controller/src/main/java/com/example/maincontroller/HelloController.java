package com.example.maincontroller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/user")
public class HelloController {

    @GetMapping("/{userId}")
    public String getUser(@PathVariable("userId") String userId) {
        log.info("getUser, userId: {}", userId);
        return "user: " + userId;
    }
}
