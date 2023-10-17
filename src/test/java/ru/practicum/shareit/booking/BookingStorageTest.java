package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.ItemStorage;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserStorage;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookingStorageTest {
    @Autowired
    BookingStorage bookingStorage;

    @Autowired
    UserStorage userStorage;

    @Autowired
    ItemStorage itemStorage;

    User user;

    Item item;

    User user2;

    Booking booking;

    @BeforeEach
    void beforeEach() {
        user = new User(1L, "name", "user@email.com");

        item = new Item(1L, "name", "description", true, user, null);

        user2 = new User(1L, "name2", "email2@email.com");

        booking = new Booking();
        booking.setStart(LocalDateTime.now());
        booking.setEnd(LocalDateTime.of(2023, 11, 10, 13, 0));
        booking.setStatus(Status.WAITING);
        booking.setBooker(user2);
        booking.setItem(item);
    }

    @Test
    void findCurrentOwnerBookings() {
        User userSaved = userStorage.save(user);
        itemStorage.save(item);
        User userSaved2 = userStorage.save(user2);
        bookingStorage.save(booking);


        assertThat(bookingStorage.findByItem_Owner_IdAndStartIsBeforeAndEndIsAfter(userSaved.getId(),
                        LocalDateTime.now(), LocalDateTime.now(),
                        Sort.by(Sort.Direction.DESC, "start")).size(),
                equalTo(1));
    }

    @Test
    void findCurrentBookerBookings() {
        User userSaved = userStorage.save(user);
        itemStorage.save(item);
        User userSaved2 = userStorage.save(user2);
        bookingStorage.save(booking);

        assertThat(bookingStorage.findAllByBookerIdAndStartBeforeAndEndAfter(userSaved2.getId(),
                        LocalDateTime.now(), LocalDateTime.now(),
                        Sort.by(Sort.Direction.DESC, "start")).size(),
                equalTo(1));
    }

    @Test
    void findPastOwnerBookings() {
        User userSaved = userStorage.save(user);
        Item itemSaved = itemStorage.save(item);
        User userSaved2 = userStorage.save(user2);
        bookingStorage.save(booking);

        assertThat(bookingStorage.findByItem_Owner_IdAndEndIsBefore(userSaved.getId(),
                        LocalDateTime.now(), Sort.by(Sort.Direction.DESC, "start")).size(),
                equalTo(0));
    }

    @Test
    void findFutureOwnerBookings() {
        User userSaved = userStorage.save(user);
        item.setOwner(userSaved);
        Item itemSaved = itemStorage.save(item);
        User userSaved2 = userStorage.save(user2);
        booking.setStart(LocalDateTime.of(2023, 11, 10, 13, 0));
        bookingStorage.save(booking);

        assertThat(bookingStorage.findByItem_Owner_IdAndStartIsAfter(userSaved.getId(),
                        LocalDateTime.now(), Sort.by(Sort.Direction.DESC, "start")).size(),
                equalTo(1));
    }

}