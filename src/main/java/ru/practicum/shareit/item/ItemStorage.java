package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    Item create(Item item);

    Item update(Item item);

    Item getItemById(Long itemId);

    Item delete(Long id);

    List<Item> findAllItems(Long ownerId);

    List<Item> itemSearch(String text);
}
