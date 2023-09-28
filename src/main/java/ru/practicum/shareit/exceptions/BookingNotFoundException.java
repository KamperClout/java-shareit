package ru.practicum.shareit.exceptions;

public class BookingNotFoundException extends IllegalArgumentException {
    public BookingNotFoundException(String message) {
        super(message);
    }
}
