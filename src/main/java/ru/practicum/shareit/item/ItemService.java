package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingStorage;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.exceptions.ItemNotFoundException;
import ru.practicum.shareit.exceptions.UserNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.mapper.Mapper;
import ru.practicum.shareit.user.UserService;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;


@Service
public class ItemService {
    private final ItemStorage itemStorage;
    private final UserService userService;

    private final CommentStorage commentStorage;

    private final BookingService bookingService;

    private final BookingStorage bookingStorage;

    @Autowired
    @Lazy
    public ItemService(ItemStorage itemStorage, UserService userService, CommentStorage commentStorage,
                       BookingService bookingService, BookingStorage bookingStorage) {
        this.itemStorage = itemStorage;
        this.userService = userService;
        this.commentStorage = commentStorage;
        this.bookingService = bookingService;
        this.bookingStorage = bookingStorage;
    }

    public ItemDto create(ItemDto itemDto, Long ownerId) {
        if (userService.getUserById(ownerId) == null) {
            throw new UserNotFoundException("Пользователь с id=" + ownerId + " не найден");
        }
        return Mapper.toItemDto(itemStorage.save(Mapper.toItem(itemDto, ownerId)));
    }

    public List<ItemDto> findAllItems(Long ownerId) {
        List<ItemDto> itemsDto = itemStorage.findByOwnerId(ownerId).stream()
                .map(Mapper::toItemOwnerDto)
                .collect(Collectors.toList());
        List<Booking> bookings = bookingStorage.findAllByOwnerId(ownerId,
                Sort.by(Sort.Direction.ASC, "start"));
        List<BookingShortDto> bookingDtoShorts = bookings.stream()
                .map(Mapper::toBookingShortDto)
                .collect(Collectors.toList());
        itemsDto.forEach(itemDto -> {
            setBookings(itemDto, bookingDtoShorts);
        });
        return itemsDto;
    }

    public ItemDto getItemById(Long id, Long userId) {
        ItemDto itemDto;
        Item item = itemStorage.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Вещь с ID=" + id + " не найдена!"));
        List<Booking> bookings = bookingStorage.findByItemIdAndStatus(id, Status.APPROVED,
                Sort.by(Sort.Direction.ASC, "start"));
        List<BookingShortDto> bookingDtoShorts = bookings.stream()
                .map(Mapper::toBookingShortDto)
                .collect(Collectors.toList());
        if (userId.equals(item.getOwner().getId())) {
            itemDto = Mapper.toItemOwnerDto(item);
            setBookings(itemDto, bookingDtoShorts);
        } else {
            itemDto = Mapper.toItemDto(item);
        }
        return itemDto;
    }

    public Item findItemById(Long id) {
        return itemStorage.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Вещь с id=" + id + " не найдена"));
    }


    public ItemDto update(ItemDto itemDto, Long ownerId, Long itemId) {
        if (userService.getUserById(ownerId) == null) {
            throw new UserNotFoundException("Пользователь с id=" + ownerId + " не найден!");
        }
        Item item = itemStorage.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Вещь с ID=" + itemId + " не найдена!"));
        if (!item.getOwner().getId().equals(ownerId)) {
            throw new ItemNotFoundException("У пользователя нет такой вещи!");
        }
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        return Mapper.toItemDto(itemStorage.save(item));
    }

    public void delete(Long itemId, Long ownerId) {
        Item item = itemStorage.findById(itemId).orElseThrow(() -> new ItemNotFoundException("Вещь с id " + itemId + " не найдена"));
        if (!item.getOwner().equals(ownerId)) {
            throw new ItemNotFoundException("У пользователя нет такой вещи!");
        }
        itemStorage.deleteById(itemId);
    }

    public List<ItemDto> getItemsBySearchQuery(String text) {
        List<ItemDto> searchItems = new ArrayList<>();
        if (!text.isBlank()) {
            text = text.toLowerCase();
            searchItems = itemStorage.getItemsBySearchQuery(text)
                    .stream()
                    .map(Mapper::toItemDto)
                    .collect(Collectors.toList());
        }
        return searchItems;
    }

    public CommentDto createComment(CommentDto commentDto, Long itemId, Long userId) {
        if (userService.getUserById(userId) == null) {
            throw new UserNotFoundException("Пользователь с id " + userId + " не найден!");
        }
        Comment comment = new Comment();
        Booking booking = bookingService.getBookingWithUserBookedItem(itemId, userId);
        if (booking != null) {
            comment.setCreated(LocalDateTime.now());
            comment.setItem(booking.getItem());
            comment.setAuthor(booking.getBooker());
            comment.setText(commentDto.getText());
        } else {
            throw new ValidationException("Данный пользователь вещь не бронировал!");
        }
        return Mapper.toCommentDto(commentStorage.save(comment));
    }

    public List<CommentDto> getCommentsByItemId(Long itemId) {
        return commentStorage.findAllByItem_Id(itemId,
                        Sort.by(Sort.Direction.DESC, "created")).stream()
                .map(Mapper::toCommentDto)
                .collect(toList());
    }

    private void setBookings(ItemDto itemDto, List<BookingShortDto> bookings) {
        itemDto.setLastBooking(bookings.stream()
                .filter(booking -> booking.getItem().getId() == itemDto.getId() &&
                        booking.getStartTime().isBefore(LocalDateTime.now()))
                .reduce((a, b) -> b).orElse(null));
        itemDto.setNextBooking(bookings.stream()
                .filter(booking -> booking.getItem().getId() == itemDto.getId() &&
                        booking.getStartTime().isAfter(LocalDateTime.now()))
                .reduce((a, b) -> a).orElse(null));
    }
}
