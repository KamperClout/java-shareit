package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.ItemNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("InMemoryItemStorage")
public class InMemoryItemStorage implements ItemStorage {

    private Map<Long, Item> items = new HashMap<>();
    private Long itemId = 0L;

    @Override
    public Item create(Item item) {
        validateItem(item);
        item.setId(++itemId);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item item) {
        if (item.getId() == null) {
            throw new ValidationException("передан пустой id");
        }
        if (!items.containsKey(item.getId())) {
            throw new ItemNotFoundException("Вещь с id=" + item.getId() + " не найдена");
        }
        if (item.getName() == null) {
            item.setName(items.get(item.getId()).getName());
        }
        if (item.getDescription() == null) {
            item.setDescription(items.get(item.getId()).getDescription());
        }
        if (item.getAvailable() == null) {
            item.setAvailable(items.get(item.getId()).getAvailable());
        }
        validateItem(item);
        items.put(item.getId(), item);

        return item;
    }

    @Override
    public Item delete(Long itemId) {
        if (itemId == null) {
            throw new ValidationException("Передан пустой id");
        }
        if (!items.containsKey(itemId)) {
            throw new ItemNotFoundException("Вещь с id=" + itemId + " не найдена");
        }
        return items.remove(itemId);
    }

    @Override
    public List<Item> findAllItems(Long ownerId) {
        return new ArrayList<>(items.values().stream()
                .filter(item -> item.getOwnerId().equals(ownerId))
                .collect(Collectors.toList()));
    }

    @Override
    public Item getItemById(Long itemId) {
        if (!items.containsKey(itemId)) {
            throw new ItemNotFoundException("Вещь с ID=" + itemId + " не найдена!");
        }
        return items.get(itemId);
    }

    @Override
    public List<Item> itemSearch(String text) {
        List<Item> searchItems = new ArrayList<>();
        if (!text.isBlank()) {
            searchItems = items.values().stream()
                    .filter(Item::getAvailable)
                    .filter(item -> item.getName().toLowerCase().contains(text) ||
                            item.getDescription().toLowerCase().contains(text))
                    .collect(Collectors.toList());
        }
        return searchItems;
    }

    private void validateItem(Item item) {
        if (item.getName().isEmpty() || item.getDescription().isEmpty() || item.getAvailable() == null) {
            throw new ValidationException("Ошибка валидации вещи");
        }
    }
}
