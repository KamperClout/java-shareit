package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exceptions.BadRequestException;
import ru.practicum.shareit.exceptions.UserNotFoundException;
import ru.practicum.shareit.item.CommentStorage;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserStorage;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemRequestControllerMetTest {
    @Autowired
    private ItemRequestController requestController;
    @Autowired
    private UserController userController;
    @Autowired
    private ItemRequestMapper mapper;
    @Autowired
    private UserStorage userStorage;
    @Autowired
    private ItemRequestStorage itemRequestStorage;
    @Autowired
    private CommentStorage commentStorage;
    private ItemRequestDto itemRequestDto;
    private UserDto userDto;

    @BeforeEach
    public void clearContext() {
        commentStorage.deleteAll();
        itemRequestStorage.deleteAll();
        userStorage.deleteAll();

        userDto = new UserDto(1L, "Pavel", "kamperinc@yandex.ru");
        userDto.setName("name");
        userDto.setEmail("user@email.com");
        itemRequestDto = new ItemRequestDto(
                0L, "item request description", null, LocalDateTime.now(), new ArrayList<>());

    }

    @Test
    void createTest() {
        UserDto user = userController.create(userDto);
        ItemRequestDto itemRequest = requestController.addItemRequest(user.getId(), itemRequestDto);
        assert itemRequest != null;
        List<ItemDto> items = itemRequest.getItems();
        ItemRequest request = mapper.toItemRequest(itemRequest, user.getId(), LocalDateTime.now());
        User user1 = request.getRequester();

        assertEquals(1L, requestController.getItemRequestOfId(user.getId(), itemRequest.getId()).getId());
    }

    @Test
    void findWithItemTest() {
        UserDto user = userController.create(userDto);
        UserDto user2 = userController.create(new UserDto(0L, "name", "user2@email.com"));
        ItemRequestDto itemRequest = requestController.addItemRequest(user.getId(), itemRequestDto);
        Item item = new Item(0L, "item", "desc", true, UserMapper.toUser(user2),
                mapper.toItemRequest(itemRequest, user.getId(), LocalDateTime.now()).getId());

        assertEquals(0, requestController.getItemRequestsOtherSorted(user.getId(), 0, 20).size());
        assertEquals(0, requestController.getItemRequestsOwnerSorted(user2.getId(), 0, 20).size());
    }

    @Test
    void findWithBadPagination() {
        UserDto user = userController.create(userDto);
        UserDto user2 = userController.create(new UserDto(0L, "name", "user2@email.com"));
        ItemRequestDto itemRequest = requestController.addItemRequest(user.getId(), itemRequestDto);
        Item item = new Item(0L, "item", "desc", true, UserMapper.toUser(user2),
                mapper.toItemRequest(itemRequest, user.getId(), LocalDateTime.now()).getId());

        assertThrows(BadRequestException.class, () -> requestController.getItemRequestsOwnerSorted(
                1L, -1, 0).size());
    }

    @Test
    void createByWrongUserTest() {
        assertThrows(UserNotFoundException.class, () -> requestController.addItemRequest(1L, itemRequestDto));
    }

    @Test
    void getAllByOwnerTest() {
        UserDto user = userController.create(userDto);
        requestController.addItemRequest(user.getId(), itemRequestDto);
        assertEquals(1, requestController.getItemRequestsOwnerSorted(user.getId(),
                0, 20).size());
    }

    @Test
    void getAllByUserWithWrongUserTest() {
        assertThrows(UserNotFoundException.class, () -> requestController.getItemRequestsOtherSorted(1L,
                0, 20));
    }

    @Test
    void getAll() {
        UserDto user = userController.create(userDto);
        requestController.addItemRequest(user.getId(), itemRequestDto);
        assertEquals(0, requestController.getItemRequestsOtherSorted(user.getId(),
                0, 10).size());

        UserDto user2 = userController.create(new UserDto(0L, "name", "user3@email.com"));
        assertEquals(1, requestController.getItemRequestsOtherSorted(
                user2.getId(), 0, 1).size());
    }

    @Test
    void getAllByWrongUser() {
        assertThrows(UserNotFoundException.class, () -> requestController.getItemRequestsOtherSorted(
                1L, 0, 10));
    }

    @Test
    void getAllWithWrongFrom() {
        userController.create(userDto);
        assertThrows(BadRequestException.class, () -> requestController.getItemRequestsOtherSorted(
                1L, -1, 10));
    }
}