package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.OrchestratorService;
import ru.practicum.shareit.exceptions.UserNotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestServiceTest {

    private final ItemRequestService itemRequestService;

    private final UserService userService;

    private final OrchestratorService orchestratorService;
    private UserDto userDto1 = new UserDto(101L, "One", "one@one.ru");
    private UserDto userDto2 = new UserDto(102L, "Two", "two@two.ru");

    private ItemRequestDto itemRequestDto = new ItemRequestDto(100L, "ItemRequest description",
            userDto1, LocalDateTime.of(2022, 1, 2, 3, 4, 5), null);

    @Test
    void shouldCreateItemRequest() {
        UserDto newUserDto = userService.create(userDto1);
        ItemRequestDto returnRequestDto = itemRequestService.addItemRequest(newUserDto.getId(), itemRequestDto,
                LocalDateTime.of(2022, 1, 2, 3, 4, 5));
        assertThat(returnRequestDto.getDescription(), equalTo(itemRequestDto.getDescription()));
    }

    @Test
    void shouldExceptionWhenCreateItemRequestWithWrongUserId() {
        UserNotFoundException exp = assertThrows(UserNotFoundException.class,
                () -> orchestratorService.isExistUser(-2L));
        assertEquals("Пользователь с ID=-2 не найден!", exp.getMessage());
    }

    @Test
    void shouldExceptionWhenGetItemRequestWithWrongId() {
        UserDto firstUserDto = userService.create(userDto1);
        UserNotFoundException exp = assertThrows(UserNotFoundException.class,
                () -> itemRequestService.getItemRequestOfId(-2L, firstUserDto.getId()));
        assertEquals("Пользователь с ID=-2 не найден!", exp.getMessage());
    }

    @Test
    void shouldReturnAllItemRequestsWhenSizeNotNullAndNull() {
        UserDto firstUserDto = userService.create(userDto1);
        UserDto newUserDto = userService.create(userDto2);
        ItemRequestDto returnOneRequestDto = itemRequestService.addItemRequest(newUserDto.getId(), itemRequestDto,
                LocalDateTime.of(2023, 1, 2, 3, 4, 5));
        ItemRequestDto returnTwoRequestDto = itemRequestService.addItemRequest(newUserDto.getId(), itemRequestDto,
                LocalDateTime.of(2024, 1, 2, 3, 4, 5));
        List<ItemRequestDto> listItemRequest = itemRequestService.getItemRequestsOtherSorted(firstUserDto.getId(),
                null);
        assertThat(listItemRequest.size(), equalTo(2));
    }

    @Test
    void shouldReturnAllItemRequestsWhenSizeNull() {
        UserDto firstUserDto = userService.create(userDto1);
        UserDto newUserDto = userService.create(userDto2);
        ItemRequestDto returnOneRequestDto = itemRequestService.addItemRequest(newUserDto.getId(), itemRequestDto,
                LocalDateTime.of(2023, 1, 2, 3, 4, 5));
        ItemRequestDto returnTwoRequestDto = itemRequestService.addItemRequest(newUserDto.getId(), itemRequestDto,
                LocalDateTime.of(2024, 1, 2, 3, 4, 5));
        List<ItemRequestDto> listItemRequest = itemRequestService.getItemRequestsOtherSorted(firstUserDto.getId(),
                null);
        assertThat(listItemRequest.size(), equalTo(2));
    }

    @Test
    void shouldReturnOwnItemRequests() {
        UserDto firstUserDto = userService.create(userDto1);
        UserDto newUserDto = userService.create(userDto2);
        ItemRequestDto returnOneRequestDto = itemRequestService.addItemRequest(newUserDto.getId(), itemRequestDto,
                LocalDateTime.of(2023, 1, 2, 3, 4, 5));
        ItemRequestDto returnTwoRequestDto = itemRequestService.addItemRequest(newUserDto.getId(), itemRequestDto,
                LocalDateTime.of(2024, 1, 2, 3, 4, 5));
        List<ItemRequestDto> listItemRequest = itemRequestService.getItemRequestsOwnerSorted(newUserDto.getId(), null);
        System.out.println(listItemRequest.toString());
        assertThat(listItemRequest.size(), equalTo(2));
    }

    @Test
    void shouldReturnItemRequestById() {
        UserDto firstUserDto = userService.create(userDto1);
        ItemRequestDto newItemRequestDto = itemRequestService.addItemRequest(firstUserDto.getId(), itemRequestDto,
                LocalDateTime.of(2023, 1, 2, 3, 4, 5));
        ItemRequestDto returnItemRequestDto = itemRequestService.getItemRequestOfId(firstUserDto.getId(), newItemRequestDto.getId());
        assertThat(returnItemRequestDto.getDescription(), equalTo(itemRequestDto.getDescription()));
    }

    @Test
    void shouldReturnExceptionItemRequestById() {
        UserDto firstUserDto = userService.create(userDto1);
        ItemRequestDto newItemRequestDto = itemRequestService.addItemRequest(firstUserDto.getId(), itemRequestDto,
                LocalDateTime.of(2023, 1, 2, 3, 4, 5));
        UserNotFoundException exp = assertThrows(UserNotFoundException.class,
                () -> itemRequestService.getItemRequestOfId(-2L, newItemRequestDto.getId()));
        assertEquals("Пользователь с ID=-2 не найден!", exp.getMessage());
    }
}
