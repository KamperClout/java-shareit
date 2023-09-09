package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserService;

import javax.validation.Valid;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
@Slf4j
public class ItemController {
    private static final String OWNER = "X-Sharer-User-Id";
    private ItemService itemService;
    private UserService userService;


    @Autowired
    public ItemController(ItemService itemService, UserService userService) {
        this.itemService = itemService;
        this.userService = userService;
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable Long itemId) {
        log.info("Получен GET-запрос: '/items' на получение вещи с ID={}", itemId);
        return itemService.getItemById(itemId);
    }

    @PostMapping
    public ItemDto create(@Valid @RequestBody ItemDto itemDto, @RequestHeader(OWNER) Long ownerId) {
        log.info("Получен POST-запрос: '/items' на добавление вещи владельцем с ID={}", ownerId);
        ItemDto newItemDto = null;
        if (userService.getUserById(ownerId) != null) {
            newItemDto = itemService.create(itemDto, ownerId);
        }
        return newItemDto;
    }

    @GetMapping
    public List<ItemDto> findAllItems(@RequestHeader(OWNER) Long ownerId) {
        log.info("Получен GET-запрос: '/items' на получение всех вещей владельца с ID={}", ownerId);
        return itemService.findAllItems(ownerId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestBody ItemDto itemDto, @PathVariable Long itemId,
                          @RequestHeader(OWNER) Long ownerId) {
        log.info("Получен PATCH-запрос: '/items' на обновление вещи с ID={}", itemId);
        ItemDto newItemDto = null;
        if (userService.getUserById(ownerId) != null) {
            newItemDto = itemService.update(itemDto, ownerId, itemId);
        }
        return newItemDto;
    }

    @DeleteMapping("/{itemId}")
    public ItemDto delete(@PathVariable Long itemId, @RequestHeader(OWNER) Long ownerId) {
        log.info("Получен DELETE-запрос: '/items' на удаление вещи с ID={}", itemId);
        return itemService.delete(itemId, ownerId);
    }

    @GetMapping("/search")
    public List<ItemDto> itemsSearch(@RequestParam String text) {
        log.info("Получен GET-запрос: '/items/search' на поиск вещи с текстом={}", text);
        return itemService.getItemsBySearchQuery(text);
    }
}