package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingForItem {
    private Long itemId;
    @FutureOrPresent(message = "должно содержать сегодняшнее число или дату, которая еще не наступила")
    @NotNull(message = "не должно равняться null")
    private LocalDateTime start;
    @Future(message = "должно содержать дату, которая еще не наступила")
    @NotNull(message = "не должно равняться null")
    private LocalDateTime end;
}
