package com.debatetimer.client;

public interface ChannelNotifier {

    void sendErrorMessage(Throwable throwable);
}
