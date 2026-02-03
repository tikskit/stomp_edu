package ru.tikskit.stompintro.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.security.Principal;

@Component
@Log4j2
public class StompEventListener {
    @EventListener
    public void handleSessionConnectEvent(SessionConnectEvent event) {
        SimpMessageHeaderAccessor a = SimpMessageHeaderAccessor.wrap(event.getMessage());
        Principal user = a.getUser();
        log.info("Подключение пользователя {}, сессия {}", user.getName(), a.getSessionId());
    }

    @EventListener
    public void handleSessionDisconnectEvent(SessionDisconnectEvent event) {
        SimpMessageHeaderAccessor a = SimpMessageHeaderAccessor.wrap(event.getMessage());
        Principal user = a.getUser();
        log.info("Отключение пользователя {}, сессия {}", user.getName(), a.getSessionId());
    }

    @EventListener
    public void handleSessionSubscribeEvent(SessionSubscribeEvent event) {
        SimpMessageHeaderAccessor a = SimpMessageHeaderAccessor.wrap(event.getMessage());

        log.info("Подписка пользователя {}, сессия {} на destination {}", a.getUser().getName(), a.getSessionId(),
                a.getDestination());
    }

    @EventListener
    public void handleSessionUnsubscribeEvent(SessionUnsubscribeEvent event) {
        SimpMessageHeaderAccessor a = SimpMessageHeaderAccessor.wrap(event.getMessage());

        log.info("Отписка пользователя {}, сессия {} на destination {}", a.getUser().getName(), a.getSessionId(),
                a.getDestination());

    }
}
