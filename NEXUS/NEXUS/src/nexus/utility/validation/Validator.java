package nexus.utility.validation;

/** Functional interface: throws ValidationException when the value is invalid. */
@FunctionalInterface
public interface Validator<T> {
    void validate(T value);

    default Validator<T> and(Validator<T> next) {
        return value -> { validate(value); next.validate(value); };
    }
}
