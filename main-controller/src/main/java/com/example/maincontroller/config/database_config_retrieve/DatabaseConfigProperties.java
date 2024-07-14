package com.example.maincontroller.config.database_config_retrieve;

import lombok.Data;

@Data
public class DatabaseConfigProperties {

    private final String url;
    private final String username;
    private final String password;

    private final String driverClassName;

    public DatabaseConfigProperties(String url, String username, String password, String driverClassName) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.driverClassName = driverClassName;
    }
}
