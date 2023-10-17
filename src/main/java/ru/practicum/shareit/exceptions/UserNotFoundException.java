package ru.practicum.shareit.exceptions;

import lombok.Generated;

@Generated
public class UserNotFoundException extends IllegalArgumentException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
