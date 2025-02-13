package com.debatetimer.controller.tool.export;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ExcelExportInterceptor implements HandlerInterceptor {

    private static final String SPREAD_SHEET_MEDIA_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        ContentDisposition contentDisposition = ContentDisposition.attachment().build();
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
        response.setContentType(SPREAD_SHEET_MEDIA_TYPE);
        return true;
    }
}
