package ru.tikskit.stompintro.ws;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.tikskit.stompintro.dto.ChatError;
import ru.tikskit.stompintro.dto.ChatItem;
import ru.tikskit.stompintro.exceptions.AddChatItemException;

import java.security.Principal;
import java.time.ZonedDateTime;

@Controller
@Log4j2
@RequiredArgsConstructor
public class WSController {
    private final SimpMessagingTemplate template;

    @MessageMapping("/chat")
    public ChatItem gotMessage(@Payload String message, SimpMessageHeaderAccessor accessor) {
        if (Math.random() < 0.5) {
            throw new AddChatItemException("Ошибка добавления сообщения в чат");
        }
        Principal user = accessor.getUser();
        return new ChatItem(user.getName(), message, ZonedDateTime.now());
    }

    @PostMapping("/system")
    public void sendSystemMessage(@RequestParam String message) {
        template.convertAndSend("/topic/chat", new ChatItem("System", message, ZonedDateTime.now()));
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public ChatError errorHandler(AddChatItemException exception) {
        return new ChatError(String.format("Ошибка добавления сообщения в чат: %s, сообщение не было добавлено", exception.getMessage()));
    }
}
