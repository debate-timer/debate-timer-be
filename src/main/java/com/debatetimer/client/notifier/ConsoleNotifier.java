package com.debatetimer.client.notifier;

public class ConsoleNotifier implements ErrorNotifier {

    private static final String ERROR_SEND_MESSAGE = "에러 정보가 채널로 발송되었습니다.";

    @Override
    public void sendErrorMessage(Throwable throwable) {
        System.out.println(ERROR_SEND_MESSAGE);
    }
}
