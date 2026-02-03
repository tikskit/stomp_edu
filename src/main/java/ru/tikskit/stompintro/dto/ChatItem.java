package ru.tikskit.stompintro.dto;

import java.time.ZonedDateTime;

public record ChatItem(String author, String message, ZonedDateTime timestamp) {
}
