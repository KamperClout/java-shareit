package ru.practicum.shareit.item;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage extends JpaRepository<Item, Long> {
    List<Item> findByOwnerId(Long ownerId);

    @Query(" select i from Item i " +
            "where lower(i.name) like lower(concat('%', :search, '%')) " +
            " or lower(i.description) like lower(concat('%', :search, '%')) " +
            " and i.available = true")
    List<Item> getItemsBySearchQuery(@Param("search") String text, PageRequest pageRequest);

    List<Item> findItemsByRequestId(Long requestId);

    @Query(" select i from Item i " +
            "where i.requestId is not null")
    List<Item> findAllWithNonNullRequest();

    List<Item> findAllByRequestId(Long requestId, Sort sort);
}
