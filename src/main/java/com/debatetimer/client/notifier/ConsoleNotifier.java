package com.debatetimer.client.notifier;

import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsoleNotifier implements ErrorNotifier {

    private static final String ERROR_SEND_MESSAGE = "에러 정보가 채널로 발송되었습니다";

    @Override
    public void sendErrorMessage(Throwable throwable) {
        log.error("{} : {}", ERROR_SEND_MESSAGE, throwable);
        log.error(getStackTraceAsString(throwable));
    }

    private String getStackTraceAsString(Throwable throwable) {
        return Arrays.stream(throwable.getStackTrace())
                .map(StackTraceElement::toString)
                .collect(Collectors.joining(System.lineSeparator()));
    }
}
