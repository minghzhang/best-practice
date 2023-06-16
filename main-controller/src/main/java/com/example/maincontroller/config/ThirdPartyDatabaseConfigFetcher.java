package com.example.maincontroller.config;

import com.alibaba.fastjson2.JSON;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ThirdPartyDatabaseConfigFetcher implements DatabaseConfigFetcher {

    @Override
    public DatabaseConfigProperties fetchDatabaseConfig() {
        Request request = new Builder().url("http://127.0.0.1:9999/csms/db-config").build();
        OkHttpClient client = new OkHttpClient();
        Call call = client.newCall(request);
        Response response = null;
        try {
            response = call.execute();
            ResponseBody body = response.body();
            String bodyString = body.string();
            DatabaseConfigProperties databaseConfig = JSON.parseObject(bodyString, DatabaseConfigProperties.class);
            log.info("fetchDatabaseConfig: {}", databaseConfig);
            return databaseConfig;
        } catch (IOException e) {
            log.error("fetchDatabaseConfig failed", e);
            throw new RuntimeException(e);
        }
    }
}
