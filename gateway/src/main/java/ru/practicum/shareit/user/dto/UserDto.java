package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
public class UserDto {
    private Long id;
    @NotBlank(message = "не должно быть пустым")
    private String name;
    @Email(message = "должно иметь формат адреса электронной почты")
    @NotBlank(message = "не должно быть пустым")
    private String email;
}
