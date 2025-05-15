package com.melly.timerocketserver.websocket.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    // 세션 관리용 Map (나중엔 Redis로 확장 가능)
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();   // 동시 접속자가 많은 환경에서 스레드 안정성 확보

    @Override   // 클라이언트가 WebSocket에 처음 연결되었을 때 호출
    public void afterConnectionEstablished(WebSocketSession session) {
        // sessions 라는 Map 에 현재 연결된 WebSocket 클라이언트의 세션 객체(WebSocketSession)를 저장
        sessions.put(session.getId(), session);
        log.info("연결됨:{}", session.getId());
    }

    @Override   // 클라이언트가 텍스트 메시지를 보냈을 때 호출
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        log.info("수신된 메시지:{}", message.getPayload());
        // 연결된 모든 세션에 메시지 브로드캐스트
        for (WebSocketSession s : sessions.values()) {
            if (s.isOpen()) {
                s.sendMessage(message);
            }
        }
    }

    @Override   // 클라이언트가 연결을 끊었을 때 호출
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        log.info("연결 종료:{}", session.getId());
    }
}
