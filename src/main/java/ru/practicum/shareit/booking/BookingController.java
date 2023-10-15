package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingForItem;
import ru.practicum.shareit.exceptions.BadRequestException;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@Slf4j
public class BookingController {
    private static final String USER_ID = "X-Sharer-User-Id";
    private final BookingService service;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.service = bookingService;
    }

    @ResponseBody
    @PostMapping
    public BookingDto create(@Valid @RequestBody BookingForItem bookingForItemDto,
                             @RequestHeader(USER_ID) Long bookerId) {
        log.info("Получен POST-запрос: '/bookings' " +
                "на создание бронирования от пользователя с ID={}", bookerId);
        return service.createBooking(bookingForItemDto, bookerId);
    }

    @ResponseBody
    @PatchMapping("/{bookingId}")
    public BookingDto update(@PathVariable Long bookingId,
                             @RequestHeader(USER_ID) Long userId, @RequestParam Boolean approved) {
        log.info("Получен PATCH-запрос: '/bookings' на обновление статуса бронирования с ID={}", bookingId);
        return service.update(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(@PathVariable Long bookingId, @RequestHeader(USER_ID) Long userId) {
        log.info("Получен GET-запрос: '/bookings' на получение бронирования с ID={}", bookingId);
        return service.getBookingById(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> getBookings(@RequestParam(name = "state", defaultValue = "ALL") String state,
                                        @RequestHeader(USER_ID) Long userId, @RequestParam(defaultValue = "0") int from,
                                        @RequestParam(defaultValue = "20") int size) {
        log.info("Получен GET-запрос: '/bookings' на получение " +
                "списка всех бронирований пользователя с ID={} с параметром STATE={}", userId, state);
        if (from < 0 || size <= 0) {
            throw new BadRequestException("параметры пагинации не могут быть отрицательными или равны нулю");
        }
        return service.getBookings(state, userId, PageRequest.of(from / size, size));
    }

    @GetMapping("/owner")
    public List<BookingDto> getBookingsOwner(@RequestParam(name = "state", defaultValue = "ALL") String state,
                                             @RequestHeader(USER_ID) Long userId, @RequestParam(defaultValue = "0") int from,
                                             @RequestParam(defaultValue = "20") int size) {
        log.info("Получен GET-запрос: '/bookings/owner' на получение " +
                "списка всех бронирований вещей пользователя с ID={} с параметром STATE={}", userId, state);
        if (from < 0 || size <= 0) {
            throw new BadRequestException("параметры пагинации не могут быть отрицательными или равны нулю");
        }
        return service.getBookingsOwner(state, userId, PageRequest.of(from / size, size));
    }
}
