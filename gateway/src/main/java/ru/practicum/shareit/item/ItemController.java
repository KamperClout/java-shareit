package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;


@Controller
@RequestMapping("/items")
@Slf4j
@RequiredArgsConstructor
public class ItemController {
    private static final String OWNER = "X-Sharer-User-Id";
    private final ItemClient itemClient;

    @GetMapping
    public ResponseEntity<Object> getItems(@RequestHeader(OWNER) @Min(1) Long userId,
                                           @Min(0) @RequestParam(defaultValue = "0") Integer from,
                                           @Min(1) @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получение всех вещей пользователя {}", userId);
        return itemClient.getItems(userId, from, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getItem(@PathVariable Long id,
                                          @RequestHeader(OWNER) @Min(1) Long userId) {
        log.info("Получение вещи {}", id);
        return itemClient.getItem(id, userId);
    }

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader(OWNER) @Min(1) Long userId,
                                             @RequestBody @Valid ItemDto itemDto) {
        log.info("Создание вещи");
        return itemClient.createItem(userId, itemDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateItem(@RequestBody ItemDto itemDto,
                                             @PathVariable("id") @Min(1) Long id,
                                             @RequestHeader(OWNER) @Min(1) Long userId) {
        log.info("Обновление вещи {}", id);
        return itemClient.updateItem(itemDto, id, userId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteItem(@PathVariable("id") @Min(1) Long id) {
        log.info("Удаление вещи {}", id);
        return itemClient.deleteItem(id);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItem(@RequestHeader(OWNER) Long userId,
                                             @RequestParam(required = false) String text,
                                             @RequestParam(defaultValue = "0") Integer from,
                                             @RequestParam(defaultValue = "20") Integer size) {
        log.info("Поиск вещи по тексту {}", text);
        return itemClient.searchItem(userId, text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@PathVariable("itemId") @Min(1) Long itemId,
                                                @RequestHeader(OWNER) @Min(1) Long userId,
                                                @Valid @RequestBody CommentDto commentDto) {
        log.info("Добавление комментария к вещи {}", itemId);
        return itemClient.createComment(itemId, userId, commentDto);
    }
}