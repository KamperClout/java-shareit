package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemRequestService itemRequestService;

    @Autowired
    MockMvc mvc;

    static final String USER_ID = "X-Sharer-User-Id";

    private UserDto userDto = new UserDto(1L, "Pavel", "kamperinc@yandex.ru");

    private ItemRequestDto itemRequestDto = new ItemRequestDto(1L, "ItemRequest description",
            userDto, LocalDateTime.of(2022, 1, 2, 3, 4, 5), null);

    private List<ItemRequestDto> listItemRequestDto = new ArrayList<>();

    @Test
    void createItemRequest() throws Exception {
        when(itemRequestService.addItemRequest(any(Long.class), any(), any(LocalDateTime.class)))
                .thenReturn(itemRequestDto);
        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.requesterName.id", is(itemRequestDto.getRequesterName().getId()), Long.class))
                .andExpect(jsonPath("$.requesterName.name", is(itemRequestDto.getRequesterName().getName())))
                .andExpect(jsonPath("$.requesterName.email", is(itemRequestDto.getRequesterName().getEmail())))
                .andExpect(jsonPath("$.created",
                        is(itemRequestDto.getCreated().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))));
    }

    @Test
    void getItemRequest() throws Exception {
        when(itemRequestService.getItemRequestOfId(any(Long.class), any(Long.class)))
                .thenReturn(itemRequestDto);
        mvc.perform(get("/requests/1")
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.requesterName.id", is(itemRequestDto.getRequesterName().getId()), Long.class))
                .andExpect(jsonPath("$.requesterName.name", is(itemRequestDto.getRequesterName().getName())))
                .andExpect(jsonPath("$.requesterName.email", is(itemRequestDto.getRequesterName().getEmail())))
                .andExpect(jsonPath("$.created",
                        is(itemRequestDto.getCreated().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))));
    }

    @Test
    void getOwnItemRequests() throws Exception {
        when(itemRequestService.getItemRequestsOwnerSorted(any(Long.class), any()))
                .thenReturn(List.of(itemRequestDto));
        mvc.perform(get("/requests")
                        .content(mapper.writeValueAsString(listItemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.[0].requesterName.id", is(itemRequestDto.getRequesterName().getId()), Long.class))
                .andExpect(jsonPath("$.[0].requesterName.name", is(itemRequestDto.getRequesterName().getName())))
                .andExpect(jsonPath("$.[0].requesterName.email", is(itemRequestDto.getRequesterName().getEmail())))
                .andExpect(jsonPath("$.[0].created",
                        is(itemRequestDto.getCreated().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))));
    }

    @Test
    void getItemRequests() throws Exception {
        when(itemRequestService.getItemRequestsOtherSorted(any(Long.class), any()))
                .thenReturn(List.of(itemRequestDto));
        mvc.perform(get("/requests/all")
                        .content(mapper.writeValueAsString(listItemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.[0].requesterName.id", is(itemRequestDto.getRequesterName().getId()), Long.class))
                .andExpect(jsonPath("$.[0].requesterName.name", is(itemRequestDto.getRequesterName().getName())))
                .andExpect(jsonPath("$.[0].requesterName.email", is(itemRequestDto.getRequesterName().getEmail())))
                .andExpect(jsonPath("$.[0].created",
                        is(itemRequestDto.getCreated().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))));
    }

}