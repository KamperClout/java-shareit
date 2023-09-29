package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingForItem;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;

@Component
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
        if (booking != null) {
            return new BookingDto(
                    booking.getId(),
                    booking.getStart(),
                    booking.getEnd(),
                    itemMapper.toItemDto(booking.getItem()),
                    UserMapper.toUserDto(booking.getBooker()),
                    booking.getStatus()
            );
        } else {
            return null;
        }
    }

    public BookingShortDto toBookingShortDto(Booking booking) {
        if (booking != null) {
            return new BookingShortDto(
                    booking.getId(),
                    booking.getItem(),
                    booking.getBooker().getId(),
                    booking.getStart(),
                    booking.getEnd()
            );
        } else {
            return null;
        }
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

    public BookingForItem toBookingForItem(Booking booking) {
        return new BookingForItem(
                booking.getItem().getId(),
                booking.getStart(),
                booking.getEnd()
        );
    }

}
