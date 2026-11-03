package nexus.domain.common;

/** Anything with a unique identifier. Generic so repositories can be reused for any id type. */
public interface Identifiable<T> {
    T getId();
}
