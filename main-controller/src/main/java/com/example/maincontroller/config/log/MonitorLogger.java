package com.example.maincontroller.config.log;


import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorLogger {

    private static Logger logger = LoggerFactory.getLogger(MonitorLogger.class);

    public static  <T> void log(T log) {
        String jsonString = JSON.toJSONString(log);
        logger.trace(jsonString);
    }
}
