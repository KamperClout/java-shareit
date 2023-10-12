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
public class BookingForItemTest {
    private JacksonTester<BookingForItem> json;
    private BookingForItem bookingInputDto;
    private Validator validator;

    public BookingForItemTest(@Autowired JacksonTester<BookingForItem> json) {
        this.json = json;
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void beforeEach() {
        bookingInputDto = new BookingForItem(
                1L,
                LocalDateTime.of(2030,12,25,12,00),
                LocalDateTime.of(2030,12,26,12,00)
        );
    }

    @Test
    void testJsonBookingInputDto() throws Exception {
        JsonContent<BookingForItem> result = json.write(bookingInputDto);
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2030-12-25T12:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2030-12-26T12:00:00");
    }

    @Test
    void whenBookingInputDtoIsValidThenViolationsShouldBeEmpty() {
        Set<ConstraintViolation<BookingForItem>> violations = validator.validate(bookingInputDto);
        assertThat(violations).isEmpty();
    }

    @Test
    void whenBookingInputDtoStartNotNullThenViolationsShouldBeReportedNotNull() {
        bookingInputDto.setStart(null);
        Set<ConstraintViolation<BookingForItem>> violations = validator.validate(bookingInputDto);
        assertThat(violations).isNotEmpty();
        assertThat(violations.toString()).contains("interpolatedMessage='не должно равняться null'");
    }

    @Test
    void whenBookingInputDtoEndNotNullThenViolationsShouldBeReportedNotNull() {
        bookingInputDto.setEnd(null);
        Set<ConstraintViolation<BookingForItem>> violations = validator.validate(bookingInputDto);
        assertThat(violations).isNotEmpty();
        assertThat(violations.toString()).contains("interpolatedMessage='не должно равняться null'");
    }

    @Test
    void whenBookingInputDtoStartBeforeNowThenViolationsShouldBeReportedNotNull() {
        bookingInputDto.setStart(LocalDateTime.now().minusSeconds(1));
        Set<ConstraintViolation<BookingForItem>> violations = validator.validate(bookingInputDto);
        System.out.println(violations);
        assertThat(violations).isNotEmpty();
        assertThat(violations.toString()).contains("должно содержать сегодняшнее число или дату, которая еще не наступила'");
    }

    @Test
    void whenBookingInputDtoEndBeforeNowThenViolationsShouldBeReportedNotNull() {
        bookingInputDto.setEnd(LocalDateTime.now().minusSeconds(1));
        Set<ConstraintViolation<BookingForItem>> violations = validator.validate(bookingInputDto);
        assertThat(violations).isNotEmpty();
        assertThat(violations.toString()).contains("interpolatedMessage='должно содержать дату, которая еще не наступила'");
    }
}