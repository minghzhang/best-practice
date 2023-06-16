package com.bestpractice.csmsconfig.dto;

import lombok.Data;

@Data
public class DBConfig {

    private String applicationName;

    private String driverClassName;

    private String url;

    private String username;

    private String password;
}
