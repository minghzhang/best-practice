package com.example.maincontroller.config.database_config_retrieve;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DatabaseBeanPostProcessor implements BeanPostProcessor {

    @Autowired
    private DatabaseConfigFetcher databaseConfigFetcher;

    public static String DBBEANSTR = "spring.datasource-org.springframework.boot.autoconfigure.jdbc.DataSourceProperties";


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (DBBEANSTR.equals(beanName)) {
            DataSourceProperties dataSourceProperties = (DataSourceProperties) bean;
            DatabaseConfigProperties dynamicConfig = databaseConfigFetcher.fetchDatabaseConfig();
            dataSourceProperties.setUrl(dynamicConfig.getUrl());
            dataSourceProperties.setUsername(dynamicConfig.getUsername());
            dataSourceProperties.setPassword(dynamicConfig.getPassword());
        }
        return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
