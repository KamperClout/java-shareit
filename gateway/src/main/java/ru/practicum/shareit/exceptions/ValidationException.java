package ru.practicum.shareit.exceptions;

import lombok.Generated;

@Generated
public class ValidationException extends IllegalArgumentException {
    public ValidationException(String message) {
        super(message);
    }
}
