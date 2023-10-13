package ru.practicum.shareit.exceptions;

import lombok.Generated;

@Generated
public class UserAlreadyExistsException extends IllegalArgumentException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
