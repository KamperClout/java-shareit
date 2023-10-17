package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingForItem;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingForItemTest {
    JacksonTester<BookingForItem> json;
    BookingForItem bookingForItem;
    Validator validator;

    BookingForItemTest(@Autowired JacksonTester<BookingForItem> json) {
        this.json = json;
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void beforeEach() {
        bookingForItem = new BookingForItem(
                1L,
                LocalDateTime.of(2030, 12, 25, 12, 00),
                LocalDateTime.of(2030, 12, 26, 12, 00)
        );
    }

    @Test
    void testJsonbookingForItem() throws Exception {
        JsonContent<BookingForItem> result = json.write(bookingForItem);
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2030-12-25T12:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2030-12-26T12:00:00");
    }

    @Test
    void whenbookingForItemIsValidThenViolationsShouldBeEmpty() {
        Set<ConstraintViolation<BookingForItem>> violations = validator.validate(bookingForItem);
        assertThat(violations).isEmpty();
    }

    @Test
    void whenbookingForItemStartNotNullThenViolationsShouldBeReportedNotNull() {
        bookingForItem.setStart(null);
        Set<ConstraintViolation<BookingForItem>> violations = validator.validate(bookingForItem);
        assertThat(violations).isNotEmpty();
        assertThat(violations.toString()).contains("interpolatedMessage='не должно равняться null'");
    }

    @Test
    void whenbookingForItemEndNotNullThenViolationsShouldBeReportedNotNull() {
        bookingForItem.setEnd(null);
        Set<ConstraintViolation<BookingForItem>> violations = validator.validate(bookingForItem);
        assertThat(violations).isNotEmpty();
        assertThat(violations.toString()).contains("interpolatedMessage='не должно равняться null'");
    }

    @Test
    void whenbookingForItemStartBeforeNowThenViolationsShouldBeReportedNotNull() {
        bookingForItem.setStart(LocalDateTime.now().minusSeconds(1));
        Set<ConstraintViolation<BookingForItem>> violations = validator.validate(bookingForItem);
        System.out.println(violations);
        assertThat(violations).isNotEmpty();
        assertThat(violations.toString()).contains("должно содержать сегодняшнее число или дату, которая еще не наступила'");
    }

    @Test
    void whenbookingForItemEndBeforeNowThenViolationsShouldBeReportedNotNull() {
        bookingForItem.setEnd(LocalDateTime.now().minusSeconds(1));
        Set<ConstraintViolation<BookingForItem>> violations = validator.validate(bookingForItem);
        assertThat(violations).isNotEmpty();
        assertThat(violations.toString()).contains("interpolatedMessage='должно содержать дату, которая еще не наступила'");
    }
}
