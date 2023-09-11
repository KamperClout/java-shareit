package ru.practicum.shareit.user;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.UserAlreadyExistsException;
import ru.practicum.shareit.exceptions.UserNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("InMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {
    private Map<Long, User> users = new HashMap<>();
    private Long userId = 0L;

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User create(User user) {
        if (users.values().stream().anyMatch(u -> u.getEmail().equals(user.getEmail()))) {
            throw new UserAlreadyExistsException("Пользователь с E-mail=" + user.getEmail() + " уже существует");
        }
        if (isValidUser(user)) {
            if (user.getId() == null) {
                user.setId(++userId);
            }
            users.put(user.getId(), user);
        }
        return user;
    }

    @Override
    public User update(User user) {
        if (user.getId() == null) {
            throw new ValidationException("Передано пустое id!");
        }
        if (!users.containsKey(user.getId())) {
            throw new UserNotFoundException("Пользователь с ID=" + user.getId() + " не найден!");
        }
        if (user.getName() == null) {
            user.setName(users.get(user.getId()).getName());
        }
        if (user.getEmail() == null) {
            user.setEmail(users.get(user.getId()).getEmail());
        }
        if (users.values().stream()
                .filter(u -> u.getEmail().equals(user.getEmail()))
                .anyMatch(u -> !u.getId().equals(user.getId()))) {
            throw new UserAlreadyExistsException("Пользователь с E-mail=" + user.getEmail() + " уже существует!");
        }
        if (isValidUser(user)) {
            users.put(user.getId(), user);
        }
        return user;
    }

    @Override
    public User getUserById(Long userId) {
        if (!users.containsKey(userId)) {
            throw new UserNotFoundException("Пользователь с ID=" + userId + " не найден!");
        }
        return users.get(userId);
    }

    @Override
    public User delete(Long userId) {
        if (userId == null) {
            throw new ValidationException("Передано пустое id!");
        }
        if (!users.containsKey(userId)) {
            throw new UserNotFoundException("Пользователь с ID=" + userId + " не найден!");
        }
        return users.remove(userId);
    }

    private boolean isValidUser(User user) {
        if (!user.getEmail().contains("@")) {
            throw new ValidationException("Некорректный e-mail пользователя: " + user.getEmail());
        }
        if (user.getName().isEmpty() || user.getName().contains(" ")) {
            throw new ValidationException("Некорректный логин пользователя: " + user.getName());
        }
        return true;
    }
}
