package ru.practicum.shareit.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * TODO Sprint add-item-requests.
 */

@Data
public class ItemRequest {
    private Long id;                  // уникальный идентификатор запроса
    private String description;       // текст запроса, содержащий описание требуемой вещи
    private String requestorName;     // пользователь, создавший запрос
    private LocalDateTime created;    // дата и время создания запроса
}
