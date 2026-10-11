package dev.abdulrahman.excelsior.business.user.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRegistrationDtoTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDownValidator() {
        if (validatorFactory != null) {
            validatorFactory.close();
        }
    }

    @Test
    void validate_ValidRegistrationDto_NoViolations() {
        UserRegistrationDto dto = new UserRegistrationDto("valid_user-123", "ValidPass123");
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_UsernameMinLengthBoundary_Valid() {
        UserRegistrationDto dto = new UserRegistrationDto("abc", "ValidPass123");
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_UsernameMaxLengthBoundary_Valid() {
        String username32Chars = "a".repeat(32);
        UserRegistrationDto dto = new UserRegistrationDto(username32Chars, "ValidPass123");
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_UsernameNull_ViolatesNotBlank() {
        UserRegistrationDto dto = new UserRegistrationDto(null, "ValidPass123");
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "Username cannot be blank".equals(v.getMessage())));
    }

    @Test
    void validate_UsernameBlank_ViolatesNotBlank() {
        UserRegistrationDto dto = new UserRegistrationDto("   ", "ValidPass123");
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "Username cannot be blank".equals(v.getMessage())));
    }

    @Test
    void validate_UsernameTooShort_ViolatesSize() {
        UserRegistrationDto dto = new UserRegistrationDto("ab", "ValidPass123");
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "Username must be between 3 and 32 characters long".equals(v.getMessage())));
    }

    @Test
    void validate_UsernameTooLong_ViolatesSize() {
        String username33Chars = "a".repeat(33);
        UserRegistrationDto dto = new UserRegistrationDto(username33Chars, "ValidPass123");
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "Username must be between 3 and 32 characters long".equals(v.getMessage())));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "user name",
            "user@name",
            "user#1",
            "user!name",
            "user.name",
            "user$name",
            "user+name",
            "user?name"
    })
    void validate_UsernameWithInvalidCharacters_ViolatesPattern(String invalidUsername) {
        UserRegistrationDto dto = new UserRegistrationDto(invalidUsername, "ValidPass123");
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v ->
                "Username can only contain letters, numbers underscores and hyphens".equals(v.getMessage())));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "simpleUser",
            "user_name",
            "user-name",
            "user_name-99",
            "12345",
            "USER_123"
    })
    void validate_UsernameWithAllowedPatterns_Valid(String validUsername) {
        UserRegistrationDto dto = new UserRegistrationDto(validUsername, "ValidPass123");
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_PasswordMinLengthBoundary_Valid() {
        UserRegistrationDto dto = new UserRegistrationDto("validuser", "Passwo12");
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_PasswordNull_ViolatesNotBlank() {
        UserRegistrationDto dto = new UserRegistrationDto("validuser", null);
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "Password cannot be blank".equals(v.getMessage())));
    }

    @Test
    void validate_PasswordBlank_ViolatesNotBlank() {
        UserRegistrationDto dto = new UserRegistrationDto("validuser", "   ");
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "Password cannot be blank".equals(v.getMessage())));
    }

    @Test
    void validate_PasswordTooShort_ViolatesSize() {
        UserRegistrationDto dto = new UserRegistrationDto("validuser", "Pass123");
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "Password must be at least 8 characters long".equals(v.getMessage())));
    }

    @Test
    void validate_PasswordMissingUppercase_ViolatesPattern() {
        UserRegistrationDto dto = new UserRegistrationDto("validuser", "password123");
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v ->
                "Password must contain at least one uppercase letter, one lowercase letter, and one number".equals(v.getMessage())));
    }

    @Test
    void validate_PasswordMissingLowercase_ViolatesPattern() {
        UserRegistrationDto dto = new UserRegistrationDto("validuser", "PASSWORD123");
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v ->
                "Password must contain at least one uppercase letter, one lowercase letter, and one number".equals(v.getMessage())));
    }

    @Test
    void validate_PasswordMissingDigit_ViolatesPattern() {
        UserRegistrationDto dto = new UserRegistrationDto("validuser", "PasswordNoDigit");
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v ->
                "Password must contain at least one uppercase letter, one lowercase letter, and one number".equals(v.getMessage())));
    }

    @Test
    void validate_PasswordWithSpecialCharacters_Valid() {
        UserRegistrationDto dto = new UserRegistrationDto("validuser", "P@ssw0rd!#$");
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_BothFieldsInvalid_MultipleViolations() {
        UserRegistrationDto dto = new UserRegistrationDto("!", "bad");
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(dto);

        assertTrue(violations.size() >= 2);
    }
}
