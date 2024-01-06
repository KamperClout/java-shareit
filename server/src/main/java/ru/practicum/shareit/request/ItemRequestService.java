package ru.practicum.shareit.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.OrchestratorService;
import ru.practicum.shareit.exceptions.ItemRequestNotFoundException;
import ru.practicum.shareit.exceptions.UserNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemStorage;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItemRequestService {
    private final ItemRequestStorage itemRequestStorage;
    private final OrchestratorService orchestratorService;
    private final ItemRequestMapper itemRequestMapper;
    private final ItemStorage itemStorage;
    private final ItemMapper itemMapper;

    @Autowired
    public ItemRequestService(ItemRequestStorage itemRequestStorage,
                              OrchestratorService orchestratorService, ItemRequestMapper itemRequestMapper,
                              ItemStorage itemStorage, ItemMapper itemMapper) {
        this.itemRequestStorage = itemRequestStorage;
        this.orchestratorService = orchestratorService;
        this.itemRequestMapper = itemRequestMapper;
        this.itemStorage = itemStorage;
        this.itemMapper = itemMapper;
    }

    public ItemRequestDto addItemRequest(Long userId, ItemRequestDto itemRequestDto, LocalDateTime created) {
        if (itemRequestDto.getDescription() == null || itemRequestDto.getDescription().equals("")) {
            throw new ValidationException("описание не может быть пустым");
        }
        if (!orchestratorService.isExistUser(userId)) {
            throw new UserNotFoundException("Пользователь с ID=" + userId + " не найден!");
        }
        ItemRequest itemRequest = itemRequestMapper.toItemRequest(itemRequestDto, userId, created);
        return itemRequestMapper.toItemRequestDto(itemRequestStorage.save(itemRequest));
    }

    public List<ItemRequestDto> getItemRequestsOwnerSorted(Long userId, PageRequest pageReq) {
        if (!orchestratorService.isExistUser(userId)) {
            throw new UserNotFoundException("Пользователь с ID=" + userId + " не найден!");
        }
        List<ItemRequest> requests = itemRequestStorage.findAllByRequesterIdOrderByCreatedDesc(pageReq, userId);
        return addItemsToRequest(requests);
    }

    public List<ItemRequestDto> getItemRequestsOtherSorted(Long userId, PageRequest pageReq) {
        if (!orchestratorService.isExistUser(userId)) {
            throw new UserNotFoundException("Пользователь с ID=" + userId + " не найден!");
        }
        List<ItemRequest> requests = itemRequestStorage.findAllByRequesterIdNotOrderByCreatedDesc(pageReq, userId);
        return addItemsToRequest(requests);
    }

    public ItemRequestDto getItemRequestOfId(Long userId, Long requestId) {
        if (!orchestratorService.isExistUser(userId)) {
            throw new UserNotFoundException("Пользователь с ID=" + userId + " не найден!");
        }

        ItemRequest request = itemRequestStorage.findById(requestId)
                .orElseThrow(() -> new ItemRequestNotFoundException("запроса с id " + requestId + " нет"));

        ItemRequestDto requestDto = itemRequestMapper.toItemRequestDto(request);

        requestDto.setItems(itemStorage.findItemsByRequestId(requestId)
                .stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList()));

        return requestDto;
    }

    private List<ItemRequestDto> addItemsToRequest(List<ItemRequest> requests) {
        List<Item> items = itemStorage.findAllWithNonNullRequest();
        List<ItemRequestDto> itemRequestDto = new ArrayList<>();

        for (ItemRequest iR : requests) {
            List<ItemDto> itemDto = new ArrayList<>();
            for (Item i : items) {
                if (iR.getId().equals(i.getRequestId())) {
                    itemDto.add(itemMapper.toItemDto(i));
                }
            }
            ItemRequestDto dto = itemRequestMapper.toItemRequestDto(iR);
            dto.setItems(itemDto);
            itemRequestDto.add(dto);
        }
        return itemRequestDto;
    }
}
