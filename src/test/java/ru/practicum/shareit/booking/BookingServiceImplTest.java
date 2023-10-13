package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.OrchestratorService;
import ru.practicum.shareit.exceptions.BookingNotFoundException;
import ru.practicum.shareit.exceptions.UserNotFoundException;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTest {
    @Mock
    BookingStorage bookingStorage;
    @Mock
    OrchestratorService orchestratorService;

    @Test
    void shouldExceptionWhenGetBookingWithWrongId() {
        BookingService bookingService = new BookingService(bookingStorage, null,
                orchestratorService);
        when(orchestratorService.isExistUser(any(Long.class)))
                .thenReturn(true);
        when(bookingStorage.findById(any(Long.class)))
                .thenReturn(Optional.empty());
        final BookingNotFoundException exception = Assertions.assertThrows(
                BookingNotFoundException.class,
                () -> bookingService.getBookingById(-1L, 1L));
        Assertions.assertEquals("Бронирование с ID=-1 не найдено!", exception.getMessage());
    }

    @Test
    void shouldExceptionWhenGetBookingWithWrongUserId() {
        BookingService bookingService = new BookingService(bookingStorage, null,
                orchestratorService);
        when(orchestratorService.isExistUser(any(Long.class)))
                .thenReturn(false);
        final UserNotFoundException exception = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> bookingService.getBookingById(1L, 1L));
        Assertions.assertEquals("Пользователь с ID=1 не найден!", exception.getMessage());
    }

    @Test
    void shouldExceptionWhenGetBookingsWithWrongUserId() {
        BookingService bookingService = new BookingService(bookingStorage, null,
                orchestratorService);
        when(orchestratorService.isExistUser(any(Long.class)))
                .thenReturn(false);
        final UserNotFoundException exception = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> bookingService.getBookings("CURRENT", 1L, null));
        Assertions.assertEquals("Пользователь с ID=1 не найден!", exception.getMessage());
    }

    @Test
    void shouldExceptionWhenGetBookingsOwnerWithWrongUserId() {
        BookingService bookingService = new BookingService(bookingStorage, null,
                orchestratorService);
        when(orchestratorService.isExistUser(any(Long.class)))
                .thenReturn(false);
        final UserNotFoundException exception = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> bookingService.getBookingsOwner("CURRENT", 1L, null));
        Assertions.assertEquals("Пользователь с ID=1 не найден!", exception.getMessage());
    }
}