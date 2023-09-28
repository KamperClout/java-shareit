package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.UserAlreadyExistsException;
import ru.practicum.shareit.exceptions.UserNotFoundException;
import ru.practicum.shareit.mapper.Mapper;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<UserDto> getUsers() {
        return userStorage.findAll().stream()
                .map(Mapper::toUserDto)
                .collect(Collectors.toList());
    }


    public UserDto getUserById(Long id) {
        return Mapper.toUserDto(userStorage.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с ID=" + id + " не найден!")));
    }

    public UserDto create(UserDto userDto) {
        try {
            return Mapper.toUserDto(userStorage.save(Mapper.toUser(userDto)));
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistsException("Пользователь с E-mail=" +
                    userDto.getEmail() + " уже существует!");
        }

    }

    public UserDto update(UserDto userDto, Long id) {
        if (userDto.getId() == null) {
            userDto.setId(id);
        }
        User user = userStorage.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с ID=" + id + " не найден!"));
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if ((userDto.getEmail() != null) && (!userDto.getEmail().equals(user.getEmail()))) {
            if (userStorage.findAll()
                    .stream()
                    .filter(u -> u.getEmail().equals(userDto.getEmail()))
                    .allMatch(u -> u.getId().equals(userDto.getId()))) {
                user.setEmail(userDto.getEmail());
            } else {
                throw new UserAlreadyExistsException("Пользователь с E-mail=" + user.getEmail() + " уже существует!");
            }

        }
        return Mapper.toUserDto(userStorage.save(user));
    }


    public void delete(Long userId) {
        try {
            userStorage.deleteById(userId);
        } catch (EmptyResultDataAccessException e) {
            throw new UserNotFoundException("Пользователь с ID=" + userId + " не найден!");
        }
    }


    public User findUserById(Long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с ID=" + id + " не найден!"));
    }
}