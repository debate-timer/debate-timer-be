package com.debatetimer;

import com.debatetimer.fixture.HeaderGenerator;
import com.debatetimer.fixture.entity.MemberGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

@ExtendWith(DataBaseCleaner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseStompTest {

    private static final String SOCKET_ENDPOINT = "/ws";

    protected StompSession stompSession;

    @LocalServerPort
    private int port;

    private final String url;

    private final WebSocketStompClient websocketClient;

    @Autowired
    protected MemberGenerator memberGenerator;

    @Autowired
    protected HeaderGenerator headerGenerator;

    public BaseStompTest() {
        List<Transport> transports = List.of(new WebSocketTransport(new StandardWebSocketClient()));
        this.websocketClient = new WebSocketStompClient(new SockJsClient(transports));
        this.websocketClient.setMessageConverter(buildMessageConverter());
        this.url = "ws://localhost:";
    }

    private MessageConverter buildMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.findAndRegisterModules();
        converter.setObjectMapper(objectMapper);
        return converter;
    }

    @BeforeEach
    public void connect() throws ExecutionException, InterruptedException, TimeoutException {
        this.stompSession = this.websocketClient
                .connectAsync(url + port + SOCKET_ENDPOINT, new StompSessionHandlerAdapter() {
                })
                .get(3, TimeUnit.SECONDS);
    }

    @AfterEach
    public void disconnect() {
        if (this.stompSession.isConnected()) {
            this.stompSession.disconnect();
        }
    }
}
