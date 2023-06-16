package com.bestpractice.csmsconfig.controller;

import com.bestpractice.csmsconfig.dto.DBConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/csms")
public class CsmsController {

    @GetMapping("/db-config")
    public DBConfig getDBConfig() {

        DBConfig dbConfig = new DBConfig();
        dbConfig.setPassword("1qaz@WSX");
        dbConfig.setUsername("root");
        dbConfig.setUrl("jdbc:mysql://0.0.0.0:3316/novels");
        dbConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        log.info("CsmsController getDBConfig: {}", dbConfig);
        return dbConfig;
    }
}
