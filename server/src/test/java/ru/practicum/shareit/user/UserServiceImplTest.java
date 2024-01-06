package ru.practicum.shareit.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.exceptions.UserAlreadyExistsException;
import ru.practicum.shareit.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    UserStorage userStorage;
    UserService userService;
    UserDto userDto = new UserDto(1L, "Pavel", "kamperinc@yandex.ru");

    @BeforeEach
    void beforeEach() {
        userService = new UserService(userStorage);
    }

    @Test
    void shouldExceptionWhenGetUserWithWrongId() {
        when(userStorage.findById(any(Long.class)))
                .thenReturn(Optional.empty());
        final UserNotFoundException exception = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserById(-1L));
        Assertions.assertEquals("Пользователь с ID=-1 не найден!", exception.getMessage());
    }

    @Test
    void shouldExceptionWhenCreateUserWithExistEmail() {
        when(userStorage.save(any()))
                .thenThrow(new DataIntegrityViolationException(""));
        final UserAlreadyExistsException exception = Assertions.assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.create(userDto));
        Assertions.assertEquals("Пользователь с E-mail=" + userDto.getEmail() + " уже существует!",
                exception.getMessage());
    }

    @Test
    void shouldReturnUserWhenFindUserById() {
        when(userStorage.findById(any(Long.class)))
                .thenReturn(Optional.of(UserMapper.toUser(userDto)));
        User user = userService.findUserById(1L);
        verify(userStorage, Mockito.times(1))
                .findById(1L);
        assertThat(user.getName(), equalTo(userDto.getName()));
        assertThat(user.getEmail(), equalTo(userDto.getEmail()));
    }

}