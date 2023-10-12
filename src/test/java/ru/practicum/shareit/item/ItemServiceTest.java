package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingForItem;
import ru.practicum.shareit.exceptions.ItemNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.lang.Thread.sleep;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {
    @Mock
    private ItemStorage storage;
    private final ItemService itemService;
    private final UserService userService;
    private final ItemMapper itemMapper;
    private final BookingService bookingService;
    private final User user = new User(200L, "First", "first@yandex.ru");
    private final UserDto userDto1 = new UserDto(201L, "PavelOne", "pavelone@yandex.ru");
    private final UserDto userDto2 = new UserDto(202L, "PavelTwo", "paveltwo@yandex.ru");
    private final ItemDto itemDto = new ItemDto(200L, "Item1", "Description1", true,
            user, null, null, null, null);
    private final ItemDto itemDto2 = new ItemDto(202L, "Item2", "Description2", true,
            user, null, null, null, null);

    @Test
    void shouldCreateItem() {
        UserDto newUserDto = userService.create(userDto1);
        ItemDto newItemDto = itemService.create(itemDto, newUserDto.getId());
        ItemDto returnItemDto = itemService.getItemById(newItemDto.getId(), newUserDto.getId());
        assertThat(returnItemDto.getDescription(), equalTo(itemDto.getDescription()));
    }

    @Test
    void shouldDeleteItemWhenUserNotOwner() {
        UserDto ownerDto = userService.create(userDto1);
        UserDto newUserDto = userService.create(userDto2);
        ItemDto newItemDto = itemService.create(itemDto, ownerDto.getId());
        ItemNotFoundException exp = assertThrows(ItemNotFoundException.class,
                () -> itemService.delete(newItemDto.getId(), newUserDto.getId()));
        assertEquals("У пользователя нет такой вещи!", exp.getMessage());
    }

    @Test
    void shouldDeleteWhenUserIsOwner() {
        UserDto ownerDto = userService.create(userDto1);
        ItemDto newItemDto = itemService.create(itemDto, ownerDto.getId());
        itemService.delete(newItemDto.getId(), ownerDto.getId());
        ItemNotFoundException exp = assertThrows(ItemNotFoundException.class,
                () -> itemService.getItemById(newItemDto.getId(), ownerDto.getId()));
        assertEquals("Вещь с ID=" + newItemDto.getId() + " не найдена!", exp.getMessage());
    }

    @Test
    void shouldExceptionWhenDeleteItemNotExist() {
        UserDto ownerDto = userService.create(userDto1);
        ItemNotFoundException exp = assertThrows(ItemNotFoundException.class,
                () -> itemService.delete(-2L, ownerDto.getId()));
        assertEquals("Вещь с id -2 не найдена", exp.getMessage());
    }

    @Test
    void shouldUpdateItem() {
        UserDto newUserDto = userService.create(userDto1);
        ItemDto newItemDto = itemService.create(itemDto, newUserDto.getId());
        newItemDto.setName("NewName");
        newItemDto.setDescription("NewDescription");
        newItemDto.setAvailable(false);
        ItemDto returnItemDto = itemService.update(newItemDto, newUserDto.getId(), newItemDto.getId());
        assertThat(returnItemDto.getName(), equalTo("NewName"));
        assertThat(returnItemDto.getDescription(), equalTo("NewDescription"));
        assertFalse(returnItemDto.getAvailable());
    }

    @Test
    void shouldExceptionWhenUpdateItemNotOwner() {
        UserDto ownerDto = userService.create(userDto1);
        UserDto newUserDto = userService.create(userDto2);
        ItemDto newItemDto = itemService.create(itemDto, ownerDto.getId());
        ItemNotFoundException exp = assertThrows(ItemNotFoundException.class,
                () -> itemService.update(newItemDto, newUserDto.getId(), newItemDto.getId()));
        assertEquals("У пользователя нет такой вещи!", exp.getMessage());
    }

    @Test
    void shouldReturnItemsByOwner() {
        UserDto ownerDto = userService.create(userDto1);
        itemService.create(itemDto, ownerDto.getId());
        itemService.create(itemDto2, ownerDto.getId());
        List<ItemDto> listItems = itemService.findAllItems(ownerDto.getId(), null);
        assertEquals(2, listItems.size());
    }

    @Test
    void shouldReturnItemsBySearch() {
        UserDto ownerDto = userService.create(userDto1);
        itemService.create(itemDto, ownerDto.getId());
        itemService.create(itemDto2, ownerDto.getId());
        List<ItemDto> listItems = itemService.getItemsBySearchQuery("item", null);
        assertEquals(2, listItems.size());
    }

    @Test
    void shouldReturnItemsBySearchWhenSizeIsNull() {
        UserDto ownerDto = userService.create(userDto1);
        itemService.create(itemDto, ownerDto.getId());
        itemService.create(itemDto2, ownerDto.getId());
        List<ItemDto> listItems = itemService.getItemsBySearchQuery("item", null);
        assertEquals(2, listItems.size());
    }

    @Test
    void shouldExceptionWhenCreateCommentWhenUserNotBooker() {
        UserDto ownerDto = userService.create(userDto1);
        UserDto newUserDto = userService.create(userDto2);
        ItemDto newItemDto = itemService.create(itemDto, ownerDto.getId());
        CommentDto commentDto = new CommentDto(1L, "Comment1", itemMapper.toItem(itemDto, ownerDto.getId()),
                newUserDto.getName(), LocalDateTime.now());
        ValidationException exp = assertThrows(ValidationException.class,
                () -> itemService.createComment(commentDto, itemDto.getId(), newUserDto.getId()));
        assertEquals("Данный пользователь вещь не бронировал!", exp.getMessage());
    }

    @Test
    void shouldCreateComment() {
        UserDto ownerDto = userService.create(userDto1);
        UserDto newUserDto = userService.create(userDto2);
        ItemDto newItemDto = itemService.create(itemDto, ownerDto.getId());
        BookingForItem bookingForItem = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.now().plusSeconds(1),
                LocalDateTime.now().plusSeconds(3)
        );
        BookingDto bookingDto = bookingService.createBooking(bookingForItem, newUserDto.getId());
        bookingService.update(bookingDto.getId(), ownerDto.getId(), true);
        try {
            sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        CommentDto commentDto = new CommentDto(1L, "Comment1", itemMapper.toItem(itemDto, ownerDto.getId()),
                newUserDto.getName(), LocalDateTime.now());
        itemService.createComment(commentDto, newItemDto.getId(), newUserDto.getId());
        Assertions.assertEquals(1, itemService.getCommentsByItemId(newItemDto.getId()).size());
    }

    @Test
    void shouldExceptionWhenGetItemWithWrongId() {
        ItemService itemService = new ItemService(storage, null, null,
                null, null, null, null, null);
        when(storage.findById(any(Long.class)))
                .thenReturn(Optional.empty());
        final ItemNotFoundException exception = Assertions.assertThrows(
                ItemNotFoundException.class,
                () -> itemService.getItemById(-1L, 1L));
        Assertions.assertEquals("Вещь с ID=-1 не найдена!", exception.getMessage());
    }
}