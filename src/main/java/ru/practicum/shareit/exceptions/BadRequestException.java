package ru.practicum.shareit.exceptions;

import lombok.Generated;

@Generated
public class BadRequestException extends IllegalArgumentException {
    public BadRequestException(String message) {
        super(message);
    }
}
