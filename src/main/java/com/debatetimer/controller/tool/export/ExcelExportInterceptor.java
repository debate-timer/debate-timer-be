package com.debatetimer.controller.tool.export;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class ExcelExportInterceptor implements HandlerInterceptor {

    private static final String SPREAD_SHEET_MEDIA_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    public void postHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            @Nullable ModelAndView modelAndView
    ) throws IOException {
        response.getOutputStream().flush();
//        ContentDisposition attachContent = ContentDisposition.attachment().build();
//        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sample.xlsx");
//        response.setHeader(HttpHeaders.CONTENT_TYPE, SPREAD_SHEET_MEDIA_TYPE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sample.xlsx");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        System.out.println("------ 통과합니다.");

    }

    private HttpHeaders getExcelHeader(String fileName) {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(
                new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        );
        header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        return header;
    }
}
