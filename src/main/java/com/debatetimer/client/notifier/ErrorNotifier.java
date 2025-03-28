package com.debatetimer.client.notifier;

public interface ErrorNotifier {

    void sendErrorMessage(Throwable throwable);
}
