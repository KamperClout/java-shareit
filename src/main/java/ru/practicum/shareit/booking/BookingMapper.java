package ru.practicum.shareit.booking;

import lombok.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingForItem;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.exceptions.BookingNotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;

@Component
@Generated
public class BookingMapper {

    private final UserService userService;
    private final ItemService itemService;

    private final ItemMapper itemMapper;

    @Autowired
    @Lazy
    public BookingMapper(UserService userService, ItemService itemService, ItemMapper itemMapper) {
        this.userService = userService;
        this.itemService = itemService;
        this.itemMapper = itemMapper;
    }

    public BookingDto toBookingDto(Booking booking) {
        if (booking == null) {
            throw new BookingNotFoundException("Бронирование не найдено!");
        }
        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                itemMapper.toItemDto(booking.getItem()),
                UserMapper.toUserDto(booking.getBooker()),
                booking.getStatus()
        );
    }

    public BookingShortDto toBookingShortDto(Booking booking) {
        if (booking == null) {
            throw new BookingNotFoundException("Бронирование не найдено!");
        }
        return new BookingShortDto(
                booking.getId(),
                booking.getItem(),
                booking.getBooker().getId(),
                booking.getStart(),
                booking.getEnd()
        );
    }

    public Booking toBooking(BookingForItem bookingForItemDto, Long bookerId) {
        return new Booking(
                null,
                bookingForItemDto.getStart(),
                bookingForItemDto.getEnd(),
                itemService.findItemById(bookingForItemDto.getItemId()),
                userService.findUserById(bookerId),
                Status.WAITING
        );
    }
}
