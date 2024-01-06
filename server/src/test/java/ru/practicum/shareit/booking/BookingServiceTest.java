package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingForItem;
import ru.practicum.shareit.exceptions.BookingNotFoundException;
import ru.practicum.shareit.exceptions.UserNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceTest {
    final BookingService bookingService;
    final UserService userService;
    final ItemService itemService;
    User user = new User(300L, "First", "first@first300.ru");
    UserDto userDto1 = new UserDto(301L, "PavelOne", "pavelone@yandex.ru");
    UserDto userDto2 = new UserDto(302L, "PavelTwo", "paveltwo@yandex.ru");
    ItemDto itemDto1 = new ItemDto(301L, "Item1", "Description1", true,
            user, null, null, null, null);
    ItemDto itemDto2 = new ItemDto(302L, "Item2", "Description2", true,
            user, null, null, null, null);


    @Test
    void shouldExceptionWhenCreateBookingByOwnerItem() {
        UserDto ownerDto = userService.create(userDto1);
        ItemDto newItemDto = itemService.create(itemDto1, ownerDto.getId());
        BookingForItem bookingForItem = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 00, 00),
                LocalDateTime.of(2030, 12, 26, 12, 00, 00));
        BookingNotFoundException exp = assertThrows(BookingNotFoundException.class,
                () -> bookingService.createBooking(bookingForItem, ownerDto.getId()));
        assertEquals("Вещь с id=" + newItemDto.getId() + " недоступна для бронирования самим владельцем!",
                exp.getMessage());
    }

    @Test
    void shouldExceptionWhenGetBookingByNotOwnerOrNotBooker() {
        UserDto ownerDto = userService.create(userDto1);
        UserDto newUserDto = userService.create(userDto2);
        UserDto userDto3 = new UserDto(303L, "AlexThird", "alexthird@alex300.ru");
        userDto3 = userService.create(userDto3);
        Long userId = userDto3.getId();
        ItemDto newItemDto = itemService.create(itemDto1, ownerDto.getId());
        BookingForItem bookingInputDto = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 00, 00),
                LocalDateTime.of(2030, 12, 26, 12, 00, 00));
        BookingDto bookingDto = bookingService.createBooking(bookingInputDto, newUserDto.getId());
        UserNotFoundException exp = assertThrows(UserNotFoundException.class,
                () -> bookingService.getBookingById(bookingDto.getId(), userId));
        assertEquals("Посмотреть данные бронирования может только владелец вещи или бронирующий ее!",
                exp.getMessage());
    }

    @Test
    void shouldReturnBookingsWhenGetBookingsByBookerAndSizeIsNull() {
        UserDto ownerDto = userService.create(userDto1);
        UserDto newUserDto = userService.create(userDto2);
        ItemDto newItemDto = itemService.create(itemDto1, ownerDto.getId());
        BookingForItem bookingForItem = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 00, 00),
                LocalDateTime.of(2030, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem, newUserDto.getId());
        BookingForItem bookingForItem1 = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 00, 00),
                LocalDateTime.of(2031, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem1, newUserDto.getId());
        List<BookingDto> listBookings = bookingService.getBookings("ALL", newUserDto.getId(), null);
        assertEquals(2, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetBookingsByBookerAndSizeIsNotNull() {
        UserDto ownerDto = userService.create(userDto1);
        UserDto newUserDto = userService.create(userDto2);
        ItemDto newItemDto = itemService.create(itemDto1, ownerDto.getId());
        BookingForItem bookingForItem = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 00, 00),
                LocalDateTime.of(2030, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem, newUserDto.getId());
        BookingForItem bookingForItem1 = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 00, 00),
                LocalDateTime.of(2031, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem1, newUserDto.getId());
        List<BookingDto> listBookings = bookingService.getBookings("ALL", newUserDto.getId(), null);
        assertEquals(2, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetBookingsInWaitingStatusByBookerAndSizeIsNull() {
        UserDto ownerDto = userService.create(userDto1);
        UserDto newUserDto = userService.create(userDto2);
        ItemDto newItemDto = itemService.create(itemDto1, ownerDto.getId());
        BookingForItem bookingForItem = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 00, 00),
                LocalDateTime.of(2030, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem, newUserDto.getId());
        BookingForItem bookingForItem1 = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 00, 00),
                LocalDateTime.of(2031, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem1, newUserDto.getId());
        List<BookingDto> listBookings = bookingService.getBookings("WAITING", newUserDto.getId(), null);
        assertEquals(2, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetBookingsInWaitingStatusByBookerAndSizeNotNull() {
        UserDto ownerDto = userService.create(userDto1);
        UserDto newUserDto = userService.create(userDto2);
        ItemDto newItemDto = itemService.create(itemDto1, ownerDto.getId());
        BookingForItem bookingForItem = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 00, 00),
                LocalDateTime.of(2030, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem, newUserDto.getId());
        BookingForItem bookingForItem1 = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 00, 00),
                LocalDateTime.of(2031, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem1, newUserDto.getId());
        List<BookingDto> listBookings = bookingService.getBookings("WAITING", newUserDto.getId(), null);
        assertEquals(2, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetBookingsInRejectedStatusByBookerAndSizeIsNull() {
        UserDto ownerDto = userService.create(userDto1);
        UserDto newUserDto = userService.create(userDto2);
        ItemDto newItemDto = itemService.create(itemDto1, ownerDto.getId());
        BookingForItem bookingForItem = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 00, 00),
                LocalDateTime.of(2030, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem, newUserDto.getId());
        BookingForItem bookingForItem1 = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 00, 00),
                LocalDateTime.of(2031, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem1, newUserDto.getId());
        List<BookingDto> listBookings = bookingService.getBookings("REJECTED", newUserDto.getId(), null);
        assertEquals(0, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetBookingsInRejectedStatusByBookerAndSizeNotNull() {
        UserDto ownerDto = userService.create(userDto1);
        UserDto newUserDto = userService.create(userDto2);
        ItemDto newItemDto = itemService.create(itemDto1, ownerDto.getId());
        BookingForItem bookingForItem = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 00, 00),
                LocalDateTime.of(2030, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem, newUserDto.getId());
        BookingForItem bookingForItem1 = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 00, 00),
                LocalDateTime.of(2031, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem1, newUserDto.getId());
        List<BookingDto> listBookings = bookingService.getBookings("REJECTED", newUserDto.getId(),
                null);
        assertEquals(0, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetBookingsByOwnerAndSizeIsNull() {
        UserDto ownerDto = userService.create(userDto1);
        UserDto newUserDto = userService.create(userDto2);
        ItemDto newItemDto = itemService.create(itemDto1, ownerDto.getId());
        BookingForItem bookingForItem = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 00, 00),
                LocalDateTime.of(2030, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem, newUserDto.getId());
        BookingForItem bookingForItem1 = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 00, 00),
                LocalDateTime.of(2031, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem1, newUserDto.getId());
        List<BookingDto> listBookings = bookingService.getBookingsOwner("ALL", ownerDto.getId(), null);
        assertEquals(2, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetBookingsByOwnerAndSizeNotNull() {
        UserDto ownerDto = userService.create(userDto1);
        UserDto newUserDto = userService.create(userDto2);
        ItemDto newItemDto = itemService.create(itemDto1, ownerDto.getId());
        BookingForItem bookingForItem = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 00, 00),
                LocalDateTime.of(2030, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem, newUserDto.getId());
        BookingForItem bookingForItem1 = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 00, 00),
                LocalDateTime.of(2031, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem1, newUserDto.getId());
        List<BookingDto> listBookings = bookingService.getBookingsOwner("ALL", ownerDto.getId(),
                null);
        assertEquals(2, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetBookingsByOwnerAndStatusWaitingAndSizeIsNull() {
        UserDto ownerDto = userService.create(userDto1);
        UserDto newUserDto = userService.create(userDto2);
        ItemDto newItemDto = itemService.create(itemDto1, ownerDto.getId());
        BookingForItem bookingForItem = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 00, 00),
                LocalDateTime.of(2030, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem, newUserDto.getId());
        BookingForItem bookingForItem1 = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 00, 00),
                LocalDateTime.of(2031, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem1, newUserDto.getId());
        List<BookingDto> listBookings = bookingService.getBookingsOwner("WAITING", ownerDto.getId(),
                null);
        assertEquals(2, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetBookingsByOwnerAndStatusWaitingAndSizeNotNull() {
        UserDto ownerDto = userService.create(userDto1);
        UserDto newUserDto = userService.create(userDto2);
        ItemDto newItemDto = itemService.create(itemDto1, ownerDto.getId());
        BookingForItem bookingForItem = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 00, 00),
                LocalDateTime.of(2030, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem, newUserDto.getId());
        BookingForItem bookingForItem1 = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 00, 00),
                LocalDateTime.of(2031, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem1, newUserDto.getId());
        List<BookingDto> listBookings = bookingService.getBookingsOwner("WAITING", ownerDto.getId(),
                null);
        assertEquals(2, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetBookingsByOwnerAndStatusRejectedAndSizeIsNull() {
        UserDto ownerDto = userService.create(userDto1);
        UserDto newUserDto = userService.create(userDto2);
        ItemDto newItemDto = itemService.create(itemDto1, ownerDto.getId());
        BookingForItem bookingForItem = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 00, 00),
                LocalDateTime.of(2030, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem, newUserDto.getId());
        BookingForItem bookingForItem1 = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 00, 00),
                LocalDateTime.of(2031, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem1, newUserDto.getId());
        List<BookingDto> listBookings = bookingService.getBookingsOwner("REJECTED", ownerDto.getId(),
                null);
        assertEquals(0, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetBookingsByOwnerAndStatusRejectedAndSizeNotNull() {
        UserDto ownerDto = userService.create(userDto1);
        UserDto newUserDto = userService.create(userDto2);
        ItemDto newItemDto = itemService.create(itemDto1, ownerDto.getId());
        BookingForItem bookingForItem = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 00, 00),
                LocalDateTime.of(2030, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem, newUserDto.getId());
        BookingForItem bookingForItem1 = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 00, 00),
                LocalDateTime.of(2031, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem, newUserDto.getId());
        List<BookingDto> listBookings = bookingService.getBookingsOwner("REJECTED", ownerDto.getId(),
                null);
        assertEquals(0, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetBookingsByOwnerAndStateCurrentAndSizeNotNull() {
        UserDto ownerDto = userService.create(userDto1);
        UserDto newUserDto = userService.create(userDto2);
        ItemDto newItemDto = itemService.create(itemDto1, ownerDto.getId());
        BookingForItem bookingForItem = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 00, 00),
                LocalDateTime.of(2030, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem, newUserDto.getId());
        BookingForItem bookingForItem1 = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 00, 00),
                LocalDateTime.of(2031, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem, newUserDto.getId());
        List<BookingDto> listBookings = bookingService.getBookingsOwner("CURRENT", ownerDto.getId(),
                null);
        assertEquals(0, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetBookingsByOwnerAndStateFutureAndSizeNotNull() {
        UserDto ownerDto = userService.create(userDto1);
        UserDto newUserDto = userService.create(userDto2);
        ItemDto newItemDto = itemService.create(itemDto1, ownerDto.getId());
        BookingForItem bookingForItem = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 00, 00),
                LocalDateTime.of(2030, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem, newUserDto.getId());
        BookingForItem bookingForItem1 = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 00, 00),
                LocalDateTime.of(2031, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem, newUserDto.getId());
        List<BookingDto> listBookings = bookingService.getBookingsOwner("FUTURE", ownerDto.getId(),
                null);
        assertEquals(2, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetBookingsByOwnerAndStatePastAndSizeNotNull() {
        UserDto ownerDto = userService.create(userDto1);
        UserDto newUserDto = userService.create(userDto2);
        ItemDto newItemDto = itemService.create(itemDto1, ownerDto.getId());
        BookingForItem bookingForItem = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 00, 00),
                LocalDateTime.of(2030, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem, newUserDto.getId());
        BookingForItem bookingForItem1 = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 00, 00),
                LocalDateTime.of(2031, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem, newUserDto.getId());
        List<BookingDto> listBookings = bookingService.getBookingsOwner("PAST", ownerDto.getId(),
                null);
        assertEquals(0, listBookings.size());
    }

    @Test
    void shouldReturnExceptionWhenGetBookingsByOwnerAndStateDefaultAndSizeNotNull() {
        UserDto ownerDto = userService.create(userDto1);
        UserDto newUserDto = userService.create(userDto2);
        ItemDto newItemDto = itemService.create(itemDto1, ownerDto.getId());
        BookingForItem bookingForItem = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 00, 00),
                LocalDateTime.of(2030, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem, newUserDto.getId());
        BookingForItem bookingForItem1 = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 00, 00),
                LocalDateTime.of(2031, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem, newUserDto.getId());
        ValidationException exp = assertThrows(ValidationException.class,
                () -> bookingService.getBookingsOwner("ABCD", ownerDto.getId(),
                        null));
        assertEquals("Unknown state: " + "ABCD",
                exp.getMessage());
    }

    @Test
    void shouldReturnBookingsWhenGetBookingsInPastStatusByBookerAndSizeIsNull() {
        UserDto ownerDto = userService.create(userDto1);
        UserDto newUserDto = userService.create(userDto2);
        ItemDto newItemDto = itemService.create(itemDto1, ownerDto.getId());
        BookingForItem bookingForItem = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 00, 00),
                LocalDateTime.of(2030, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem, newUserDto.getId());
        BookingForItem bookingForItem1 = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 00, 00),
                LocalDateTime.of(2031, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem1, newUserDto.getId());
        List<BookingDto> listBookings = bookingService.getBookings("PAST", newUserDto.getId(), null);
        assertEquals(0, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetBookingsInFutureStatusByBookerAndSizeIsNull() {
        UserDto ownerDto = userService.create(userDto1);
        UserDto newUserDto = userService.create(userDto2);
        ItemDto newItemDto = itemService.create(itemDto1, ownerDto.getId());
        BookingForItem bookingForItem = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 00, 00),
                LocalDateTime.of(2030, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem, newUserDto.getId());
        BookingForItem bookingForItem1 = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 00, 00),
                LocalDateTime.of(2031, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem1, newUserDto.getId());
        List<BookingDto> listBookings = bookingService.getBookings("FUTURE", newUserDto.getId(), null);
        assertEquals(2, listBookings.size());
    }

    @Test
    void shouldReturnBookingsWhenGetBookingsInCurrentStatusByBookerAndSizeIsNull() {
        UserDto ownerDto = userService.create(userDto1);
        UserDto newUserDto = userService.create(userDto2);
        ItemDto newItemDto = itemService.create(itemDto1, ownerDto.getId());
        BookingForItem bookingForItem = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 00, 00),
                LocalDateTime.of(2030, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem, newUserDto.getId());
        BookingForItem bookingForItem1 = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 00, 00),
                LocalDateTime.of(2031, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem1, newUserDto.getId());
        List<BookingDto> listBookings = bookingService.getBookings("CURRENT", newUserDto.getId(), null);
        assertEquals(0, listBookings.size());
    }

    @Test
    void shouldReturnExceptionWhenGetBookingsAndStateDefaultAndSizeNotNull() {
        UserDto ownerDto = userService.create(userDto1);
        UserDto newUserDto = userService.create(userDto2);
        ItemDto newItemDto = itemService.create(itemDto1, ownerDto.getId());
        BookingForItem bookingForItem = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2030, 12, 25, 12, 00, 00),
                LocalDateTime.of(2030, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem, newUserDto.getId());
        BookingForItem bookingForItem1 = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.of(2031, 12, 25, 12, 00, 00),
                LocalDateTime.of(2031, 12, 26, 12, 00, 00));
        bookingService.createBooking(bookingForItem, newUserDto.getId());
        ValidationException exp = assertThrows(ValidationException.class,
                () -> bookingService.getBookings("ABCD", ownerDto.getId(),
                        null));
        assertEquals("Unknown state: " + "ABCD",
                exp.getMessage());
    }
}