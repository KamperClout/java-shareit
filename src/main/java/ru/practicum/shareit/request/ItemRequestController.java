package ru.practicum.shareit.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exceptions.BadRequestException;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@Slf4j
public class ItemRequestController {
    private final ItemRequestService service;
    private static final String OWNER = "X-Sharer-User-Id";

    @Autowired
    public ItemRequestController(ItemRequestService service) {
        this.service = service;
    }

    @PostMapping
    public ItemRequestDto addItemRequest(@Valid @RequestHeader(OWNER) Long userId,
                                         @RequestBody ItemRequestDto itemRequestDto) {
        log.info("поступил запрос от пользователя {} на добавление запроса вещи {}", userId, itemRequestDto);
        return service.addItemRequest(userId, itemRequestDto, LocalDateTime.now());
    }

    @GetMapping
    public List<ItemRequestDto> getItemRequestsOwnerSorted(@RequestHeader(OWNER) Long userId,
                                                           @RequestParam(defaultValue = "0") int from,
                                                           @RequestParam(defaultValue = "20") int size) {
        log.info("поступил запрос пользователя {} на получение  списка своих запросов вместе с данными об ответах на них", userId);
        if (from < 0 || size <= 0) {
            throw new BadRequestException("параметры пагинации не могут быть отрицательными или равны нулю");
        }
        return service.getItemRequestsOwnerSorted(userId, PageRequest.of(from / size, size));
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getItemRequestsOtherSorted(@RequestHeader(OWNER) Long userId,
                                                           @RequestParam(defaultValue = "0") int from,
                                                           @RequestParam(defaultValue = "20") int size) {
        log.info("поступил запрос от пользователя {} на получение списка запросов, созданных другими пользователями", userId);
        if (from < 0 || size <= 0) {
            throw new BadRequestException("параметры пагинации не могут быть отрицательными или равны нулю");
        }
        return service.getItemRequestsOtherSorted(userId, PageRequest.of(from / size, size));
    }

    @GetMapping("/{id}")
    public ItemRequestDto getItemRequestOfId(@RequestHeader(OWNER) Long userId,
                                             @PathVariable("id") Long requestId) {
        log.info("поступил запрос на получение пользователем {} данных об одном " +
                "конкретном запросе id {} вместе с данными об ответах на него", userId, requestId);
        return service.getItemRequestOfId(userId, requestId);
    }
}
