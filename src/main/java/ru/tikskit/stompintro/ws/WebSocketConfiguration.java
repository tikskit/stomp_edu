package ru.tikskit.stompintro.ws;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@Log4j2
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public WebSocketConfiguration(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        /*
        Что означает enableSimpleBroker("/topic", "/queue")
            /topic/** — обычно используют как pub/sub (один отправил, много подписчиков получили).
                Клиент подписывается, например: /topic/chat
                Сервер отправляет, например: /topic/chat
            /queue/** — обычно используют как point-to-point (очередь, чаще “одному получателю”).
                Клиент подписывается, например: /queue/notifications
                Сервер отправляет, например: /queue/notifications

        Почему выбраны именно /topic и /queue
            Это конвенция Spring (почти во всех туториалах так), но ты можешь выбрать любые префиксы — главное, чтобы
            клиент и сервер использовали одинаковые.
         */
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*"); // эндпоинт для подключенияSecurityConfig
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor == null) {
                    return message;
                }

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String login = accessor.getLogin();
                    String passcode = accessor.getPasscode();

                    if (login == null || passcode == null) {
                        throw new BadCredentialsException("Missing STOMP login/passcode");
                    }

                    UserDetails user = userDetailsService.loadUserByUsername(login);
                    if (!passwordEncoder.matches(passcode, user.getPassword())) {
                        throw new BadCredentialsException("Invalid STOMP credentials");
                    }

                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            null,
                            user.getAuthorities()
                    );
                    accessor.setUser(authentication);
                    log.info("Пользователь {} успешно выполнил вход", user.getUsername());
                }

                return message;
            }
        });
    }
}
