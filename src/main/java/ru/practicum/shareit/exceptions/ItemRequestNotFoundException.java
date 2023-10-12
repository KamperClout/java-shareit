package ru.practicum.shareit.exceptions;

public class ItemRequestNotFoundException extends IllegalArgumentException {
    public ItemRequestNotFoundException(String message) {
        super(message);
    }
}