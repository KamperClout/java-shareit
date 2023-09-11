package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.mapper.Mapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;


import java.util.List;
import java.util.stream.Collectors;


@Service
public class ItemService {
    private ItemStorage itemStorage;
    private UserService userService;

    @Autowired
    public ItemService(@Qualifier("InMemoryItemStorage") ItemStorage itemStorage,UserService userService) {
        this.itemStorage = itemStorage;
        this.userService = userService;
    }

    public ItemDto create(ItemDto itemDto, Long ownerId) {
        User user = Mapper.toUser(userService.getUserById(ownerId));
        Item item = Mapper.toItem(itemDto,user.getId());
        return Mapper.toItemDto(itemStorage.create(item));
    }

    public List<ItemDto> findAllItems(Long ownderId) {
        return itemStorage.findAllItems(ownderId).stream()
                .map(Mapper::toItemDto)
                .collect(Collectors.toList());
    }

    public ItemDto getItemById(Long id) {
        return Mapper.toItemDto(itemStorage.getItemById(id));
    }

    public ItemDto update(ItemDto itemDto, Long ownerId, Long itemId) {
        User user = Mapper.toUser(userService.getUserById(ownerId));
        Item item = Mapper.toItem(itemDto,user.getId());
        if (item.getId() == null) {
            item.setId(itemId);
        }
        Item oldItem = itemStorage.getItemById(itemId);
        if (!oldItem.getOwnerId().equals(ownerId)) {
            throw new ItemNotFoundException("У пользователя нет такой вещи!");
        }
        return Mapper.toItemDto(itemStorage.update(item));
    }

    public ItemDto delete(Long itemId, Long ownerId) {
        Item item = itemStorage.getItemById(itemId);
        if (!item.getOwnerId().equals(ownerId)) {
            throw new ItemNotFoundException("У пользователя нет такой вещи!");
        }
        return Mapper.toItemDto(itemStorage.delete(itemId));
    }


    public List<ItemDto> getItemsBySearchQuery(String text) {
        text = text.toLowerCase();
        return itemStorage.itemSearch(text).stream()
                .map(Mapper::toItemDto)
                .collect(Collectors.toList());
    }
}
