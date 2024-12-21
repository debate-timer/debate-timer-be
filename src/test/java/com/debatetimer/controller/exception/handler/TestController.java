package com.debatetimer.controller.exception.handler;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("test")
@RestController
public class TestController {

    @GetMapping("/test")
    void testMethod() throws Exception {
    }
}
