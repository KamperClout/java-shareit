package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.OrchestratorService;
import ru.practicum.shareit.booking.*;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.exceptions.ItemNotFoundException;
import ru.practicum.shareit.exceptions.UserNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.ItemRequestStorage;


import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;


@Service
public class ItemService {
    private final ItemStorage itemStorage;

    private final CommentStorage commentStorage;

    private final BookingStorage bookingStorage;

    private final ItemMapper itemMapper;

    private final BookingMapper bookingMapper;

    private final OrchestratorService orchestratorService;

    private final ItemRequestStorage itemRequestStorage;

    private final ItemRequestMapper itemRequestMapper;

    @Autowired
    @Lazy
    public ItemService(ItemStorage itemStorage, CommentStorage commentStorage,
                       BookingStorage bookingStorage, ItemMapper itemMapper,
                       BookingMapper bookingMapper, OrchestratorService orchestratorService,
                       ItemRequestStorage itemRequestStorage, ItemRequestMapper itemRequestMapper) {
        this.itemStorage = itemStorage;
        this.commentStorage = commentStorage;
        this.bookingStorage = bookingStorage;
        this.itemMapper = itemMapper;
        this.bookingMapper = bookingMapper;
        this.orchestratorService = orchestratorService;
        this.itemRequestStorage = itemRequestStorage;
        this.itemRequestMapper = itemRequestMapper;
    }

    public ItemDto create(ItemDto itemDto, Long ownerId) {
        Optional<Long> requestId = Optional.ofNullable(itemDto.getRequestId());
        ItemRequest request = null;

        if (requestId.isPresent() && requestId.get() > 0L) {
            request = itemRequestStorage.findById(requestId.get())
                    .orElseThrow(() -> new NoSuchElementException("запрос c id " + requestId + " не существует"));
        }
        if (!orchestratorService.isExistUser(ownerId)) {
            throw new UserNotFoundException("Пользователь с ID=" + ownerId + " не найден!");
        }
        if (request != null) {
            itemDto.setRequestId(request.getId());
        }
        return itemMapper.toItemDto(itemStorage.save(itemMapper.toItem(itemDto, ownerId)));
    }

    @Transactional(readOnly = true)
    public List<ItemDto> findAllItems(Long ownerId, PageRequest pageReq) {
        List<ItemDto> itemsDto = itemStorage.findByOwnerId(ownerId).stream()
                .map(itemMapper::toItemOwnerDto)
                .sorted(Comparator.comparing(ItemDto::getId))
                .collect(Collectors.toList());
        List<Booking> bookings = bookingStorage.findAllByOwnerId(ownerId,
                Sort.by(Sort.Direction.ASC, "start"), pageReq);
        List<BookingShortDto> bookingDtoShorts = bookings.stream()
                .map(bookingMapper::toBookingShortDto)
                .collect(Collectors.toList());
        itemsDto.forEach(itemDto -> {
            setBookings(itemDto, bookingDtoShorts);
        });
        return itemsDto;
    }

    @Transactional(readOnly = true)
    public ItemDto getItemById(Long id, Long userId) {
        ItemDto itemDto;
        Item item = itemStorage.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Вещь с ID=" + id + " не найдена!"));
        List<Booking> bookings = bookingStorage.findByItemIdAndStatus(id, Status.APPROVED,
                Sort.by(Sort.Direction.ASC, "start"));
        List<BookingShortDto> bookingDtoShorts = bookings.stream()
                .map(bookingMapper::toBookingShortDto)
                .collect(Collectors.toList());
        if (userId.equals(item.getOwner().getId())) {
            itemDto = itemMapper.toItemOwnerDto(item);
            setBookings(itemDto, bookingDtoShorts);
        } else {
            itemDto = itemMapper.toItemDto(item);
        }
        return itemDto;
    }

    public Item findItemById(Long id) {
        return itemStorage.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Вещь с id=" + id + " не найдена"));
    }


    @Transactional
    public ItemDto update(ItemDto itemDto, Long ownerId, Long itemId) {
        if (!orchestratorService.isExistUser(ownerId)) {
            throw new UserNotFoundException("Пользователь с ID=" + ownerId + " не найден!");
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
        return itemMapper.toItemDto(itemStorage.save(item));
    }

    public void delete(Long itemId, Long ownerId) {
        Item item = itemStorage.findById(itemId).orElseThrow(() -> new ItemNotFoundException("Вещь с id " + itemId + " не найдена"));
        if (!item.getOwner().getId().equals(ownerId)) {
            throw new ItemNotFoundException("У пользователя нет такой вещи!");
        }
        itemStorage.deleteById(itemId);
    }

    @Transactional(readOnly = true)
    public List<ItemDto> getItemsBySearchQuery(String text, PageRequest pageReq) {
        List<ItemDto> searchItems = new ArrayList<>();
        if (!text.isBlank()) {
            searchItems = itemStorage.getItemsBySearchQuery(text.toLowerCase(), pageReq)
                    .stream()
                    .map(itemMapper::toItemDto)
                    .collect(Collectors.toList());
        }
        return searchItems;
    }

    @Transactional
    public CommentDto createComment(CommentDto commentDto, Long itemId, Long userId) {
        if (!orchestratorService.isExistUser(userId)) {
            throw new UserNotFoundException("Пользователь с ID=" + userId + " не найден!");
        }
        Comment comment = new Comment();
        Booking booking = orchestratorService.getBookingWithUserBookedItem(itemId, userId);
        if (booking == null) {
            throw new ValidationException("Данный пользователь вещь не бронировал!");
        }
        comment.setCreated(LocalDateTime.now());
        comment.setItem(booking.getItem());
        comment.setAuthor(booking.getBooker());
        comment.setText(commentDto.getText());
        return itemMapper.toCommentDto(commentStorage.save(comment));
    }

    public List<CommentDto> getCommentsByItemId(Long itemId) {
        return commentStorage.findAllByItem_Id(itemId,
                        Sort.by(Sort.Direction.DESC, "created")).stream()
                .map(itemMapper::toCommentDto)
                .collect(toList());
    }

    private void setBookings(ItemDto itemDto, List<BookingShortDto> bookings) {
        itemDto.setLastBooking(bookings.stream()
                .filter(booking -> booking.getItem().getId().equals(itemDto.getId()) &&
                        booking.getStartTime().isBefore(LocalDateTime.now()))
                .reduce((bookRes, bookPrev) -> bookPrev).orElse(null));
        itemDto.setNextBooking(bookings.stream()
                .filter(booking -> booking.getItem().getId().equals(itemDto.getId()) &&
                        booking.getStartTime().isAfter(LocalDateTime.now()))
                .reduce((bookNext, bookRes) -> bookNext).orElse(null));
    }

    public List<ItemDto> getItemsByRequestId(Long requestId) {
        return itemStorage.findAllByRequestId(requestId,
                        Sort.by(Sort.Direction.DESC, "id")).stream()
                .map(itemMapper::toItemDto)
                .collect(toList());
    }
}
