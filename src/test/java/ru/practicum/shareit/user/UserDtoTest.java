package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class UserDtoTest {

    private JacksonTester<UserDto> json;
    private UserDto userDto;
    private Validator validator;

    public UserDtoTest(@Autowired JacksonTester<UserDto> json) {
        this.json = json;
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void beforeEach() {
        userDto = new UserDto(
                1L,
                "Pavel",
                "kamperinc@yandex.ru"
        );
    }

    @Test
    void testJsonUserDto() throws Exception {

        JsonContent<UserDto> result = json.write(userDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Pavel");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("kamperinc@yandex.ru");
    }

    @Test
    void whenUserDtoIsValidThenViolationsShouldBeEmpty() {
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
        assertThat(violations).isEmpty();
    }

    @Test
    void whenUserDtoNameIsBlankThenViolationsShouldBeReportedNotBlank() {
        userDto.setName(" ");
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
        assertThat(violations).isNotEmpty();
        assertThat(violations.toString()).contains("interpolatedMessage='не должно быть пустым'");
    }

    @Test
    void whenUserDtoNameIsNullThenViolationsShouldBeReportedNotBlank() {
        userDto.setName(null);
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
        assertThat(violations).isNotEmpty();
        assertThat(violations.toString()).contains("interpolatedMessage='не должно быть пустым'");
    }

    @Test
    void whenUserDtoEmailIsBlankThenViolationsShouldBeReportedNotBlank() {
        userDto.setEmail(" ");
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
        assertThat(violations).isNotEmpty();
        assertThat(violations.toString()).contains("interpolatedMessage='не должно быть пустым'");
    }

    @Test
    void whenUserDtoEmailNotEmailThenViolationsShouldBeReportedNotEmail() {
        userDto.setEmail("alex.alex");
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
        assertThat(violations).isNotEmpty();
        System.out.println(violations.toString());
        assertThat(violations.toString()).contains("interpolatedMessage='должно иметь формат адреса электронной почты'");
    }

    @Test
    void whenUserDtoEmailIsNullThenViolationsShouldBeReportedNotBlank() {
        userDto.setEmail(null);
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
        assertThat(violations).isNotEmpty();
        assertThat(violations.toString()).contains("interpolatedMessage='не должно быть пустым'");
    }
}
