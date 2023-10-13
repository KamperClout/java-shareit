package ru.practicum.shareit;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingForItem;
import ru.practicum.shareit.exceptions.UserNotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class OrchestratorServiceTest {
    private final OrchestratorService orchestratorService;

    private final UserService userService;
    private final ItemService itemService;
    private final BookingService bookingService;
    private final ItemMapper itemMapper;
    private User user = new User(1L, "User", "first@first.ru");
    private User user2 = new User(2L, "Second", "second@second.ru");
    private Item item = new Item(1L, "Item1", "Description1", true, user, null);

    @Test
    void shouldReturnTrueWhenExistUser() {
        UserDto newUserDto = userService.create(UserMapper.toUserDto(user));
        assertTrue(orchestratorService.isExistUser(newUserDto.getId()));
    }

    @Test
    void shouldReturnExceptionWhenExistUser() {
        UserDto newUserDto = UserMapper.toUserDto(new User(6L, "six", "six@six.ru"));
        assertThrows(UserNotFoundException.class, () -> orchestratorService.isExistUser(newUserDto.getId()));
    }

    @Test
    void shouldReturnTrueWhenItemAvailable() {
        UserDto newUserDto = userService.create(UserMapper.toUserDto(user));
        ItemDto newItemDto = itemService.create(itemMapper.toItemDto(item), newUserDto.getId());
        assertTrue(orchestratorService.isAvailableItem(newItemDto.getId()));
    }

    @Test
    void shouldReturnTrueWhenIsItemOwner() {
        UserDto newUserDto = userService.create(UserMapper.toUserDto(user));
        ItemDto newItemDto = itemService.create(itemMapper.toItemDto(item), newUserDto.getId());
        assertTrue(orchestratorService.isItemOwner(newItemDto.getId(), newUserDto.getId()));
    }

    @Test
    void shouldReturnBookingWithUserBookedItem() {
        UserDto firstUserDto = userService.create(UserMapper.toUserDto(user));
        UserDto secondUserDto = userService.create(UserMapper.toUserDto(user2));
        ItemDto newItemDto = itemService.create(itemMapper.toItemDto(item), firstUserDto.getId());
        BookingForItem bookingForItem = new BookingForItem(
                newItemDto.getId(),
                LocalDateTime.now().plusSeconds(1),
                LocalDateTime.now().plusSeconds(3)
        );
        BookingDto bookingDto = bookingService.createBooking(bookingForItem, secondUserDto.getId());
        bookingService.update(bookingDto.getId(), firstUserDto.getId(), true);
        try {
            sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Assertions.assertEquals(orchestratorService.getBookingWithUserBookedItem(newItemDto.getId(),
                secondUserDto.getId()).getId(), bookingDto.getId());
    }

}