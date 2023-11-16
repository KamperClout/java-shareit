package ru.practicum.shareit.exceptions;

import lombok.Generated;

@Generated
public class BookingNotFoundException extends IllegalArgumentException {
    public BookingNotFoundException(String message) {
        super(message);
    }
}
