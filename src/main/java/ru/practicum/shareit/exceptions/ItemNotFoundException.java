package ru.practicum.shareit.exceptions;

public class ItemNotFoundException extends IllegalArgumentException {
    public ItemNotFoundException(String message) {
        super(message);
    }
}
