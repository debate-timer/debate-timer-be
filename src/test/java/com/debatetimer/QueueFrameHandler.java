package com.debatetimer;

import java.lang.reflect.Type;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;

/**
 * 여러 메시지를 순서대로 받아야 하는 STOMP 테스트에서 사용한다.
 */
public class QueueFrameHandler<T> implements StompFrameHandler {

    private final BlockingQueue<T> messages = new LinkedBlockingQueue<>();
    private final Class<T> tClass;

    public QueueFrameHandler(Class<T> tClass) {
        this.tClass = tClass;
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return tClass;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handleFrame(StompHeaders headers, Object payload) {
        messages.add((T) payload);
    }

    public T poll(long timeoutSeconds) throws InterruptedException {
        return messages.poll(timeoutSeconds, TimeUnit.SECONDS);
    }
}
