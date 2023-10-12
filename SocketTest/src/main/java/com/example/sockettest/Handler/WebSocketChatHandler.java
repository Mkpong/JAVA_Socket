package com.example.sockettest.Handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.w3c.dom.Text;

import java.util.logging.Logger;
@Component
public class WebSocketChatHandler extends TextWebSocketHandler {
    private final static Logger LOG = Logger.getGlobal();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String input = message.getPayload();
        LOG.info(input);
        TextMessage textMessage = new TextMessage("Web Socket Test");
        session.sendMessage(textMessage);
    }
}
