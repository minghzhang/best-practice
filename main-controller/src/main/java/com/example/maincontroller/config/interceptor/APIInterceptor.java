package com.example.maincontroller.config.interceptor;

import com.example.maincontroller.config.log.MonitorLogger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class APIInterceptor implements HandlerInterceptor {

    private final ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        startTime.set(System.currentTimeMillis());
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HttpRequestMonitorLog httpRequestMonitorLog = HttpRequestMonitorLog.createHttpRequestMonitorLog(request, response, startTime);
        MonitorLogger.log(httpRequestMonitorLog);
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }

    @Data
    public static class HttpRequestMonitorLog {

        private String requestURI;

        private String requestMethod;

        private Integer statusCode;

        private Long costTimeInMillis;

        public static HttpRequestMonitorLog createHttpRequestMonitorLog(HttpServletRequest request, HttpServletResponse response, ThreadLocal<Long> startTime) {
            HttpRequestMonitorLog monitorLog = new HttpRequestMonitorLog();
            monitorLog.setRequestURI(request.getRequestURI());
            monitorLog.setRequestMethod(request.getMethod());
            monitorLog.setStatusCode(response.getStatus());
            monitorLog.setCostTimeInMillis(System.currentTimeMillis() - startTime.get());
            return monitorLog;
        }
    }
}
