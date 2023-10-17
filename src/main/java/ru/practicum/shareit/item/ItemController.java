package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exceptions.BadRequestException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/items")
@Slf4j
public class ItemController {
    private static final String OWNER = "X-Sharer-User-Id";
    private final ItemService itemService;


    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable Long itemId, @RequestHeader(OWNER) Long ownerId) {
        log.info("Получен GET-запрос: '/items' на получение вещи с ID={}", itemId);
        return itemService.getItemById(itemId, ownerId);
    }

    @PostMapping
    public ItemDto create(@Valid @RequestBody ItemDto itemDto, @RequestHeader(OWNER) Long ownerId) {
        log.info("Получен POST-запрос: '/items' на добавление вещи владельцем с ID={}", ownerId);
        return itemService.create(itemDto, ownerId);
    }

    @GetMapping
    public List<ItemDto> findAllItems(@RequestHeader(OWNER) Long ownerId, @RequestParam(defaultValue = "0") int from,
                                      @RequestParam(defaultValue = "20") int size) {
        log.info("Получен GET-запрос: '/items' на получение всех вещей владельца с ID={}", ownerId);
        if (from < 0 || size <= 0) {
            throw new BadRequestException("параметры пагинации не могут быть отрицательными или равны нулю");
        }
        return itemService.findAllItems(ownerId, PageRequest.of(from / size, size));
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestBody ItemDto itemDto, @PathVariable Long itemId,
                          @RequestHeader(OWNER) Long ownerId) {
        log.info("Получен PATCH-запрос: '/items' на обновление вещи с ID={}", itemId);
        return itemService.update(itemDto, ownerId, itemId);
    }

    @DeleteMapping("/{itemId}")
    public void delete(@PathVariable Long itemId, @RequestHeader(OWNER) Long ownerId) {
        log.info("Получен DELETE-запрос: '/items' на удаление вещи с ID={}", itemId);
        itemService.delete(itemId, ownerId);
    }

    @GetMapping("/search")
    public List<ItemDto> itemsSearch(@RequestParam String text, @RequestParam(defaultValue = "0") int from,
                                     @RequestParam(defaultValue = "20") int size) {
        log.info("Получен GET-запрос: '/items/search' на поиск вещи с текстом={}", text);
        if (from < 0 || size <= 0) {
            throw new BadRequestException("параметры пагинации не могут быть отрицательными или равны нулю");
        }
        return itemService.getItemsBySearchQuery(text, PageRequest.of(from / size, size));
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(@Valid @RequestBody CommentDto commentDto, @RequestHeader(OWNER) Long userId,
                                    @PathVariable Long itemId) {
        log.info("Получен POST-запрос: '/items/comment' на" +
                " добавление отзыва пользователем с ID={}", userId);
        return itemService.createComment(commentDto, itemId, userId);
    }
}