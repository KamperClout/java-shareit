package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.OrchestratorService;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingForItem;
import ru.practicum.shareit.exceptions.BookingNotFoundException;
import ru.practicum.shareit.exceptions.UserNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.Status.REJECTED;
import static ru.practicum.shareit.booking.Status.WAITING;

@Service
@Slf4j
public class BookingService {
    private final BookingStorage bookingStorage;
    private final BookingMapper bookingMapper;
    private final OrchestratorService orchestratorService;

    @Autowired
    @Lazy
    public BookingService(BookingStorage bookingStorage,
                          BookingMapper bookingMapper, OrchestratorService orchestratorService) {
        this.bookingStorage = bookingStorage;
        this.bookingMapper = bookingMapper;
        this.orchestratorService = orchestratorService;
    }

    public BookingDto createBooking(BookingForItem bookingForItemDto, Long bookerId) {
        if (!orchestratorService.isExistUser(bookerId)) {
            throw new UserNotFoundException("Пользователь с ID=" + bookerId + " не найден!");
        }
        if (bookingForItemDto.getStart().isAfter(bookingForItemDto.getEnd()) ||
                bookingForItemDto.getStart().equals(bookingForItemDto.getEnd())) {
            throw new ValidationException("Дата начала позже или равна окончанию бронирования");
        }

        if (!orchestratorService.isAvailableItem(bookingForItemDto.getItemId())) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }
        Booking booking = bookingMapper.toBooking(bookingForItemDto, bookerId);
        if (bookerId.equals(booking.getItem().getOwner().getId())) {
            throw new BookingNotFoundException("Вещь с id=" + bookingForItemDto.getItemId() +
                    " недоступна для бронирования самим владельцем!");
        }
        return bookingMapper.toBookingDto(bookingStorage.save(booking));
    }

    public BookingDto update(Long bookingId, Long userId, Boolean approved) {
        if (!orchestratorService.isExistUser(userId)) {
            throw new UserNotFoundException("Пользователь с ID=" + userId + " не найден!");
        }
        Booking booking = bookingStorage.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Бронирование с ID=" + bookingId + " не найдено!"));
        if (booking.getEnd().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Время бронирования истекло!");
        }

        if (booking.getBooker().getId().equals(userId) && approved) {
            throw new UserNotFoundException("Подтвердить бронирование может только владелец вещи!");
        } else if ((orchestratorService.isItemOwner(booking.getItem().getId(), userId)) &&
                (!booking.getStatus().equals(Status.CANCELED))) {
            if (!booking.getStatus().equals(WAITING)) {
                throw new ValidationException("Решение по бронированию уже принято!");
            }
            if (approved) {
                booking.setStatus(Status.APPROVED);
                log.info("Пользователь с ID={} подтвердил бронирование с ID={}", userId, bookingId);
            } else {
                booking.setStatus(Status.REJECTED);
                log.info("Пользователь с ID={} отклонил бронирование с ID={}", userId, bookingId);
            }
        } else {
            if (booking.getStatus().equals(Status.CANCELED)) {
                throw new ValidationException("Бронирование было отменено!");
            }
        }

        return bookingMapper.toBookingDto(bookingStorage.save(booking));
    }

    public BookingDto getBookingById(Long bookingId, Long userId) {
        if (!orchestratorService.isExistUser(userId)) {
            throw new UserNotFoundException("Пользователь с ID=" + userId + " не найден!");
        }
        Booking booking = bookingStorage.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Бронирование с ID=" + bookingId + " не найдено!"));
        if (!booking.getBooker().getId().equals(userId) && !orchestratorService.isItemOwner(booking.getItem().getId(), userId)) {
            throw new UserNotFoundException("Посмотреть данные бронирования может только владелец вещи" +
                    " или бронирующий ее!");
        }
        return bookingMapper.toBookingDto(booking);
    }

    public List<BookingDto> getBookings(String state, Long userId, PageRequest pageReq) {
        if (!orchestratorService.isExistUser(userId)) {
            throw new UserNotFoundException("Пользователь с ID=" + userId + " не найден!");
        }
        User booker = orchestratorService.findUserById(userId);
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        List<Booking> bookings;
        switch (state) {
            case "ALL":
                bookings = bookingStorage.findAllByBookerId(userId, sort, pageReq);
                break;
            case "WAITING":
                bookings = bookingStorage.findAllByBookerIdAndStatus(booker.getId(),
                        WAITING, sort);
                break;
            case "REJECTED":
                bookings = bookingStorage.findAllByBookerIdAndStatus(booker.getId(),
                        REJECTED, sort);
                break;
            case "PAST":
                bookings = bookingStorage.findAllByBookerIdAndEndBefore(booker.getId(),
                        LocalDateTime.now(), sort);
                break;
            case "FUTURE":
                bookings = bookingStorage.findAllByBookerIdAndStartAfter(booker.getId(),
                        LocalDateTime.now(), sort);
                break;
            case "CURRENT":
                bookings = bookingStorage.findAllByBookerIdAndStartBeforeAndEndAfter(booker.getId(),
                        LocalDateTime.now(), LocalDateTime.now(), sort);
                break;
            default:
                throw new ValidationException("Unknown state: " + state);
        }
        return bookings
                .stream()
                .map(bookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }


    public List<BookingDto> getBookingsOwner(String state, Long userId, PageRequest pageReq) {
        if (!orchestratorService.isExistUser(userId)) {
            throw new UserNotFoundException("Пользователь с ID=" + userId + " не найден!");
        }
        List<Booking> bookings;
        Sort sortByStartDesc = Sort.by(Sort.Direction.DESC, "start");
        switch (state) {
            case "ALL":
                bookings = bookingStorage.findByItem_Owner_Id(userId, sortByStartDesc, pageReq);
                break;
            case "CURRENT":
                bookings = bookingStorage.findByItem_Owner_IdAndStartIsBeforeAndEndIsAfter(userId, LocalDateTime.now(),
                        LocalDateTime.now(), sortByStartDesc);
                break;
            case "PAST":
                bookings = bookingStorage.findByItem_Owner_IdAndEndIsBefore(userId, LocalDateTime.now(), sortByStartDesc);
                break;
            case "FUTURE":
                bookings = bookingStorage.findByItem_Owner_IdAndStartIsAfter(userId, LocalDateTime.now(),
                        sortByStartDesc);
                break;
            case "WAITING":
                bookings = bookingStorage.findByItem_Owner_IdAndStatus(userId, WAITING, sortByStartDesc);
                break;
            case "REJECTED":
                bookings = bookingStorage.findByItem_Owner_IdAndStatus(userId, REJECTED, sortByStartDesc);
                break;
            default:
                throw new ValidationException("Unknown state: " + state);
        }
        return bookings.stream()
                .map(bookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    public Booking getBookingWithUserBookedItem(Long itemId, Long userId) {
        return bookingStorage.findFirstByItem_IdAndBooker_IdAndEndIsBeforeAndStatus(itemId,
                userId, LocalDateTime.now(), Status.APPROVED);
    }
}
