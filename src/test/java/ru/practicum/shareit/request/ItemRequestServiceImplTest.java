package ru.practicum.shareit.request;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.OrchestratorService;
import ru.practicum.shareit.exceptions.ItemRequestNotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceImplTest {
    @Mock
    private ItemRequestStorage itemRequestStorage;
    @Mock
    private OrchestratorService orchestratorService;
    private ItemRequestService itemRequestService;
    private ItemRequestMapper itemRequestMapper;

    private UserDto userDto = new UserDto(1L, "Alex", "alex@alex.ru");

    private ItemRequestDto itemRequestDto = new ItemRequestDto(1L, "ItemRequest description",
            userDto, LocalDateTime.of(2022, 1, 2, 3, 4, 5), null);

    @BeforeEach
    void beforeEach() {
        itemRequestService = new ItemRequestService(itemRequestStorage,
                orchestratorService, itemRequestMapper, null, null);
    }

    @Test
    void shouldExceptionWhenGetItemRequestWithWrongId() {
        when(orchestratorService.isExistUser(any(Long.class)))
                .thenReturn(true);
        when(itemRequestStorage.findById(any(Long.class)))
                .thenReturn(Optional.empty());
        final ItemRequestNotFoundException exception = Assertions.assertThrows(
                ItemRequestNotFoundException.class,
                () -> itemRequestService.getItemRequestOfId(-1L, 1L));
        Assertions.assertEquals("запроса с id 1 нет", exception.getMessage());
    }
}