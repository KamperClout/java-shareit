package ru.practicum.shareit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

@Service
public class OrchestratorService {
    private final UserService userService;

    private final ItemService itemService;

    private final BookingService bookingService;

    @Autowired
    public OrchestratorService(UserService userService, ItemService itemService, BookingService bookingService) {
        this.userService = userService;
        this.itemService = itemService;
        this.bookingService = bookingService;
    }

    public boolean isExistUser(Long userId) {
        boolean exist = false;
        if (userService.getUserById(userId) != null) {
            exist = true;
        }
        return exist;
    }

    public boolean isAvailableItem(Long itemId) {
        return itemService.findItemById(itemId).getAvailable();
    }

    public User findUserById(Long userId) {
        return userService.findUserById(userId);
    }

    public boolean isItemOwner(Long itemId, Long userId) {

        return itemService.findAllItems(userId,null).stream()
                .anyMatch(i -> i.getId().equals(itemId));
    }

    public Booking getBookingWithUserBookedItem(Long itemId, Long userId) {
        return bookingService.getBookingWithUserBookedItem(itemId, userId);
    }
}
