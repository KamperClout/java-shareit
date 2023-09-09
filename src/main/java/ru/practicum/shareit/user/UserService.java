package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.mapper.Mapper;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("InMemoryUserStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<UserDto> getUsers() {
        return userStorage.getUsers().stream()
                .map(Mapper::toUserDto)
                .collect(Collectors.toList());
    }


    public UserDto getUserById(Long id) {
        return Mapper.toUserDto(userStorage.getUserById(id));
    }

    public UserDto create(UserDto userDto) {
        return Mapper.toUserDto(userStorage.create(Mapper.toUser(userDto)));
    }

    public UserDto update(UserDto userDto, Long id) {
        if (userDto.getId() == null) {
            userDto.setId(id);
        }
        return Mapper.toUserDto(userStorage.update(Mapper.toUser(userDto)));
    }

    public UserDto delete(Long userId) {
        return Mapper.toUserDto(userStorage.delete(userId));
    }
}