package com.debatetimer.controller.tool.export;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ExcelExportInterceptor implements HandlerInterceptor {

    private static final String SPREAD_SHEET_MEDIA_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String EXCEL_FILE_NAME = "my_debate_template.xlsx";

    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        if (isPreflight(request)) {
            return true;
        }
        if (isExcelExportRequest(handler)) {
            setExcelHeader(response);
        }
        return true;
    }

    private boolean isExcelExportRequest(Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return false;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        return handlerMethod.hasMethodAnnotation(ExcelExport.class)
                && handlerMethod.getBeanType().isAnnotationPresent(RestController.class);
    }

    private boolean isPreflight(HttpServletRequest request) {
        return HttpMethod.OPTIONS.toString()
                .equals(request.getMethod());
    }

    private void setExcelHeader(HttpServletResponse response) {
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(EXCEL_FILE_NAME)
                .build();
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
        response.setContentType(SPREAD_SHEET_MEDIA_TYPE);
    }
}
