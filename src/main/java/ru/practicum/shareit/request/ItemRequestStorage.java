package ru.practicum.shareit.request;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRequestStorage extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findAllByRequesterIdOrderByCreatedDesc(PageRequest pageReq, long userId);

    List<ItemRequest> findAllByRequesterIdNotOrderByCreatedDesc(PageRequest pageReq, long userId);
}
