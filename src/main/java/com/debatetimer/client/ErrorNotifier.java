package com.debatetimer.client;

public interface ErrorNotifier {

    void sendErrorMessage(Throwable throwable);
}
