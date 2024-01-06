package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exceptions.BadRequestException;

import javax.validation.Valid;
import javax.validation.constraints.Min;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;
    private static final String OWNER = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> addBooking(@RequestHeader(OWNER) @Min(1) Long ownerId,
                                             @Valid @RequestBody BookItemRequestDto bookingDto) {
        log.info("запрос на добавление бронирования: " + bookingDto);
        return bookingClient.addBooking(ownerId, bookingDto);
    }

    @GetMapping
    public ResponseEntity<Object> getBookings(@RequestHeader(OWNER) @Min(1) Long userId,
                                              @RequestParam(name = "state", defaultValue = "all") String stateParam,
                                              @Min(0) @RequestParam(defaultValue = "0") Integer from,
                                              @Min(1) @RequestParam(defaultValue = "10") Integer size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new BadRequestException("Unknown state: " + stateParam));
        log.info("Получение бронирования со статусом {}, userId={}, from={}, size={}", stateParam, userId, from, size);
        return bookingClient.getBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingOwner(@RequestHeader(OWNER) @Min(1) Long userId,
                                                  @RequestParam(name = "state", defaultValue = "all") String stateParam,
                                                  @Min(0) @RequestParam(defaultValue = "0") Integer from,
                                                  @Min(1) @RequestParam(defaultValue = "10") Integer size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new BadRequestException("Unknown state: " + stateParam));
        log.info("получение бронирования со статусом {}, userId={}, from={}, size={}", stateParam, userId, from, size);
        return bookingClient.getBookingCurrentOwner(userId, state, from, size);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@RequestHeader(OWNER) @Min(1) Long userId,
                                             @PathVariable @Min(1) Long bookingId) {
        log.info("Get booking {}, userId={}", bookingId, userId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveStatus(@RequestHeader(OWNER) @Min(1) Long userId,
                                                @PathVariable @Min(1) Long bookingId,
                                                @RequestParam boolean approved) {
        log.info("Подтверждение статуса бронирования {}", bookingId);
        return bookingClient.approveStatus(userId, bookingId, approved);
    }


}
