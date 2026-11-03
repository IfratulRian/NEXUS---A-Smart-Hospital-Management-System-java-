package nexus.utility.validation;

import nexus.exception.ValidationException;
import java.time.LocalDate;
import java.util.regex.Pattern;

/** Reusable, composable validators (lambdas + Validator.and). */
public final class Validators {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{7,15}$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_.]{3,20}$");

    public static final Validator<String> NAME = notBlank("Name").and(maxLength("Name", 80));
    public static final Validator<String> EMAIL = matches("Email", EMAIL_PATTERN, "e.g. name@example.com");
    public static final Validator<String> PHONE = matches("Phone", PHONE_PATTERN, "7-15 digits, optional leading +");
    public static final Validator<String> USERNAME = matches("Username", USERNAME_PATTERN, "3-20 letters, digits, _ or .");
    public static final Validator<String> NOT_BLANK = notBlank("Value");

    private Validators() { }

    public static Validator<String> notBlank(String field) {
        return v -> { if (v == null || v.isBlank()) throw new ValidationException(field + " must not be blank"); };
    }

    public static Validator<String> maxLength(String field, int max) {
        return v -> { if (v != null && v.length() > max) throw new ValidationException(field + " must be at most " + max + " characters"); };
    }

    public static Validator<String> matches(String field, Pattern pattern, String hint) {
        return v -> {
            if (v == null || !pattern.matcher(v.trim()).matches())
                throw new ValidationException("Invalid " + field.toLowerCase() + " (" + hint + ")");
        };
    }

    public static void requireNonNull(Object value, String field) {
        if (value == null) throw new ValidationException(field + " is required");
    }

    public static void requireDateOfBirth(LocalDate dob) {
        requireNonNull(dob, "Date of birth");
        if (dob.isAfter(LocalDate.now())) throw new ValidationException("Date of birth cannot be in the future");
        if (dob.isBefore(LocalDate.now().minusYears(130))) throw new ValidationException("Date of birth is unrealistic");
    }

    public static void requireNonNegative(double value, String field) {
        if (value < 0 || Double.isNaN(value)) throw new ValidationException(field + " must be zero or positive");
    }
}
