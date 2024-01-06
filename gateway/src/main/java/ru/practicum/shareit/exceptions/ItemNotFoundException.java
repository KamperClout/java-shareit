package ru.practicum.shareit.exceptions;

import lombok.Generated;

@Generated
public class ItemNotFoundException extends IllegalArgumentException {
    public ItemNotFoundException(String message) {
        super(message);
    }
}
