package ru.practicum.shareit.request;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-item-requests.
 */

@Data
public class ItemRequest {
    private Long id;
    private String description;
    private String requestorName;
    private LocalDateTime created;
}
